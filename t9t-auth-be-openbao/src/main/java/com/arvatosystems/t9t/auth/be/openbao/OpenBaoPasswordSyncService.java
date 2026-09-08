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
package com.arvatosystems.t9t.auth.be.openbao;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.auth.services.IPasswordSyncService;
import com.arvatosystems.t9t.auth.services.PasswordSyncStatus;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.UplinkConfiguration;

/**
 * OpenBao KV v2 implementation of {@link IPasswordSyncService}.
 *
 * <p>Configuration is read lazily from {@code UplinkConfiguration} using the key {@link #OPENBAO_UPLINK_KEY}.
 * The following fields are used:</p>
 * <ul>
 *   <li>{@code url} – OpenBao base URL (e.g. {@code http://openbao-server:8200})</li>
 *   <li>{@code token} – OpenBao authentication token (sent as {@code X-Vault-Token} header)</li>
 *   <li>{@code clientId} – KV v2 mount path (e.g. {@code secret} or {@code kv})</li>
 *   <li>{@code extraParam} – Optional OpenBao namespace (sent as {@code X-Vault-Namespace} header)</li>
 * </ul>
 *
 * <p>Secret path convention: {@code {clientId}/data/{tenantId}/{userId}}</p>
 */
@Singleton
public class OpenBaoPasswordSyncService implements IPasswordSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenBaoPasswordSyncService.class);

    /** The key in {@code UplinkConfiguration} that identifies the OpenBao configuration entry. */
    public static final String OPENBAO_UPLINK_KEY = "openbao";

    private static final int HTTP_OK_MIN = 200;
    private static final int HTTP_OK_MAX = 299;
    private static final int HTTP_NOT_FOUND = 404;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int DEFAULT_TOKEN_VALIDITY = 600; // seconds

    // lazily initialized fields
    private volatile boolean initialized = false;
    private volatile boolean configured = false;
    private String baseUrl;
    private String authUrl;
    private String token;
    private LocalDateTime tokenFetchedAt;
    private HttpClient httpClient;

    private String getToken() {
        final UplinkConfiguration config = ConfigProvider.getUplink(OPENBAO_UPLINK_KEY);
        final int validity = config.getExtraParam() != null ? Integer.parseInt(config.getExtraParam()) : DEFAULT_TOKEN_VALIDITY;

        if (tokenFetchedAt == null) {
            // not fetched a token yet, login and fetch first token
            final String newToken = loginWithAppRole(config.getClientId(), config.getBasicAuth());
            if (newToken != null) {
                token = newToken;
                tokenFetchedAt = LocalDateTime.now();
            }
        } else if (tokenFetchedAt.plusSeconds(validity).isBefore(LocalDateTime.now())) {
            // token is expired: renew it, falling back to AppRole login if renewal fails
            String newToken = renewToken();
            if (newToken == null) {
                newToken = loginWithAppRole(config.getClientId(), config.getBasicAuth());
            }
            if (newToken != null) {
                token = newToken;
                tokenFetchedAt = LocalDateTime.now();
            }
        }

        return token;
    }

    private String loginWithAppRole(final String roleId, final String secretId) {
        final String body = buildLoginBody(roleId, secretId);
        try {
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(authUrl + "/auth/approle/login")).timeout(DEFAULT_TIMEOUT)
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            final int status = response.statusCode();
            if (status >= HTTP_OK_MIN && status <= HTTP_OK_MAX) {
                final JsonNode json = OBJECT_MAPPER.readTree(response.body());
                return json.path("auth").path("client_token").asText();
            } else {
                LOGGER.error("OpenBao login returned HTTP {}: {}", status, response.body());
                return null;
            }
        } catch (final Exception e) {
            LOGGER.error("OpenBao login failed with exception: {}", e.getMessage());
            return null;
        }
    }

    private String renewToken() {
        try {
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(authUrl + "/auth/token/renew-self")).timeout(DEFAULT_TIMEOUT)
                .header("Content-Type", "application/json").header("X-Vault-Token", token)
                .POST(HttpRequest.BodyPublishers.ofString("{}", StandardCharsets.UTF_8)).build();

            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            final int status = response.statusCode();
            if (status >= HTTP_OK_MIN && status <= HTTP_OK_MAX) {
                final JsonNode json = OBJECT_MAPPER.readTree(response.body());
                return json.path("auth").path("client_token").asText();
            } else {
                LOGGER.error("OpenBao login returned HTTP {}: {}", status, response.body());
                return null;
            }
        } catch (final Exception e) {
            LOGGER.error("OpenBao login failed with exception: {}", e.getMessage());
            return null;
        }
    }

    private synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }
        initialized = true;
        final UplinkConfiguration config = ConfigProvider.getUplink(OPENBAO_UPLINK_KEY);
        if (config == null || config.getUrl() == null || config.getBasicAuth() == null || config.getClientId() == null) {
            LOGGER.debug("No OpenBao configuration found for key '{}' – password sync is disabled.", OPENBAO_UPLINK_KEY);
            configured = false;
            return;
        }
        baseUrl = config.getUrl();
        authUrl = config.getIdentityServiceUrl();
        httpClient = HttpClient.newBuilder()
            .connectTimeout(DEFAULT_TIMEOUT)
            .build();
        token = getToken();
        if (token == null) {
            LOGGER.error("Failed to fetch OpenBao token – password sync is disabled.");
            configured = false;
            return;
        }
        configured = true;
        LOGGER.debug("OpenBao password sync configured: url={}", baseUrl);
    }

    @Override
    public PasswordSyncStatus storePassword(final String userId, final String tenantId, final String password) {
        ensureInitialized();
        if (!configured) {
            return PasswordSyncStatus.DISABLED;
        }
        final String path = buildPath(tenantId, userId);
        final String body = buildStoreBody(userId, password);
        try {
            final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(DEFAULT_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("X-Vault-Token", getToken())
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            final HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            final int status = response.statusCode();
            if (status >= HTTP_OK_MIN && status <= HTTP_OK_MAX) {
                LOGGER.debug("Successfully stored password for user {}/{} in OpenBao (HTTP {})", tenantId, userId, status);
                return PasswordSyncStatus.SUCCESS;
            } else {
                LOGGER.error("OpenBao storePassword for user {}/{} returned HTTP {}: {}", tenantId, userId, status, response.body());
                return PasswordSyncStatus.ERROR;
            }
        } catch (final Exception e) {
            LOGGER.error("OpenBao storePassword for user {}/{} failed with exception: {}", tenantId, userId, e.getMessage());
            return PasswordSyncStatus.ERROR;
        }
    }

    @Override
    public PasswordSyncStatus deletePassword(final String userId, final String tenantId) {
        ensureInitialized();
        if (!configured) {
            return PasswordSyncStatus.DISABLED;
        }
        final String path = buildPath(tenantId, userId);
        try {
            final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(DEFAULT_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("X-Vault-Token", getToken())
                .DELETE();
            final HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            final int status = response.statusCode();
            if ((status >= HTTP_OK_MIN && status <= HTTP_OK_MAX) || status == HTTP_NOT_FOUND) {
                // 404 is acceptable for delete – the secret may not exist
                LOGGER.debug("Successfully deleted (or not found) password for user {}/{} in OpenBao (HTTP {})", tenantId, userId, status);
                return PasswordSyncStatus.SUCCESS;
            } else {
                LOGGER.error("OpenBao deletePassword for user {}/{} returned HTTP {}: {}", tenantId, userId, status, response.body());
                return PasswordSyncStatus.ERROR;
            }
        } catch (final Exception e) {
            LOGGER.error("OpenBao deletePassword for user {}/{} failed with exception: {}", tenantId, userId, e.getMessage(), e);
            return PasswordSyncStatus.ERROR;
        }
    }

    private String buildStoreBody(final String userId, final String password) {
        final ObjectNode data = OBJECT_MAPPER.createObjectNode()
            .put("userName", userId)
            .put("password", password);
        return OBJECT_MAPPER.createObjectNode().set("data", data).toString();
    }

    private String buildLoginBody(final String roleId, final String secretId) {
        return OBJECT_MAPPER.createObjectNode().put("role_id", roleId).put("secret_id", secretId).toString();
    }

    private String buildPath(final String tenantId, final String userId) {
        return "/" + encodePathSegment(tenantId) + "/" + encodePathSegment(userId);
    }

    private String encodePathSegment(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
