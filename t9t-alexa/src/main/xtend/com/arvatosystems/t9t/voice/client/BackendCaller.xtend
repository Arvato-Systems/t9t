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
package com.arvatosystems.t9t.voice.client

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.types.SessionParameters
import com.arvatosystems.t9t.client.connection.IRemoteConnection
import com.arvatosystems.t9t.voice.VoiceProvider
import com.arvatosystems.t9t.voice.request.ProvideSessionRequest
import com.arvatosystems.t9t.voice.request.ProvideSessionResponse
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import java.util.UUID
import java.util.concurrent.TimeUnit

@AddLogger
@Singleton
class BackendCaller implements IBackendCaller {
    @Inject protected IRemoteConnection statelessServiceSession

    val ROOT_API_KEY = UUID.fromString("282fd9dd-233c-4ebe-8d91-727bebd83567")
    val USER_AGENT   = "Alexa proxy 0.1"
    val Cache<UUID, String> REMOTE_BASE = CacheBuilder.newBuilder.expireAfterWrite(5, TimeUnit.MINUTES).build

    def protected String getJwt(UUID apiKey) {
        return REMOTE_BASE.get(apiKey) [
            val sr = statelessServiceSession.executeAuthenticationRequest(new AuthenticationRequest => [
                authenticationParameters = new ApiKeyAuthentication(apiKey)
                sessionParameters = new SessionParameters => [
                    userAgent = USER_AGENT
                ]
            ])
            if (sr instanceof AuthenticationResponse)
                return "Bearer " + sr.encodedJwt
            LOGGER.error("Could not get access for API Key {}", apiKey)
            throw new T9tException(T9tException.NOT_AUTHENTICATED, "Received error code " + sr.returnCode)
        ]
    }

    override getSession(String applicationId, String userId, String locale) {
        // first, get the connection to get the connection...
        val encodedRootJwt = getJwt(ROOT_API_KEY)
        val ProvideSessionRequest psr = new ProvideSessionRequest
        psr.provider = VoiceProvider.ALEXA
        psr.applicationId = applicationId
        psr.userId = userId
        val psresp = statelessServiceSession.execute(encodedRootJwt, psr)
        if (psresp instanceof ProvideSessionResponse)
            return psresp
        return null
    }

    override executeWithApiKey(UUID apiKey, RequestParameters rq) {
        val jwt = getJwt(apiKey)
        return statelessServiceSession.execute(jwt, rq)
    }
}
