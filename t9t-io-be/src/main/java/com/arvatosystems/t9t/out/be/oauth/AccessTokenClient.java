/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.out.be.oauth;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.oauth.AccessTokenDTO;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.arvatosystems.t9t.out.services.oauth.IAccessTokenClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.jpaw.dp.Dependent;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Dependent
public class AccessTokenClient<KEY> implements IAccessTokenClient<KEY> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenClient.class);

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String FORM_HEADER = "application/x-www-form-urlencoded";
    private static final String DEFAULT_TOKEN_TYPE = "Bearer";
    private static final int EXPIRE_BEFORE_SEC = 5;

    protected final Cache<KEY, AccessTokenDTO> accessTokenCache = Caffeine.newBuilder().build();
    protected final HttpClient httpClient = HttpClient.newHttpClient();
    protected final ObjectMapper jsonMapper = JacksonTools.createObjectMapper().configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .addHandler(new AccessTokenDeserializationProblemHandler());

    @Override
    public AccessTokenDTO getAccessToken(@Nonnull final KEY cacheKey, @Nonnull final String clientId, @Nonnull final String clientSecret,
                                         @Nonnull final String authServerUrl, @Nullable final String authType,
                                         @Nullable final Map<String, Object> additionalParam) {
        return getAccessToken(cacheKey, clientId, clientSecret, authServerUrl, authType, additionalParam, null);
    }

    @Override
    public AccessTokenDTO getAccessToken(@Nonnull final KEY cacheKey, @Nonnull final String clientId, @Nonnull final String clientSecret,
                                         @Nonnull final String authServerUrl, @Nullable final String authType,
                                         @Nullable final Map<String, Object> additionalParam, @Nullable final String scope) {
        LOGGER.debug("Getting access token for cache key: {}", cacheKey);
        AccessTokenDTO token = accessTokenCache.getIfPresent(cacheKey);
        if (token == null) {
            LOGGER.debug("Access token not found for key: {}. Requesting new access token.", cacheKey);
            final AccessTokenDTO newToken = requestNewAccessToken(clientId, clientSecret, authServerUrl, authType, additionalParam, scope);
            accessTokenCache.put(cacheKey, newToken);
            return newToken;
        } else if (isAccessTokenExpired(token)) {
            LOGGER.debug("Access token is expired for key: {}. Refreshing token.", cacheKey);
            final String refreshToken = token.getRefreshToken();
            final AccessTokenDTO newToken;
            if (refreshToken != null) {
                // if refresh token is available, use it to get new access token
                newToken = refreshAccessToken(refreshToken, clientId, clientSecret, authServerUrl, authType, additionalParam, scope);
                if (newToken.getRefreshToken() == null) {
                    populateRefreshToken(newToken, refreshToken);
                }
            } else {
                LOGGER.debug("No refresh token found for key: {}. Acquiring new access token.", cacheKey);
                newToken = requestNewAccessToken(clientId, clientSecret, authServerUrl, authType, additionalParam, scope);
            }
            accessTokenCache.put(cacheKey, newToken);
            return newToken;
        }
        return token;
    }

    @Override
    public void invalidateAccessToken(@Nonnull final KEY cacheKey) {
        LOGGER.debug("Invalidate access token cache for key: {}", cacheKey);
        accessTokenCache.invalidate(cacheKey);
    }

    protected AccessTokenDTO requestNewAccessToken(@Nonnull final String clientId, @Nonnull final String clientSecret, @Nonnull final String authServerUrl,
                                                   @Nullable final String authType, @Nullable final Map<String, Object> additionalParam,
                                                   @Nullable final String scope) {
        final URI accessTokenUri = getNewAccessTokenUri(authServerUrl, additionalParam);
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(accessTokenUri);
        populateAccessTokenRequestHeader(requestBuilder, clientId, clientSecret, authType, additionalParam, scope);
        populateAccessTokenRequestBody(requestBuilder, clientId, clientSecret, authType, additionalParam, scope);
        final HttpRequest httpRequest = requestBuilder.build();
        LOGGER.debug("Sending new access token request to OAuth server: {}", accessTokenUri);
        final HttpResponse<?> httpResponse;
        try {
            httpResponse = httpClient.send(httpRequest, getBodyHandler());
        } catch (Exception ex) {
            LOGGER.error("Unable to send new access token request.", ex);
            throw new T9tException(T9tIOException.ACCESS_TOKEN_ERROR, ex.getMessage());
        }
        LOGGER.debug("Received new access token response with status code {}", httpResponse.statusCode());
            if (httpResponse.statusCode() == 200) {
                final AccessTokenDTO token = parseAccessTokenResponseBody(httpResponse, additionalParam);
                if (token == null || token.getAccessToken() == null) {
                    LOGGER.error("Unable to get new access token. token {}, original body [{}]", token, httpResponse.body());
                    throw new T9tException(T9tIOException.ACCESS_TOKEN_ERROR, "Unable to get new access token");
                }
                return token;
            } else {
                LOGGER.error("Failed to get access token from OAuth server. Status code: {}, body: [{}]", httpResponse.statusCode(), httpResponse.body());
                throw new T9tException(T9tIOException.ACCESS_TOKEN_ERROR, "Failed to get access token from OAuth server");
            }
    }

    protected AccessTokenDTO refreshAccessToken(@Nonnull final String refreshToken, @Nonnull final String clientId, @Nonnull final String clientSecret,
                                                @Nonnull final String authServerUrl, @Nullable final String authType,
                                                @Nullable final Map<String, Object> additionalParam, @Nullable final String scope) {
        final URI refreshTokenUri = getRefreshTokenUri(authServerUrl, additionalParam);
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(refreshTokenUri);
        populateRefreshTokenRequestHeader(requestBuilder, clientId, clientSecret, authType, additionalParam, scope);
        populateRefreshTokenRequestBody(requestBuilder, refreshToken, clientId, clientSecret, authType, additionalParam, scope);
        final HttpRequest httpRequest = requestBuilder.build();
        LOGGER.debug("Sending refresh token request to OAuth server: {}", refreshTokenUri);
        final HttpResponse<?> httpResponse;
        try {
            httpResponse = httpClient.send(httpRequest, getBodyHandler());
        } catch (Exception ex) {
            LOGGER.error("Unable to send refresh token request.", ex);
            throw new T9tException(T9tIOException.ACCESS_TOKEN_ERROR, ex.getMessage());
        }
        LOGGER.debug("Received refresh access token response with status code {}", httpResponse.statusCode());
        if (httpResponse.statusCode() == 200) {
            AccessTokenDTO token = parseRefreshTokenResponseBody(httpResponse, additionalParam);
            if (token == null || token.getAccessToken() == null) {
                LOGGER.error("Unable to refresh access token. token {}, original body [{}]", token, httpResponse.body());
                throw new T9tException(T9tIOException.ACCESS_TOKEN_ERROR, "Unable to refresh access token");
            }
            return token;
        } else {
            LOGGER.error("Failed to refresh access token from OAuth server. Status code: {}, body: [{}]", httpResponse.statusCode(), httpResponse.body());
            throw new T9tException(T9tIOException.ACCESS_TOKEN_ERROR, "Failed to refresh access token from OAuth server");
        }
    }

    protected void populateAccessTokenRequestHeader(@Nonnull final HttpRequest.Builder requestBuilder, @Nonnull final String clientId,
                                                    @Nonnull final String clientSecret, @Nullable final String authType,
                                                    @Nullable final Map<String, Object> additionalParam, @Nullable final String scope) {
        requestBuilder.header(HEADER_CONTENT_TYPE, FORM_HEADER);
        if (authType != null) {
            // if authType is not null, then add client id and client secret to the header
            requestBuilder.header(authType, "Basic " + generateCredential(clientId, clientSecret));
        }
    }

    protected void populateAccessTokenRequestBody(@Nonnull final HttpRequest.Builder requestBuilder, @Nonnull final String clientId,
                                                  @Nonnull final String clientSecret, @Nullable final String authType,
                                                  @Nullable final Map<String, Object> additionalParam, @Nullable final String scope) {
        final StringBuilder requestBody = authType == null ? generateRequestBody(scope, clientId, clientSecret, additionalParam) : generateRequestBody(scope, null, null, additionalParam);
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));
    }

    protected void populateRefreshTokenRequestHeader(@Nonnull final HttpRequest.Builder requestBuilder, @Nonnull final String clientId,
                                                     @Nonnull final String clientSecret, @Nullable final String authType,
                                                     @Nullable final Map<String, Object> additionalParam, @Nullable final String scope) {
        populateAccessTokenRequestHeader(requestBuilder, clientId, clientSecret, authType, additionalParam, scope);
    }

    protected void populateRefreshTokenRequestBody(@Nonnull final HttpRequest.Builder requestBuilder, @Nonnull final String refreshToken,
                                                   @Nonnull final String clientId, @Nonnull final String clientSecret, @Nullable final String authType,
                                                   @Nullable final Map<String, Object> additionalParam, @Nullable final String scope) {
        final StringBuilder requestBody = authType == null ? generateRequestBody(scope, clientId, clientSecret, additionalParam)
                : generateRequestBody(scope, null, null, additionalParam);
        requestBody.append("&refresh_token=").append(refreshToken);
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()));
    }

    protected HttpResponse.BodyHandler<?> getBodyHandler() {
        return HttpResponse.BodyHandlers.ofString();
    }

    protected AccessTokenDTO parseAccessTokenResponseBody(@Nonnull final HttpResponse<?> httpResponse, @Nullable final Map<String, Object> additionalParam) {
        try {
            if (httpResponse.body() != null && httpResponse.body() instanceof String responseBody && !responseBody.isEmpty()) {
                final AccessTokenDTO token = jsonMapper.readValue(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)), AccessTokenDTO.class);
                if (token.getExpiresIn() != null) {
                    final long expiresIn = token.getExpiresIn() > EXPIRE_BEFORE_SEC ? token.getExpiresIn() - EXPIRE_BEFORE_SEC : token.getExpiresIn();
                    token.setExpiredAt(Instant.now().plusSeconds(expiresIn));
                    LOGGER.debug("Received access token expiresIn: {}, expiredAt {}", token.getExpiresIn(), token.getExpiredAt());
                } else {
                    LOGGER.debug("Received access token has no expiry");
                }
                if (token.getTokenType() == null) {
                    token.setTokenType(DEFAULT_TOKEN_TYPE);
                }
                return token;
            }
            LOGGER.error("Unexpected response body [{}],  type: {}", httpResponse.body(), httpResponse.body() != null ? httpResponse.body().getClass() : null);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while parsing access token response body [{}]", httpResponse.body(), ex);
        }
        return null;
    }

    protected AccessTokenDTO parseRefreshTokenResponseBody(@Nonnull final HttpResponse<?> httpResponse, @Nullable final Map<String, Object> additionalParam) {
        return parseAccessTokenResponseBody(httpResponse, additionalParam);
    }

    protected String generateCredential(@Nonnull final String clientId, @Nonnull final String clientSecret) {
        final String credential = clientId + ":" + clientSecret;
        return Base64.getEncoder().encodeToString(credential.getBytes());
    }

    protected StringBuilder generateRequestBody(@Nullable final String scope, @Nullable final String clientId, @Nullable final String clientSecret,
                                                @Nullable final Map<String, Object> additionalParam) {
        final StringBuilder requestBody = new StringBuilder("grant_type=client_credentials");
        if (clientId != null && clientSecret != null) {
            requestBody.append("&client_id=").append(clientId).append("&client_secret=").append(clientSecret);
        }
        if (scope != null) {
            requestBody.append("&scope=").append(scope);
        }
        return requestBody;
    }

    protected boolean isAccessTokenExpired(@Nonnull final AccessTokenDTO accessToken) {
        if (accessToken.getExpiredAt() == null) {
            // token have no expiry
            return false;
        }
        return !accessToken.getExpiredAt().isAfter(Instant.now());
    }

    protected URI getNewAccessTokenUri(@Nonnull final String authServerUrl, @Nullable final Map<String, Object> additionalParam) {
        return URI.create(authServerUrl);
    }

    protected URI getRefreshTokenUri(@Nonnull final String authServerUrl, @Nonnull final Map<String, Object> additionalParam) {
        return getNewAccessTokenUri(authServerUrl, additionalParam);
    }

    protected void populateRefreshToken(@Nonnull final AccessTokenDTO newToken, @Nullable final String refreshToken) {
        // sometimes auth server didn't return refresh token, so we use old one.
        newToken.setRefreshToken(refreshToken);
    }
}
