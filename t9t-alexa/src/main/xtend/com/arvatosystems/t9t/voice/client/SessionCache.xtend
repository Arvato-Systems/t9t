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

import com.arvatosystems.t9t.voice.VoiceProvider
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import java.util.concurrent.TimeUnit

@AddLogger
abstract class SessionCache<T extends VoiceSessionContext> {
    protected val Cache<String, T> CACHE = CacheBuilder.newBuilder.expireAfterWrite(1L, TimeUnit.HOURS).build
    @Inject protected IBackendCaller backendCaller

    def T get(String providerSession) {
        return CACHE.getIfPresent(providerSession)
    }

    // called just to create the object
    def abstract protected T createSession();

    // called AFTER basic initialization is done
    def protected void createHook(T session) {

    }
    def T createSession(String sessionId, VoiceProvider provider, String applicationId, String userId, String locale) {
        val session = createSession
        val auth = backendCaller.getSession(applicationId, userId, locale)
        if (auth === null) {
            LOGGER.error("Could not authenticate at backend!!!!")
            return null
        }
        session.providerSessionKey = sessionId
        session.connectionApiKey = auth.application.apiKey
        session.shouldTerminateWhenDone = auth.application.sessionPerExtSession
        session.userName = auth.user?.name
        session.userInternalId = auth.user?.internalId
        CACHE.put(sessionId, session)
        return session
    }

    def void removeSession(String providerSession) {
        CACHE.invalidate(providerSession)
    }
}
