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
package com.arvatosystems.t9t.client.apache;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.AbstractAsyncRemoteConnection;
import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.IRemoteDefaultUrlRetriever;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.HttpPostResponseObject;
import de.jpaw.bonaparte.util.IMarshaller;
import de.jpaw.bonaparte.util.impl.RecordMarshallerCompactBonaparteIdentity;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;

@Singleton
public class RemoteConnection extends AbstractAsyncRemoteConnection implements IRemoteConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConnection.class);

    protected final IRemoteDefaultUrlRetriever cfgRetriever = Jdp.getRequired(IRemoteDefaultUrlRetriever.class);
    protected final RemoteHttpClient client = new RemoteHttpClient(20);  // FIXME: obtain from JNDI
    protected final URI authUri;
    protected final URI rpcUri;

    // protected final IMarshaller marshaller = new RecordMarshallerBonaparte();
    protected final IMarshaller marshaller = new RecordMarshallerCompactBonaparteIdentity();  // preserve objects within Json fields!

    public RemoteConnection() {
        try {
            authUri = new URI(cfgRetriever.getDefaultRemoteUrlLogin());
        } catch (final URISyntaxException e) {
            LOGGER.error("FATAL: Cannot construct remote authentication URI: {}", ExceptionUtil.causeChain(e));
            throw new RuntimeException(e);
        }
        try {
            rpcUri = new URI(cfgRetriever.getDefaultRemoteUrl());
        } catch (final URISyntaxException e) {
            LOGGER.error("FATAL: Cannot construct remote request URI: {}", ExceptionUtil.causeChain(e));
            throw new RuntimeException(e);
        }
    }

    private ServiceResponse convertResponse(final HttpPostResponseObject resp, final boolean isAuthentication) {
        if (resp.getHttpReturnCode() / 100 != 2) {
            final BonaPortable response = resp.getResponseObject();
            if (response instanceof ServiceResponse) {
                ServiceResponse sr = (ServiceResponse)response;
                if (!ApplicationException.isOk(sr.getReturnCode())) {
                    return MessagingUtil.createServiceResponse(
                        T9tException.HTTP_ERROR + resp.getHttpReturnCode(),
                        Integer.toString(sr.getReturnCode()) + ": " + (sr.getErrorDetails() != null ? sr.getErrorDetails() + " " : "") + sr.getErrorMessage(),
                        null, null);
                }
            }
            return MessagingUtil.createServiceResponse(
                    T9tException.HTTP_ERROR + resp.getHttpReturnCode(),
                    resp.getHttpStatusMessage(),
                    null, null);
        }
        final BonaPortable response = resp.getResponseObject();
        if (response == null) {
            if (isAuthentication) {
                LOGGER.info("Response object is null for AUTHENTICATION: http code {}, status {}", resp.getHttpReturnCode(), resp.getHttpStatusMessage());
                return MessagingUtil.createServiceResponse(
                        T9tException.GENERAL_AUTH_PROBLEM,
                        AuthenticationResponse.class.getCanonicalName(),
                        null, null);
            } else {
                LOGGER.info("Response object is null for GENERAL request: http code {}, status {}", resp.getHttpReturnCode(), resp.getHttpStatusMessage());
                return MessagingUtil.createServiceResponse(
                        T9tException.BAD_REMOTE_RESPONSE,
                        Integer.toString(resp.getHttpReturnCode()),
                        null, null);
            }
        }
        if (response instanceof ServiceResponse) {
            final ServiceResponse r = (ServiceResponse)response;
            LOGGER.info("Received response type {} with return code {}", r.ret$PQON(), r.getReturnCode());
            if (r.getReturnCode() != 0) {
                LOGGER.info("Error details are {}, message is {}", r.getErrorDetails(), r.getErrorMessage());
            }
            return r;
        }


        LOGGER.error("Response is of wrong type: {}", response.getClass().getCanonicalName());
        return MessagingUtil.createServiceResponse(
                T9tException.GENERAL_EXCEPTION,
                response.getClass().getCanonicalName(),
                null, null);

    }

    @Override
    public CompletableFuture<ServiceResponse> executeAsync(final String authentication, final RequestParameters rp) {
        try {
            final CompletableFuture<HttpPostResponseObject> respF = client.doIO(rpcUri, authentication, rp);
            return respF.thenApply(resp -> {
                return convertResponse(resp, rp instanceof AuthenticationRequest);
            });
        } catch (final Exception e) {
            final String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("I/O error for PQON {}: {}", rp.ret$PQON(), causeChain);
            return CompletableFuture.supplyAsync(() -> MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain, null, null));
        }
    }

    @Override
    public CompletableFuture<ServiceResponse> executeAuthenticationAsync(final AuthenticationRequest rp) {
        try {
            final CompletableFuture<HttpPostResponseObject> respF = client.doIO(rpcUri, null, rp);
            return respF.thenApply(resp -> {
                return convertResponse(resp, rp instanceof AuthenticationRequest);
            });
        } catch (final Exception e) {
            final String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("I/O error for PQON {}: {}", rp.ret$PQON(), causeChain);
            return CompletableFuture.supplyAsync(() -> MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain, null, null));
        }
    }
}
