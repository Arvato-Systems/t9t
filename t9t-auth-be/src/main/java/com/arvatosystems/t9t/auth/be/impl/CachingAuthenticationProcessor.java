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
package com.arvatosystems.t9t.auth.be.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.arvatosystems.t9t.base.services.SessionInvalidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.jwt.IJWT;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationInfo;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.services.IAuthCacheInvalidation;
import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry;
import com.arvatosystems.t9t.server.services.IAuthenticate;
import com.arvatosystems.t9t.server.services.ICachingAuthenticationProcessor;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class CachingAuthenticationProcessor implements ICachingAuthenticationProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingAuthenticationProcessor.class);
    protected static final Cache<String, AuthenticationInfo> AUTH_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(110L, TimeUnit.SECONDS).maximumSize(200L).build();
    protected static final AuthenticationInfo ACCESS_DENIED_DUE_TO_EXCEPTION = new AuthenticationInfo();
    static {
        ACCESS_DENIED_DUE_TO_EXCEPTION.setHttpStatusCode(403);
        ACCESS_DENIED_DUE_TO_EXCEPTION.setMessage("Invalid JWT");
        ACCESS_DENIED_DUE_TO_EXCEPTION.freeze();
    }
    protected static final AuthenticationInfo ACCESS_DENIED_INVALID_API_KEY = new AuthenticationInfo();
    static {
        ACCESS_DENIED_INVALID_API_KEY.setHttpStatusCode(403);
        ACCESS_DENIED_INVALID_API_KEY.setMessage("Invalid API key");
        ACCESS_DENIED_INVALID_API_KEY.freeze();
    }
    protected static final AuthenticationInfo ACCESS_DENIED_INVALID_BASIC = new AuthenticationInfo();
    static {
        ACCESS_DENIED_INVALID_BASIC.setHttpStatusCode(403);
        ACCESS_DENIED_INVALID_BASIC.setMessage("Invalid basic auth info");
        ACCESS_DENIED_INVALID_BASIC.freeze();
    }
    protected static final AuthenticationInfo HANDS_OFF = new AuthenticationInfo();
    static {
        HANDS_OFF.setHttpStatusCode(403);
        HANDS_OFF.setMessage("Told ya, hands off!");
        HANDS_OFF.freeze();
    }
    protected static final AuthenticationInfo UNSUPPORTED_AUTH_METHOD = new AuthenticationInfo();
    static {
        UNSUPPORTED_AUTH_METHOD.setHttpStatusCode(403);
        UNSUPPORTED_AUTH_METHOD.setMessage("Unsupported authentication method");
        UNSUPPORTED_AUTH_METHOD.freeze();
    }

    protected final IJWT jwt = Jdp.getRequired(IJWT.class);
    protected final IAuthenticate authModule = Jdp.getRequired(IAuthenticate.class);

    public CachingAuthenticationProcessor() {
        final ICacheInvalidationRegistry cacheInvalidationRegistry = Jdp.getOptional(ICacheInvalidationRegistry.class);
        if (cacheInvalidationRegistry != null) {
            cacheInvalidationRegistry.registerInvalidator(IAuthCacheInvalidation.AUTH_CACHE_ID, dto -> AUTH_CACHE.invalidateAll());
        }
    }

    private boolean isOrWasValid(AuthenticationInfo authInfo) {
        return authInfo.getEncodedJwt() != null && authInfo.getJwtInfo() != null;
    }

    private boolean isStillValid(AuthenticationInfo authInfo) {
        final Instant expiresAt = authInfo.getJwtInfo().getExpiresAt();
        return expiresAt == null || expiresAt.isAfter(Instant.now());
    }

    protected AuthenticationInfo storeSuccessful(String header, String encodedJwt, JwtInfo jwtInfo) {
        final AuthenticationInfo authInfo = new AuthenticationInfo();
        authInfo.setEncodedJwt(encodedJwt);
        authInfo.setJwtInfo(jwtInfo);
        AUTH_CACHE.put(header, authInfo);
        return authInfo;
    }

    protected AuthenticationInfo authByJwtAndStoreResult(String header) {
        final String jwtToken = header.substring(7).trim();
        try {
            final JwtInfo info = jwt.decode(jwtToken);
            if (SessionInvalidation.isSessionInvalidated(info)) {
                AUTH_CACHE.put(header, ACCESS_DENIED_DUE_TO_EXCEPTION);
                return ACCESS_DENIED_DUE_TO_EXCEPTION;
            }
            return storeSuccessful(header, jwtToken, info);
        } catch (Exception e) {
            LOGGER.info("JWT rejected: {}: {}", e.getClass().getSimpleName(), e.getMessage());
            AUTH_CACHE.put(header, ACCESS_DENIED_DUE_TO_EXCEPTION);
        }
        return ACCESS_DENIED_DUE_TO_EXCEPTION;
    }

    protected AuthenticationInfo authByApiKeyAndStoreResult(String header) {
        try {
            final UUID uuid = UUID.fromString(header.substring(8).trim());
            final AuthenticationResponse authResp = authModule.login(new AuthenticationRequest(new ApiKeyAuthentication(uuid)));
            if (authResp.getReturnCode() == 0) {
                return storeSuccessful(header, authResp.getEncodedJwt(), authResp.getJwtInfo());
            } else {
                LOGGER.info("Auth by API key rejected: Code {}: {} {}", authResp.getReturnCode(), authResp.getErrorMessage(), authResp.getErrorDetails());
            }
        } catch (Exception e) {
            LOGGER.info("Bad API Key auth: {}: {}", e.getClass().getSimpleName(), e.getMessage());
        }
        AUTH_CACHE.put(header, ACCESS_DENIED_INVALID_API_KEY);
        return ACCESS_DENIED_INVALID_API_KEY;
    }

    protected AuthenticationInfo authByUserPasswordAndStoreResult(String header) {
        try {
            final String decoded = new String(Base64.getUrlDecoder().decode(header.substring(6).trim()), StandardCharsets.UTF_8);
            final int colonPos = decoded.indexOf(':');
            if (colonPos > 0 && colonPos < decoded.length()) {
                final AuthenticationResponse authResp = authModule.login(new AuthenticationRequest(
                  new PasswordAuthentication(decoded.substring(0, colonPos), decoded.substring(colonPos + 1))));
                if (authResp.getReturnCode() == 0) {
                    return storeSuccessful(header, authResp.getEncodedJwt(), authResp.getJwtInfo());
                } else {
                    LOGGER.info("Auth by Basic username / PW rejected: Code {}: {} {}",
                      authResp.getReturnCode(), authResp.getErrorMessage(), authResp.getErrorDetails());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Bad Basic auth: {}: {}", e.getClass().getSimpleName(), e.getMessage());
        }
        AUTH_CACHE.put(header, ACCESS_DENIED_INVALID_BASIC);
        return ACCESS_DENIED_INVALID_BASIC;
    }

    @Override
    public AuthenticationInfo getCachedJwt(String authorizationHeader) {
        // cache test is common for all types of headers
        final AuthenticationInfo cachedUser = AUTH_CACHE.getIfPresent(authorizationHeader);
        if (cachedUser != null) {
            if (isOrWasValid(cachedUser)) {
                if (isStillValid(cachedUser)) {
                    LOGGER.debug("Found cached authentication entry for user {}, method {}",
                      cachedUser.getJwtInfo().getUserId(), authorizationHeader.substring(0, 7));
                    if (SessionInvalidation.isSessionInvalidated(cachedUser.getJwtInfo())) {
                        LOGGER.debug("But session for user {} has been invalidated or logged out", cachedUser.getJwtInfo().getUserId());
                        return ACCESS_DENIED_DUE_TO_EXCEPTION;
                    }
                    return cachedUser;
                } else {
                    LOGGER.debug("Authentication: cached JWT for {} has expired, performing new authentication",
                      cachedUser.getJwtInfo().getUserId());
                    // fall through
                }
            } else {
                // denied! Do not waste time, this may be a DOS attack
                LOGGER.debug("Repeated attempt for declined header {}", authorizationHeader);
                return HANDS_OFF;
            }
        }

        LOGGER.debug("New authentication for {}", authorizationHeader.substring(0, 7));   // do not log the full credentials, just the type
        if (authorizationHeader.startsWith(T9tConstants.HTTP_AUTH_PREFIX_JWT)) {
            return authByJwtAndStoreResult(authorizationHeader);
        }
        if (authorizationHeader.startsWith(T9tConstants.HTTP_AUTH_PREFIX_API_KEY)) {
            return authByApiKeyAndStoreResult(authorizationHeader);
        }
        if (authorizationHeader.startsWith(T9tConstants.HTTP_AUTH_PREFIX_USER_PW)) {
            return authByUserPasswordAndStoreResult(authorizationHeader);
        }

        return UNSUPPORTED_AUTH_METHOD;
    }
}
