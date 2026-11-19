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
package com.arvatosystems.t9t.remote.connect;

import java.util.UUID;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.util.impl.RecordMarshallerBonaparte;
import de.jpaw.bonaparte.util.impl.RecordMarshallerCompactBonaparteIdentity;
import de.jpaw.bonaparte.util.impl.RecordMarshallerJson;

import com.arvatosystems.t9t.authc.api.LogoutRequest;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;

/**
 * Remote connection via http.
 *
 * This class provides a variety of constructors to connect to a remote server via http.
 * The constructors can be separated into 3 groups:
 * - authentication via UUID
 * - authentication via userId / password. These also come with flavours which change password or tenant.
 * - unspecified authentication. Everything is read from system parameters, environment, or file $HOME/t9taccess.properties
 *
 * The connection is specifically targeted for the t9t rpc endpoint.
 **/
public class Connection extends AbstractConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    private static final ConnectionTypes DEFAULT_CONNECTION_TYPE = ConnectionTypes.COMPACT_BONAPARTE;
    private static final boolean DEFAULT_LOG_SIZES = true;
    private static final boolean DEFAULT_LOG_MESSAGES = false;

    // Logical configuration.
    private final String baseUrl;
    private final HttpPostClient dlg;

    @Nonnull
    private static String defaultUrl() {
        final String url = getInitialUrl();
        if (url != null) {
            return url;
        }
        // The URL was not specified directly, so assemble it from the schema, host, and port.
        final String schema = getInitialSchema();
        final String port = getInitialPort();
        final String protocol = T9tUtil.nvl(schema, "443".equals(port) ? "https" : "http");
        return protocol + "://" + getInitialHost() + ":" + port;
    }


    /**
     * Connect via a specific UUID - URL is determined from config.
     *
     * If the provide UUID is null, fall back to userId / password.
     **/
    public Connection(@Nullable final UUID initialUUID) {
        this(defaultUrl(), DEFAULT_LOG_SIZES, DEFAULT_LOG_MESSAGES, DEFAULT_CONNECTION_TYPE, initialUUID, null, null);
    }

    /**
     * Connect via a specific UUID - URL provided directly.
     *
     * If the provide UUID is null, fall back to userId / password.
     **/
    public Connection(@Nonnull final String url, @Nullable final UUID initialUUID) {
        this(url, DEFAULT_LOG_SIZES, DEFAULT_LOG_MESSAGES, DEFAULT_CONNECTION_TYPE, initialUUID, null, null);
    }


    /**
     * Connect via a specified user ID and password - URL is determined from config.
     *
     * Will never use a UUID / API key to connect.
     **/
    public Connection(@Nonnull final String userId, @Nonnull final String password) {
        this(defaultUrl(), DEFAULT_LOG_SIZES, DEFAULT_LOG_MESSAGES, DEFAULT_CONNECTION_TYPE, null, userId, password);
    }

    /**
     * Convenience constructor using the default URL and connection type, and configured credentials.
     **/
    public Connection() {
        this(DEFAULT_LOG_SIZES, DEFAULT_LOG_MESSAGES);
    }

    /**
     * Convenience constructor using the default URL and connection type, and configured credentials.
     * Allows to override the logging behaviour.
     * When false, log less than the no-argument constructor; when true, log more.
     **/
    public Connection(final boolean differentLog) {
        this(differentLog, differentLog);
    }

//    /** When false, log less than the no-argument constructor; when true, log more. */
//    public Connection(final boolean differentLog, @Nullable final ConnectionTypes connectionType) {
//        this(differentLog, differentLog, connectionType);
//    }

    /**
     * Common subconstructor for all connections using default credentials.
     **/
    private Connection(final boolean logSizes, final boolean logMessage) {
        this(defaultUrl(), logSizes, logMessage, DEFAULT_CONNECTION_TYPE, getInitialApiKey(), null, null);
    }

    protected Connection(@Nonnull final String url, final boolean logSizes, final boolean logMessage, @Nullable final ConnectionTypes connectionType,
            @Nullable final UUID initialUUID, @Nullable final String userId, @Nullable final String password) {
        LOGGER.info("Connecting to {}", url);
        baseUrl = url;

        if (connectionType == null) {
            throw new IllegalArgumentException("format NULL not supported by this client");
        }
        switch (connectionType) {
        case BONAPARTE:
            dlg = new HttpPostClient(url, false, logSizes, logMessage, false, new RecordMarshallerBonaparte());
            break;
        case COMPACT_BONAPARTE:
            dlg = new HttpPostClient(url, false, logSizes, false, logMessage, new RecordMarshallerCompactBonaparteIdentity());
            break;
        case JSON:
            dlg = new HttpPostClient(url, false, logSizes, logMessage, false, new RecordMarshallerJson());
            break;
        default:
            throw new IllegalArgumentException("format " + connectionType.name() + " not supported by this client");
        }

        // Authenticate and obtain the token.
        dlg.setBaseUrl(baseUrl + "/login");

        final AuthenticationResponse authResult;
        // if an API key is provided, use it; otherwise, use userId / password. If both are null, use the default credentials.
        if (initialUUID != null) {
            final UUID apiKey = initialUUID;
            authResult = auth(apiKey);
        } else if (userId != null && password != null) {
            authResult = auth(userId, password);
        } else {
            authResult = auth(getInitialUserId(), getInitialPassword());
        }

        dlg.setAuthentication(T9tConstants.HTTP_AUTH_PREFIX_JWT + authResult.getEncodedJwt()); // the encoded token
        dlg.setBaseUrl(baseUrl + "/rpc");
    }

    @Override
    public void switchUser(@Nullable final UUID newApiKey) {
        dlg.setBaseUrl(baseUrl + "/login");
        final AuthenticationResponse authResult = newApiKey == null
                ? auth(getInitialUserId(), getInitialPassword())
                : auth(newApiKey);
        dlg.setAuthentication(T9tConstants.HTTP_AUTH_PREFIX_JWT + authResult.getEncodedJwt()); // the encoded token
        dlg.setBaseUrl(baseUrl + "/rpc");
    }

    @Override
    public void switchUser(@Nonnull final String userId, @Nonnull final String password) {
        dlg.setBaseUrl(baseUrl + "/login");
        final AuthenticationResponse authResult = auth(userId, password);
        dlg.setAuthentication(T9tConstants.HTTP_AUTH_PREFIX_JWT + authResult.getEncodedJwt());
        dlg.setBaseUrl(baseUrl + "/rpc");
    }

    // Just a proxy.
    @Override
    @Nullable
    public BonaPortable doIO(@Nonnull final BonaPortable rp) {
        try {
            return dlg.doIO(rp);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logout() {
        LOGGER.info("Logging out");
        dlg.setBaseUrl(baseUrl + "/logout");
        try {
            dlg.doIO(new LogoutRequest());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    // This method supports remote tests of incorrect authentications.
    @Override
    public void setAuthentication(@Nullable final String header) {
        dlg.setAuthentication(header);
    }

    public void setBaseUrl(@Nonnull final String postPath) {
        dlg.setBaseUrl(baseUrl + postPath);
    }
}
