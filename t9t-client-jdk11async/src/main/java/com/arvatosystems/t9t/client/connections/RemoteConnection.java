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
package com.arvatosystems.t9t.client.connections;

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

import de.jpaw.bonaparte.util.IMarshaller;
import de.jpaw.bonaparte.util.impl.RecordMarshallerCompactBonaparteIdentity;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

@Singleton
public class RemoteConnection extends AbstractAsyncRemoteConnection implements IRemoteConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConnection.class);

    protected final IRemoteDefaultUrlRetriever cfgRetriever = Jdp.getRequired(IRemoteDefaultUrlRetriever.class);
    protected final RemoteHttpClient client = new RemoteHttpClient(20, 10);  // FIXME: obtain from JNDI
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


    @Override
    public CompletableFuture<ServiceResponse> executeAsync(final String authentication, final RequestParameters rp) {
        try {
            return client.doIO(rpcUri, authentication, rp);
        } catch (final Exception e) {
            final String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("I/O error for PQON {}: {}", rp.ret$PQON(), causeChain);
            return CompletableFuture.supplyAsync(() -> MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain));
        }
    }

    @Override
    public CompletableFuture<ServiceResponse> executeAuthenticationAsync(final AuthenticationRequest rp) {
        try {
            return client.doIO(authUri, null, rp);
        } catch (final Exception e) {
            final String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("I/O error for PQON {}: {}", rp.ret$PQON(), causeChain);
            return CompletableFuture.supplyAsync(() -> MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain));
        }
    }
}
