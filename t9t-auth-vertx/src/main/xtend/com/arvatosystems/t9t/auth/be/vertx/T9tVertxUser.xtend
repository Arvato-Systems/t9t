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

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User

public class T9tVertxUser implements User {
    private final String jwtToken;          // encoded form, without "Bearer" prefix
    private final JwtInfo info;             // decoded data in map form
    private final JsonObject principal;     // token + decoded map

    public new (String jwtToken, JwtInfo info) {
        this.jwtToken = jwtToken;
        this.info = info
        principal = new JsonObject(#{ "jwt" -> jwtToken, "info" -> info })
    }

    def isOrWasValid() {
        return info !== null && jwtToken !== null
    }
    def isStillValid() {
        info.expiresAt === null || info.expiresAt.isAfterNow
    }
    def getUserId() {
        return info?.userId
    }
    override clearCache() {
    }

    override principal() {
        return principal;
    }

    override isAuthorized(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
        throw new UnsupportedOperationException("TODO: auto-generated method stub")
    }

    override setAuthProvider(AuthProvider authProvider) {
        throw new UnsupportedOperationException("TODO: auto-generated method stub")
    }
}
