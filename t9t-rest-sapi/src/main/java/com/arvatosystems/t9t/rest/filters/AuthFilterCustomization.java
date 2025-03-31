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
package com.arvatosystems.t9t.rest.filters;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.JwtAuthentication;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.ipblocker.services.IIPAddressBlocker;
import com.arvatosystems.t9t.rest.services.IAuthFilterCustomization;
import com.arvatosystems.t9t.rest.services.IGatewayAuthChecker;
import com.arvatosystems.t9t.rest.utils.RestUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Sets;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Singleton
public class AuthFilterCustomization implements IAuthFilterCustomization {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRestAuthenticationFilter.class);
    private static final int MAX_AUTH_ENTRIES = 200;
    private static final int MAX_SIZE_AUTH_HEADER = 4096;

    protected static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    protected static final Pattern BASE64_PATTERN = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");

    protected final boolean enableSwagger = RestUtils.checkIfSet("t9t.restapi.swagger", Boolean.FALSE);
    protected final boolean enablePing = enableSwagger || RestUtils.checkIfSet("t9t.restapi.unauthedPing", Boolean.FALSE); // swagger implies ping
    protected final boolean allowApiKey = RestUtils.checkIfSet("t9t.restapi.apikey", Boolean.TRUE);
    protected final boolean allowLogin  = RestUtils.checkIfSet("t9t.restapi.login",  Boolean.FALSE);
    protected final boolean allowAnyContentType  = RestUtils.checkIfSet("t9t.restapi.any.content.type",  Boolean.FALSE);
    protected final String allowedUserList = RestUtils.getConfigValue("t9t.restapi.allowedUserList");
    protected final String allowedUserRegexp = RestUtils.getConfigValue("t9t.restapi.allowedUserRegexp");
    protected final Set<String> allowedUserIds = T9tUtil.isBlank(allowedUserList) ? null : Sets.newHashSet(allowedUserList.split(","));
    protected final Pattern allowedUserPattern = T9tUtil.isBlank(allowedUserRegexp) ? null : Pattern.compile(allowedUserRegexp);
    protected final Boolean allowUserPwDefault = Boolean.valueOf(allowedUserIds != null || allowedUserPattern != null);
    protected final boolean allowUserPw = RestUtils.checkIfSet("t9t.restapi.userpw", allowUserPwDefault);
    protected final String allowedApiKeyRegexp = RestUtils.getConfigValue("t9t.restapi.allowedApiKeyRegexp");
    protected final Pattern allowedApiKeyPattern = T9tUtil.isBlank(allowedApiKeyRegexp) ? null : Pattern.compile(allowedApiKeyRegexp);

    protected final Cache<String, Boolean> goodAuths = Caffeine.newBuilder().maximumSize(MAX_AUTH_ENTRIES).expireAfterWrite(5L, TimeUnit.MINUTES)
      .<String, Boolean>build();

    protected final IIPAddressBlocker ipBlockerService = Jdp.getRequired(IIPAddressBlocker.class);
    protected final IGatewayAuthChecker authCheckerService = Jdp.getRequired(IGatewayAuthChecker.class);


    /** Checks if the request came from a blocked IP address. */
    @Override
    public boolean filterBlockedIpAddress(final ContainerRequestContext requestContext, final String remoteIp) {
        if (T9tUtil.isBlank(remoteIp)) {
            // no IP provided, cannot check
            return false;
        }
        if (ipBlockerService.isIpAddressBlocked(remoteIp)) {
            throwForbidden(requestContext);
            return true;
        }
        return false;
    }

    @Override
    public void abortFilter(final ContainerRequestContext requestContext, final Response errorStatus) {
        // throw new WebApplicationException(errorStatus);
        requestContext.abortWith(errorStatus);
        return;
    }

    /** Constructs a new Response object for the current request and throw a WebApplicationException of code 401. */
    protected void throwUnauthorized(final ContainerRequestContext requestContext) {
        abortFilter(requestContext, Response.status(Status.UNAUTHORIZED).build());
    }

    /** Constructs a new Response object for the current request and throw a WebApplicationException of code 403. */
    protected void throwForbidden(final ContainerRequestContext requestContext) {
        abortFilter(requestContext, Response.status(Status.FORBIDDEN).build());
    }

    @Override
    public boolean filterUnauthenticated(final ContainerRequestContext requestContext) {
        final String path = requestContext.getUriInfo().getPath();
        LOGGER.debug("filterUnauthenticated for method {}, type {}, path {}", requestContext.getMethod(), requestContext.getMediaType(), path);
        switch (requestContext.getMethod()) {
        case HttpMethod.POST:
            // allow login, but only if JWT is enabled
            if (!isValidLogin(path, requestContext.getMethod())) {
                throwUnauthorized(requestContext);
                return true;  // filtered
            }
            return false; // OK
        case HttpMethod.GET:
            if (enableSwagger && isValidSwagger(path)) {
                return false; // OK
            }
            if (enablePing && isValidPing(path)) {
                return false; // OK
            }
            if (allowLogin && isValidLogin(path, requestContext.getMethod())) {  // deprecated, currently forbidden by default
                return false; // OK
            }
            throwUnauthorized(requestContext);
            return true;  // filtered
        default:
            throwUnauthorized(requestContext);
            return true;  // filtered
        }
    }

    @Override
    public boolean filterAuthenticated(final ContainerRequestContext requestContext, final String authHeader) {
        // first, check if we know this authentication - use a fast track in that case
        if (authHeader.length() <= MAX_SIZE_AUTH_HEADER && goodAuths.getIfPresent(authHeader) != null) {
            // fast track - we have successfully authenticated this one before
            return false; // OK
        }

        // must have valid authentication - first, check allowed size (depending on method)
        final int firstSpace = authHeader.indexOf(' ');
        if (firstSpace <= 0) {
            throwForbidden(requestContext);
            return true; // filtered
        }
        final String typeOfAuth = authHeader.substring(0, firstSpace + 1);  // add 1 because the constant includes the space
        final String authParam = authHeader.substring(firstSpace + 1);
        final int authLength = authParam.length();

        try {
            switch (typeOfAuth) {
            case T9tConstants.HTTP_AUTH_PREFIX_JWT:
                if (!allowAuthJwt()) {
                    LOGGER.debug("Rejecting JWT authentication attempt (not enabled)");
                    throwForbidden(requestContext);
                    return true; // filtered
                }
                if (authLength < 10 || authLength > 4096) { // || !BASE64_PATTERN.matcher(authParam).matches()) {
                    LOGGER.debug("Invalid JWT - length {}", authLength);
                    throwForbidden(requestContext);
                    return true; // filtered
                }
                return filterJwt(requestContext, authHeader, authParam);
            case T9tConstants.HTTP_AUTH_PREFIX_API_KEY:
                if (!allowAuthApiKey()) {
                    LOGGER.debug("Rejecting API-Key authentication attempt (not enabled)");
                    throwForbidden(requestContext);
                    return true; // filtered
                }
                if (authLength != 36 || !UUID_PATTERN.matcher(authParam).matches()) {
                    LOGGER.debug("Invalid UUID - length {}", authLength);
                    throwForbidden(requestContext);
                    return true; // filtered
                }
                return filterApiKey(requestContext, authHeader, authParam);
            case T9tConstants.HTTP_AUTH_PREFIX_USER_PW:
                if (!allowAuthBasic()) {
                    LOGGER.debug("Rejecting Basic authentication attempt (not enabled)");
                    throwForbidden(requestContext);
                    return true; // filtered
                }
                if (authLength < 8 || authLength > 80 || !BASE64_PATTERN.matcher(authParam).matches()) {
                    LOGGER.debug("Rejecting Basic authentication (length {} or bad pattern)", authLength);
                    throwForbidden(requestContext);
                    return true; // filtered
                }
                return filterBasic(requestContext, authHeader, authParam);
            default:
                LOGGER.debug("Rejecting unknown authentication type {} with param length {}", typeOfAuth, authLength);
                throwForbidden(requestContext);
                return true; // filtered
            }
        } catch (final Throwable e) {
            LOGGER.warn("Caller caused exception: {}", ExceptionUtil.causeChain(e));
        }
        throwForbidden(requestContext);
        return true; // filtered
    }

    protected boolean filterAnyAuth(final ContainerRequestContext requestContext, final String authHeader, final AuthenticationParameters authParams) {
        final boolean isValid = authCheckerService.isValidAuth(authHeader, authParams);
        if (isValid) {
            return false;
        }
        throwForbidden(requestContext);
        return true; // filtered
    }

    /**
     * Check for acceptable Basic authentication.
     * The purpose of this method is to ensure "authentication before parameter validation".
     */
    protected boolean filterBasic(final ContainerRequestContext requestContext, final String authHeader, final String authParam) {
        // decompose basic auth into username / password
        // create User+Password Hash
        final String decoded = new String(Base64.getUrlDecoder().decode(authParam), StandardCharsets.UTF_8);
        final int colonPos = decoded.indexOf(':');
        if (colonPos > 0 && colonPos < decoded.length()) {
            final String userId = decoded.substring(0, colonPos);
            final String password = decoded.substring(colonPos + 1);
            if (filterByUserId(userId)) {
                throwForbidden(requestContext);
                return true;
            }
            final PasswordAuthentication passwordAuthentication = new PasswordAuthentication(userId, password);
            return filterAnyAuth(requestContext, authHeader, passwordAuthentication);
        } else {
            throwForbidden(requestContext);
            return true;
        }
    }

    /**
     * Checks if the specific userId is allowed.
     * By default, either all users are allowed or all users are forbidden.
     * If basic authentication is allowed, but only specific users should be permitted, this filter can be used.
     * The method does not directly perform a request filter abort, because it is also used by the login endpoint /userpw.
     *
     * @param userId  the ID of the user trying to authenticate
     * @return        true if the access is forbidden, else false
     */
    @Override
    public boolean filterByUserId(final String userId) {
        if (T9tUtil.isBlank(userId)) {
            return true;
        }
        if (!allowUserPwDefault) {
            // no specific filter has been defined, but since we are here, global userPw must have been enabled
            return false;
        }
        if (allowedUserIds != null && allowedUserIds.contains(userId)) {
            // userId allowed by specific list
            return false;
        }
        if (allowedUserPattern != null && allowedUserPattern.matcher(userId).matches()) {
            // userId allowed by pattern
            return false;
        }
        LOGGER.warn("Rejecting authentication attempt by user of ID of {} characters because not matching pattern and not in allow-list", userId.length());
        return true;
    }

    /**
     * Check for acceptable API key authentication.
     * The purpose of this method is to ensure "authentication before parameter validation".
     */
    protected boolean filterApiKey(final ContainerRequestContext requestContext, final String authHeader, final String authParam) {
        if (filterByApiKey(authParam)) {
            throwForbidden(requestContext);
            return true;
        }
        final ApiKeyAuthentication apiKeyAuthentication = new ApiKeyAuthentication();
        apiKeyAuthentication.setApiKey(UUID.fromString(authParam));
        return filterAnyAuth(requestContext, authHeader, apiKeyAuthentication);
    }

    /**
     * Checks for acceptable JWT authentication. Throws an exception if this type is not desired.
     * The purpose of this method is to ensure "authentication before parameter validation".
     */
    protected boolean filterJwt(final ContainerRequestContext requestContext, final String authHeader, final String authParam) {
        final JwtAuthentication jwtAuthentication = new JwtAuthentication();
        jwtAuthentication.setEncodedJwt(authParam);
        return filterAnyAuth(requestContext, authHeader, jwtAuthentication);
    }

    @Override
    public boolean allowAuthJwt() {
        return allowLogin;  // JWT authentication only makes sense if logins are allowed
    }

    @Override
    public boolean allowAuthBasic() {
        return allowUserPw; // http basic authentication is not preferred usually (must be enabled via setting)
    }

    @Override
    public boolean allowAuthApiKey() {
        return true;
    }

    /** Invoked for POST requests or GET: Checks for correct login path. */
    protected boolean isValidLogin(final String path, final String method) {
        if (!HttpMethod.POST.equals(method)) {
            return false;  // currently all login requests must be POST requests (can be overridden to allow the deprecated GET /apikey)
        }
        return (allowUserPw && "/userpw".equals(path))
            || (allowApiKey && "/apikey".equals(path));
    }

    /** Invoked for GET requests: Check for valid swagger path. */
    protected boolean isValidSwagger(final String path) {
        return "/openapi.json".equals(path) || path.startsWith("/swagger-ui/");
    }

    /** Invoked for GET requests: Check for allowed GET paths. */
    protected boolean isValidPing(final String path) {
        return "/ping".equals(path);
    }

    @Override
    public boolean filterSupportedMediaType(final ContainerRequestContext requestContext) {
        if (allowAnyContentType) {
            return false;
        }
        final MediaType mediaType = requestContext.getMediaType();
        if (mediaType != null) {
            final String subType = mediaType.getSubtype();
            if (!"application".equals(mediaType.getType()) || !("xml".equals(subType) || "json".equals(subType))) {
                LOGGER.debug("Rejecting request due to unsupported media type {}", mediaType);
                abortFilter(requestContext, Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean filterCorrectIdempotencyPattern(final ContainerRequestContext requestContext, final String idempotencyHeader) {
        if (idempotencyHeader == null) {
            return false; // no filtering applies
        }
        final int idempotencyHeaderLength = idempotencyHeader.length();
        if (idempotencyHeaderLength != 36 || !BASE64_PATTERN.matcher(idempotencyHeader).matches()) {
            LOGGER.error("Invoked with bad HTTP header {} of length {}", T9tConstants.HTTP_HEADER_IDEMPOTENCY_KEY, idempotencyHeaderLength);
            abortFilter(requestContext, Response.status(Status.BAD_GATEWAY).build());
            return true;
        }
        return false;
    }

    @Override
    public boolean filterByApiKey(final String apiKey) {
        if (allowedApiKeyPattern != null && !allowedApiKeyPattern.matcher(apiKey).matches()) {
            LOGGER.warn("Rejecting authentication attempt by API key due to failed API key pattern check");
            return true;
        }
        return false;
    }
}
