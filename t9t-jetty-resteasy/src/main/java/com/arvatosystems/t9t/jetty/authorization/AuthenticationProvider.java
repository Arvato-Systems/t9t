/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.jetty.authorization;

import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * Performs a login from the AuthFilter and converts it into a JWT. Also caches
 * JWTs for basicAuth Strings and ApiKey-UUIDs.
 *
 * @author LUEC034
 */
@Singleton
public class AuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationProvider.class);

    /**
     * Caches the basicAuth String in form of the Hash and connects it to a JWT.
     * This cache must expire significantly faster than the JWT duration (max 1/2 of it).
     */
    private static final Cache<String, String> basicAuthHashToJwtCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS).build();
    /**
     * Caches the ApiKey and connects it to a JWT.
     * This cache must expire significantly faster than the JWT duration (max 1/2 of it).
     */
    private static final Cache<UUID, String> apiKeyToJwtCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS).build();

    protected final IRemoteConnection connection = Jdp.getRequired(IRemoteConnection.class);

    /**
     * First checks a cache for the Basic Auth String. If it exist a JWT will be
     * returned. Otherwise an Authenticaton is performed. After a succesful
     * authentication the JWT will be cached.
     *
     * @param basicAuth
     * @return JWT
     */
    public String getJwtFromBasicAuth(String basicAuth) {
        if (basicAuth == null)
            throw new T9tException(T9tException.ACCESS_DENIED);  // just in case... to avoid visible stack trace from guava cache
        LOGGER.debug("Attempting to retrieve JWT for basic authenticaton.");
        String jwt = basicAuthHashToJwtCache.getIfPresent(basicAuth);
        if (jwt != null) {
            LOGGER.debug("JWT already cached. Returning...");
            return jwt;
        }

        String decodedBase64Auth = "";
        try {
            decodedBase64Auth = new String(Base64.getDecoder().decode(basicAuth));
        } catch (Exception e) {
            throw new T9tException(T9tException.ACCESS_DENIED); // basicAuth is not base64 encoded or corrupted
        }
        String[] userAndPassword = decodedBase64Auth.split(":");

        // create AuthenticationParamsRequest
        AuthenticationRequest authenticationParamsRequest = new AuthenticationRequest();
        // create User+Password Hash
        PasswordAuthentication passwordAuthentication = new PasswordAuthentication();
        passwordAuthentication.setUserId(userAndPassword[0]);
        passwordAuthentication.setPassword(userAndPassword[1]);
        authenticationParamsRequest.setAuthenticationParameters(passwordAuthentication);
        // execute ServiceRequest
        ServiceResponse serviceResponse = connection.executeAuthenticationRequest(authenticationParamsRequest);
        if (serviceResponse instanceof AuthenticationResponse) {
            AuthenticationResponse authenticationResponse = (AuthenticationResponse) serviceResponse;
            jwt = authenticationResponse.getEncodedJwt();
        } else {
            throw new T9tException(serviceResponse.getReturnCode());
        }
        if (jwt != null && !jwt.isEmpty()) {
            LOGGER.debug("JWT cached for future use.");
            basicAuthHashToJwtCache.put(basicAuth, jwt); // set it in cache
            return jwt;
        } else {
            throw new T9tException(T9tException.ACCESS_DENIED);
        }
    }
    /**
     * Performs an authentication with an ApiKey. The ApiKey will be cached and connected to the JWT when Authentication was succesful.
     *
     * @param apiKey
     * @return
     * @throws T9tException
     */
    public String getJwtFromApiKey(UUID apiKey) {
        if (apiKey == null)
            throw new T9tException(T9tException.ACCESS_DENIED);  // can occur in case of malformatted API keys
        String jwt = apiKeyToJwtCache.getIfPresent(apiKey);
        if (jwt != null) {
            LOGGER.debug("JWT already cached for ApiKey.");
            return jwt;
        }
        AuthenticationRequest authenticationParamsRequest = new AuthenticationRequest();
        authenticationParamsRequest.setAuthenticationParameters(new ApiKeyAuthentication(apiKey));
        // execute ServiceRequest
        ServiceResponse serviceResponse = connection.executeAuthenticationRequest(authenticationParamsRequest);
        if (serviceResponse.getReturnCode() == 0 && serviceResponse instanceof AuthenticationResponse) {
            AuthenticationResponse authenticationResponse = (AuthenticationResponse) serviceResponse;
            jwt = authenticationResponse.getEncodedJwt();
        } else {
            throw new T9tException(T9tException.ACCESS_DENIED);
        }
        if (jwt != null && !jwt.isEmpty()) {
            apiKeyToJwtCache.put(apiKey, jwt); // connect jwt to ApiKey
            LOGGER.debug("Setting JWT in cache for ApiKey.");
            return jwt;
        } else {
            throw new T9tException(T9tException.ACCESS_DENIED);
        }
    }
}
