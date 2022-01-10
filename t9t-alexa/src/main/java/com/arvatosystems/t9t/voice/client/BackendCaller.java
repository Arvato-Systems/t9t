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
package com.arvatosystems.t9t.voice.client;

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.types.SessionParameters;
import com.arvatosystems.t9t.voice.VoiceProvider;
import com.arvatosystems.t9t.voice.request.ProvideSessionRequest;
import com.arvatosystems.t9t.voice.request.ProvideSessionResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BackendCaller implements IBackendCaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackendCaller.class);
    private static final Cache<UUID, String> REMOTE_BASE = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
    private static final UUID ROOT_API_KEY = UUID.fromString("282fd9dd-233c-4ebe-8d91-727bebd83567");
    private static final String USER_AGENT = "Alexa proxy 0.1";

    protected final IRemoteConnection statelessServiceSession = Jdp.getRequired(IRemoteConnection.class);

    @Override
    public ProvideSessionResponse getSession(final String applicationId, final String userId, final String locale) {
        // first, get the connection to get the connection...
        final String encodedRootJwt = getJwt(ROOT_API_KEY);
        final ProvideSessionRequest psr = new ProvideSessionRequest();
        psr.setProvider(VoiceProvider.ALEXA);
        psr.setApplicationId(applicationId);
        psr.setUserId(userId);
        final ServiceResponse psresp = statelessServiceSession.execute(encodedRootJwt, psr);
        if (psresp instanceof ProvideSessionResponse) {
            return (ProvideSessionResponse) psresp;
        }
        return null;
    }

    @Override
    public ServiceResponse executeWithApiKey(final UUID apiKey, final RequestParameters rq) {
        return statelessServiceSession.execute(getJwt(apiKey), rq);
    }

    protected String getJwt(final UUID apiKey) {
        try {
            return REMOTE_BASE.get(apiKey, () -> {
                final SessionParameters sessionParameters = new SessionParameters();
                sessionParameters.setUserAgent(USER_AGENT);
                final AuthenticationRequest authRq = new AuthenticationRequest();
                authRq.setAuthenticationParameters(new ApiKeyAuthentication(apiKey));
                authRq.setSessionParameters(sessionParameters);
                final ServiceResponse sr = statelessServiceSession.executeAuthenticationRequest(authRq);
                if (sr instanceof AuthenticationResponse) {
                    return "Bearer " + ((AuthenticationResponse) sr).getEncodedJwt();
                }
                LOGGER.error("Could not get access for API Key {}", apiKey);
                throw new T9tException(T9tException.NOT_AUTHENTICATED, "Received error code " + sr.getReturnCode());
            });
        } catch (ExecutionException e) {
            LOGGER.error("Execution exception thrown when getting access for API Key " + apiKey, e);
            throw new T9tException(T9tException.GENERAL_EXCEPTION, "Execution exception thrown when getting access for API Key " + apiKey, e);
        }
    }
}
