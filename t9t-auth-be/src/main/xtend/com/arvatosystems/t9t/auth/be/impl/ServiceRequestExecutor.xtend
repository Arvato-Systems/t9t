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
package com.arvatosystems.t9t.auth.be.impl

import com.arvatosystems.t9t.auth.jwt.IJWT
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.api.ServiceRequest
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.types.AuthenticationJwt
import com.arvatosystems.t9t.base.types.AuthenticationParameters
import com.arvatosystems.t9t.server.services.IAuthenticate
import com.arvatosystems.t9t.server.services.IRequestProcessor
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import de.jpaw.util.ApplicationException
import java.util.concurrent.TimeUnit
import org.eclipse.xtend.lib.annotations.Data

// process ServiceRequests from unauthenticated sources. Blocking operation
@Singleton
@AddLogger
class ServiceRequestExecutor implements IUnauthenticatedServiceRequestExecutor {
    @Inject IAuthenticate       authModule;
    @Inject IRequestProcessor   requestProcessor
    @Inject IJWT                jwt

    @Data
    static class AuthData {
        String  jwtToken;          // encoded form, without "Bearer" prefix
        JwtInfo jwtInfo;             // decoded data in map form

        def isValid() {
            return jwtToken !== null
        }
    }

    protected static final AuthData ACCESS_DENIED = new AuthData(null, null)
    protected static final Cache<AuthenticationParameters,AuthData> authCache = CacheBuilder.newBuilder.expireAfterWrite(50L, TimeUnit.MINUTES).maximumSize(500L).build

    override ServiceResponse execute(ServiceRequest srq) {
        return execute(srq, false)
    }

    override ServiceResponse executeTrusted(ServiceRequest srq) {
        return execute(srq, true)
    }

    def protected ServiceResponse execute(ServiceRequest srq, boolean isTrusted) {
        val ap = srq.authentication
        if (ap === null) {
            throw new T9tException(T9tException.NOT_AUTHENTICATED);
        }
        var AuthData adata = authCache.getIfPresent(ap)
        if (adata !== null && !(ap instanceof AuthenticationJwt) && adata.jwtInfo.expiresAt.isAfterNow) {
            adata = null  // force reauth due to expiry
        }
        if (adata === null) {
            // need to compute
            if (ap instanceof AuthenticationJwt) {
                // fast track
                val jwtToken = ap.encodedJwt
                try {
                    adata = new AuthData(jwtToken, jwt.decode(jwtToken))
                } catch (Exception e) {
                    LOGGER.info("JWT rejected: {}: {}", e.class.simpleName, e.message)
                }
            } else {
                // service: authenticate!
                LOGGER.info("no cached authentication information, trying to authenticate now...")
                try {
                    val authResp = authModule.login(new AuthenticationRequest(ap))
                    if (authResp.returnCode == 0) {
                        adata = new AuthData(authResp.encodedJwt, authResp.jwtInfo)
                    } else {
                        LOGGER.info("Auth rejected: Code {}: {} {}", authResp.returnCode, authResp.errorMessage, authResp.errorDetails)
                    }
                } catch (Exception e) {
                    LOGGER.info("Bad auth: {}: {}", e.class.simpleName, e.message)
                }
            }
            authCache.put(ap, adata ?: ACCESS_DENIED)
        }
        if (adata == ACCESS_DENIED) {
            LOGGER.debug("Rejected auth (cached!)")
            return new ServiceResponse => [
                returnCode      = T9tException.NOT_AUTHENTICATED
                errorDetails    = "cached"
                errorMessage    = ApplicationException.codeToString(returnCode)
            ]
        } else {
            return requestProcessor.execute(srq.requestHeader, srq.requestParameters, adata.jwtInfo, adata.jwtToken, isTrusted)
        }
    }
}
