/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.jwt.IJWT;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.auth.JwtAuthentication;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.server.services.IAuthenticate;
import com.arvatosystems.t9t.server.services.IRequestProcessor;
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

//process ServiceRequests from unauthenticated sources. Blocking operation
@Singleton
public class ServiceRequestExecutor implements IUnauthenticatedServiceRequestExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRequestExecutor.class);

    protected final IAuthenticate authModule = Jdp.getRequired(IAuthenticate.class);
    protected final IRequestProcessor requestProcessor = Jdp.getRequired(IRequestProcessor.class);
    protected final IJWT jwt = Jdp.getRequired(IJWT.class);

    protected static final AuthData ACCESS_DENIED = new AuthData(null, null);
    protected static final Cache<AuthenticationParameters, AuthData> AUTH_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(110L, TimeUnit.SECONDS).maximumSize(500L).build();

    @Override
    public ServiceResponse execute(final ServiceRequest srq) {
        return execute(srq, false);
    }

    @Override
    public ServiceResponse executeTrusted(final ServiceRequest srq) {
        return execute(srq, true);
    }

    protected ServiceResponse execute(final ServiceRequest srq, final boolean isTrusted) {
        final AuthenticationParameters ap = srq.getAuthentication();
        if (ap == null) {
            throw new T9tException(T9tException.NOT_AUTHENTICATED);
        }
        AuthData adata = AUTH_CACHE.getIfPresent(ap);
        if (adata != null && adata.getJwtInfo() != null && adata.getJwtInfo().getExpiresAt().isBefore(Instant.now())) {
            adata = null; // force reauth due to expiry of previous JWT
        }
        if (adata == null) {
            // need to compute
            if (ap instanceof JwtAuthentication jwtAp) {
                // fast track
                final String jwtToken = jwtAp.getEncodedJwt();
                try {
                    adata = new AuthData(jwtToken, jwt.decode(jwtToken));
                } catch (Exception e) {
                    LOGGER.info("JWT rejected: {}: {}", e.getClass().getSimpleName(), e.getMessage());
                }
            } else {
                // service: authenticate!
                LOGGER.info("no cached authentication information, trying to authenticate now...");
                try {
                    final AuthenticationResponse authResp = authModule.login(new AuthenticationRequest(ap));
                    if (authResp.getReturnCode() == 0) {
                        adata = new AuthData(authResp.getEncodedJwt(), authResp.getJwtInfo());
                    } else {
                        LOGGER.info("Auth rejected: Code {}: {} {}", authResp.getReturnCode(), authResp.getErrorMessage(), authResp.getErrorDetails());
                    }
                } catch (Exception e) {
                    LOGGER.info("Bad auth: {}: {}", e.getClass().getSimpleName(), e.getMessage());
                }
            }
            AUTH_CACHE.put(ap, adata == null ? ACCESS_DENIED : adata);
        }
        if (adata == ACCESS_DENIED) {
            LOGGER.debug("Rejected auth (cached!)");
            final ServiceResponse sr = new ServiceResponse();
            sr.setReturnCode(T9tException.NOT_AUTHENTICATED);
            sr.setErrorDetails("cached");
            sr.setErrorMessage(ApplicationException.codeToString(sr.getReturnCode()));

            return sr;
        }
        return requestProcessor.execute(srq.getRequestHeader(), srq.getRequestParameters(), adata.getJwtInfo(), adata.getJwtToken(),
            isTrusted, srq.getPartitionUsed());
    }
}
