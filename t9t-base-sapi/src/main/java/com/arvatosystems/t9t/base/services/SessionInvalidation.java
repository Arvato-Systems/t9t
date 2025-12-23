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
package com.arvatosystems.t9t.base.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SessionInvalidation {
    private SessionInvalidation() { }

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionInvalidation.class);
    private static final long CACHE_EXPIRY_HOURS = 12L;

    /**
     * Keeps track of invalidated sessions. The key is the sessionRef.
     * This cache is used to track logouts for specific sessions.
     */
    private static final Cache<Long, Instant> SESSION_LOGGED_OUT_AT = Caffeine.newBuilder().expireAfterWrite(CACHE_EXPIRY_HOURS, TimeUnit.HOURS).build();

    /**
     * Keeps track of invalidated sessions per API Key. API Key deactivation invalidates all sessions.
     * All JWTs issued before the recorded Instant are considered invalid.  The key is the objectRef of {@link ApiKeyDTO}.
     */
    private static final Cache<Long, Instant> API_KEY_SESSION_INVALID = Caffeine.newBuilder().expireAfterWrite(CACHE_EXPIRY_HOURS, TimeUnit.HOURS).build();

    /**
     * Keeps track of invalidated sessions per user. A password change does invalidate all sessions for a given user.
     * All JWTs issued before the recorded Instant are considered invalid.  The key is the userId.
     */
    private static final Cache<String, Instant> USER_SESSION_INVALID = Caffeine.newBuilder().expireAfterWrite(CACHE_EXPIRY_HOURS, TimeUnit.HOURS).build();

    /**
     * Session is considered invalidated if following conditions are met:
     * - if the sessionRef of the given JWT has a logout time before current time
     * - if the userId of the session entry has an invalidation time and the issuedAt time of the JWT is before that time
     *
     * @param jwtInfo the {@link JwtInfo} to check
     * @return true if session is invalidated else false
     */
    public static boolean isSessionInvalidated(@Nonnull final JwtInfo jwtInfo) {
        if (jwtInfo.getSessionRef() != null) {
            // check if specific session is logged out
            final Instant instant = SESSION_LOGGED_OUT_AT.getIfPresent(jwtInfo.getSessionRef());
            if (instant != null && !Instant.now().isBefore(instant)) {
                LOGGER.info("JWT session for user {} is already logged out", jwtInfo.getUserId());
                return true;
            }
        }
        if (jwtInfo.getUserId() != null) {
            // check if old JWTs of a user is invalidated due to password change
            final Instant instant = USER_SESSION_INVALID.getIfPresent(jwtInfo.getUserId());
            if (instant != null && jwtInfo.getIssuedAt() != null && jwtInfo.getIssuedAt().isBefore(instant)) {
                LOGGER.info("JWT for user {} is invalidated", jwtInfo.getUserId());
                return true;
            }
        }
        final Long apiKeyRef = JsonUtil.getZLong(jwtInfo.getZ(), T9tConstants.API_KEY_JWT_KEY, null);
        if (apiKeyRef != null) {
            // check if old JWTs of an API key is invalidated due to API key deactivation
            final Instant instant = API_KEY_SESSION_INVALID.getIfPresent(apiKeyRef);
            if (instant != null && jwtInfo.getIssuedAt() != null && jwtInfo.getIssuedAt().isBefore(instant)) {
                LOGGER.info("JWT for API key {} is invalidated", apiKeyRef);
                return true;
            }
        }
        return false;
    }

    /**
     * Marks the session as logged out at the given instant.
     *
     * @param sessionRef the session reference
     * @param instant the instant when the session was logged out
     */
    public static void logoutSession(@Nonnull final Long sessionRef, @Nonnull final Instant instant) {
        LOGGER.debug("Invalidate all JWTs for session {} logged out at {}", sessionRef, instant);
        SESSION_LOGGED_OUT_AT.put(sessionRef, instant);
    }

    /**
     * Marks all sessions of the given user as invalidated at the given instant.
     *
     * @param userId the user ID
     * @param instant the instant when the sessions were invalidated
     */
    public static void invalidateUserSessions(@Nonnull final String userId, @Nonnull final Instant instant) {
        // add some buffer to avoid problems with change password requests. Because change password is done together with authentication.
        final Instant invalidateAt = instant.truncatedTo(ChronoUnit.SECONDS).minusSeconds(3);
        LOGGER.debug("Invalidate all JWTs for user {} issued before {}", userId, invalidateAt);
        USER_SESSION_INVALID.put(userId, invalidateAt);
    }

    /**
     * Removes the user session invalidation entry for the given user ID.
     *
     * @param userId the user ID
     */
    public static void removeUserSessionInvalidation(@Nonnull final String userId) {
        LOGGER.debug("Remove user session invalidation for user {}", userId);
        USER_SESSION_INVALID.invalidate(userId);
    }

    /**
     * Marks all sessions of the given API key as invalidated at the given instant.
     *
     * @param apiKeyRef the API key objectRef
     * @param instant the instant when the sessions were invalidated
     */
    public static void invalidateApiKeySession(final long apiKeyRef, @Nonnull final Instant instant) {
        LOGGER.debug("Invalidate all JWTs for API key {} issued before {}", apiKeyRef, instant);
        API_KEY_SESSION_INVALID.put(apiKeyRef, instant);
    }

    /**
     * Removes the API key invalidation entry for the given API key ID.
     *
     * @param apiKeyRef the API key objectRef
     */
    public static void removeApiKeyInvalidation(final long apiKeyRef) {
        LOGGER.debug("Remove API key session invalidation for API key {}", apiKeyRef);
        API_KEY_SESSION_INVALID.invalidate(apiKeyRef);
    }
}
