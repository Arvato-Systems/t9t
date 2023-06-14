/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.execution;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.base.types.SessionParameters;
import com.arvatosystems.t9t.server.services.IAuthenticate;
import com.arvatosystems.t9t.server.services.IRequestProcessor;
import com.arvatosystems.t9t.server.services.IStatefulServiceSession;

import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a stateful session. It connects to a stateless backend.
 * The class is not multithreading-capable, except for execute requests - a separate client is required per session.
 */
@Dependent
public class StatefulServiceSession implements IStatefulServiceSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatefulServiceSession.class);

    private final AtomicReference<AuthInfo> authInfo = new AtomicReference<>(null);
    private final IRequestProcessor processor = Jdp.getRequired(IRequestProcessor.class);
    private final IAuthenticate authBackend = Jdp.getRequired(IAuthenticate.class);

    @Override
    public void open(final SessionParameters sessionParameters, final AuthenticationParameters authenticationParameters) {
        authenticationParameters.freeze();
        final AuthInfo info = authenticate(sessionParameters, authenticationParameters);
        authInfo.set(info);
        if (info == null) {
            throw new T9tException(T9tException.NOT_AUTHENTICATED);
        }
    }

    @Override
    public boolean isOpen() {
        return authInfo.get() != null;
    }

    @Override
    public Instant authenticatedUntil() {
        if (authInfo.get() != null && authInfo.get().getJwtInfo() != null) {
            return authInfo.get().getJwtInfo().getExpiresAt();
        }
        return null;
    }

    @Override
    public String getTenantId() {
        if (authInfo.get() != null && authInfo.get().getJwtInfo() != null) {
            return authInfo.get().getJwtInfo().getTenantId();
        }
        return null;
    }

    @Override
    public ServiceResponse execute(final RequestParameters request) {
        // regular request: JWT required!
        final AuthInfo info = authInfo.get();
        if (info == null) {
            throw new T9tException(T9tException.NOT_AUTHENTICATED);
        }
        // OK, authenticated!
        final ServiceResponse resp = processor.execute(null, request, info.getJwtInfo(), info.getEncodedJwt(), false, null);
        if (resp.getReturnCode() == 0) {
            if (resp instanceof AuthenticationResponse authResp) {
                // update JWT
                if (request instanceof AuthenticationRequest authRequest) {
                    authInfo.set(new AuthInfo(authRequest.getSessionParameters(), authRequest.getAuthenticationParameters(), authResp.getJwtInfo(),
                            authResp.getEncodedJwt()));
                } else {
                    authInfo.set(
                            new AuthInfo(info.getSessionParameters(), info.getAuthenticationParameters(), authResp.getJwtInfo(), authResp.getEncodedJwt()));
                }
            }
        } else {
            // any issue
            if (resp.getReturnCode() == T9tException.JWT_EXPIRED) {
                // expired: must do a retry
                LOGGER.info("JWT expired, performing reauth");
                final AuthInfo newInfo = authenticate(info.getSessionParameters(), info.getAuthenticationParameters());
                if (newInfo == null) {
                    throw new T9tException(T9tException.NOT_AUTHENTICATED);
                }
                authInfo.set(newInfo);
                return processor.execute(null, request, newInfo.getJwtInfo(), newInfo.getEncodedJwt(), false, null);
            }
        }
        return resp;
    }

    @Override
    public void close() {
        authInfo.set(null);
    }

    private AuthInfo authenticate(final SessionParameters sessionParameters, final AuthenticationParameters authenticationParameters) {
        final AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setSessionParameters(sessionParameters);
        authenticationRequest.setAuthenticationParameters(authenticationParameters);
        final AuthenticationResponse resp = authBackend.login(authenticationRequest);
        if (resp != null && resp.getReturnCode() == 0) {
            // success
            return new AuthInfo(sessionParameters, authenticationParameters, resp.getJwtInfo(), resp.getEncodedJwt());
        }
        // failure
        return null;
    }
}
