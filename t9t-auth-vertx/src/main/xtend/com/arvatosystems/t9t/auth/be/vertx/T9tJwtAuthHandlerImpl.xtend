/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.be.vertx

import com.arvatosystems.t9t.auth.jwt.IJWT
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.dp.Inject
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import java.util.concurrent.TimeUnit

import static io.vertx.core.http.HttpHeaders.*

// BonaparteJwtAuthHandler does not implement an AuthHandler, because that one requires a SessionHandler!
@AddLogger
class T9tJwtAuthHandlerImpl implements Handler<RoutingContext> {
    protected static final T9tVertxUser ACCESS_DENIED = new T9tVertxUser(null, null)
    protected static final Cache<String,T9tVertxUser> authCache = CacheBuilder.newBuilder.expireAfterWrite(50L, TimeUnit.MINUTES).maximumSize(200L).build

    @Inject IJWT jwt

    def public String sign(JwtInfo claims, Long expiresInSeconds, String algorithm) {
        return jwt.sign(claims, expiresInSeconds, algorithm)
    }

    def protected authByJwtAndStoreResult(String header) {
        val jwtToken = header.substring(7).trim
        try {
            val info = jwt.decode(jwtToken)
            val user = new T9tVertxUser(jwtToken, info)
            authCache.put(header, user)
            return user
        } catch (Exception e) {
            LOGGER.info("JWT rejected: {}: {}", e.class.simpleName, e.message)
        }
        authCache.put(header, ACCESS_DENIED)
        return ACCESS_DENIED
    }


    // overrideable auth methods. null means no supported format, true = authenticated, false = rejected
    def protected void authenticate(RoutingContext ctx, String authorizationHeader) {
        LOGGER.debug("unsupported authentication method");
        ctx.response.statusMessage = "unsupported authentication method"
        ctx.fail(403)
    }

    override handle(RoutingContext ctx) {
        if (ctx.user !== null) {
            // probably a session handler has been used
            LOGGER.debug("user exists; seems to be authenticated already")
            ctx.next
            return
        }
        val authorizationHeader = ctx.request.headers.get(AUTHORIZATION)
        if (authorizationHeader === null || authorizationHeader.length < 8) {
            ctx.response.statusMessage = "No or too short Authorization http Header"
            ctx.fail(401)
            return
        }
        // cache test is common for all types of headers
        val cachedUser = authCache.getIfPresent(authorizationHeader)
        if (cachedUser !== null) {
            if (cachedUser.isOrWasValid) {
                if (cachedUser.isStillValid) {
                    LOGGER.debug("Found cached authentication entry for user {}, method {}", cachedUser.userId, authorizationHeader.substring(0, 7))
                    ctx.user = cachedUser
                    ctx.next
                    return
                } else {
                    LOGGER.debug("Authentication: cached JWT for {} has expired, performing new authentication", cachedUser.userId)
                    // fall through
                }
            } else {
                // denied! Do not waste time, this may be a DOS attack
                ctx.response.statusMessage = "Told ya, hands off!"
                ctx.fail(403)
                return
            }
        }

        LOGGER.debug("New authentication for {}", authorizationHeader.substring(0, 7))   // do not log the full credentials, just the type
        if (authorizationHeader.startsWith("Bearer ")) {
            val user = authByJwtAndStoreResult(authorizationHeader)
            if (user.isOrWasValid) {
                ctx.user = user
                ctx.next
                return
            } else {
                ctx.response.statusMessage = "Invalid JWT"
                ctx.fail(403)
                return
            }
        }
        ctx.authenticate(authorizationHeader)
    }
}
