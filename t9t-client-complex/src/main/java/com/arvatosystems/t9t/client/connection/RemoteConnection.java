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
package com.arvatosystems.t9t.client.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.client.init.AbstractConfigurationProvider;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.HttpPostResponseObject;
import de.jpaw.bonaparte.sock.HttpPostClient;
import de.jpaw.bonaparte.util.IMarshaller;
import de.jpaw.bonaparte.util.impl.RecordMarshallerCompactBonaparteIdentity;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

@Singleton
public class RemoteConnection extends AbstractRemoteConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConnection.class);

    protected final AbstractConfigurationProvider configurationProvider = Jdp.getRequired(AbstractConfigurationProvider.class);
    // protected final IMarshaller marshaller = new RecordMarshallerBonaparte();
    protected final IMarshaller marshaller = new RecordMarshallerCompactBonaparteIdentity();  // preserve objects within Json fields!

//    protected final IConnection connection = Jdp.getRequired(IConnection.class);
//    protected final IConnection connection;
//
//    public RemoteConnection() {
//        //use the pooling connector - IConnection instances require a non-empty args constructor
//        connection = new HttpClientPool(
//                configurationProvider.getRemoteHost(),
//                configurationProvider.getRemotePort(),
//                16,
//                marshaller);
//    }
    protected String makeUrl(String path) {
        return "http://" + configurationProvider.getRemoteHost() + ":" + configurationProvider.getRemotePort() + path;
    }

    @Override
    public ServiceResponse execute(String authenticationHeader, RequestParameters rp) {
        String url = makeUrl(configurationProvider.getRemotePathRequests());
        HttpPostClient dlg = new HttpPostClient(url, false, true, false, false, marshaller);
        dlg.setAuthentication(authenticationHeader);
        return execSub(dlg, rp);
    }

    protected ServiceResponse execSub(HttpPostClient dlg, RequestParameters rp) {
        try {
            LOGGER.info("Sending request of type {}", rp.ret$PQON());
            final HttpPostResponseObject resp = dlg.doIO2(rp);
            if (resp.getHttpReturnCode() / 100 != 2) {
                return MessagingUtil.createServiceResponse(
                        99000 + resp.getHttpReturnCode(),
                        resp.getHttpStatusMessage(),
                        null, null);
            }
            final BonaPortable response = resp.getResponseObject();
            if (response == null) {
                LOGGER.info("Response object is null for http code {}, status {}", resp.getHttpReturnCode(), resp.getHttpStatusMessage());
                return MessagingUtil.createServiceResponse(
                        T9tException.BAD_REMOTE_RESPONSE,
                        Integer.toString(resp.getHttpReturnCode()),
                        null, null);
            }
            if (response instanceof ServiceResponse) {
                ServiceResponse r = (ServiceResponse)response;
                LOGGER.info("Received response type {} with return code {}", r.ret$PQON(), r.getReturnCode());
                if (r.getReturnCode() != 0) {
                    LOGGER.info("Error details are {}, message is {}", r.getErrorDetails(), r.getErrorMessage());
                }
                return r;
            }

            if (rp instanceof AuthenticationRequest && response == null) {
                return MessagingUtil.createServiceResponse(
                        T9tException.GENERAL_AUTH_PROBLEM,
                        AuthenticationResponse.class.getCanonicalName(),
                        null, null);
            }

            LOGGER.error("Response is of wrong type: {}", response.getClass().getCanonicalName());
            return MessagingUtil.createServiceResponse(
                    T9tException.GENERAL_EXCEPTION,
                    response.getClass().getCanonicalName(),
                    null, null);
        } catch (Exception e) {
            String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("I/O error for PQON {}: {}", rp.ret$PQON(), causeChain);
            return MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain, null, null);
        }
    }

    @Override
    public ServiceResponse executeAuthenticationRequest(AuthenticationRequest rp) {
        String url = makeUrl(configurationProvider.getRemotePathAuthentication());
        HttpPostClient dlg = new HttpPostClient(url, false, true, false, false, marshaller);
        return execSub(dlg, rp);
    }
}
