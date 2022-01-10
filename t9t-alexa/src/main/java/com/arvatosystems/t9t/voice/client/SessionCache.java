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
package com.arvatosystems.t9t.voice.client;

import com.arvatosystems.t9t.voice.VoiceProvider;
import com.arvatosystems.t9t.voice.request.ProvideSessionResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.jpaw.dp.Jdp;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SessionCache<T extends VoiceSessionContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionCache.class);
    protected final Cache<String, T> cache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.HOURS).build();
    protected final IBackendCaller backendCaller = Jdp.getRequired(IBackendCaller.class);

    public T get(final String providerSession) {
        return cache.getIfPresent(providerSession);
    }

    // called just to create the object
    protected abstract T createSession();

    // called AFTER basic initialization is done
    protected void createHook(final T session) {
    }

    public T createSession(final String sessionId, final VoiceProvider provider, final String applicationId, final String userId, final String locale) {
        final T session = createSession();
        final ProvideSessionResponse auth = backendCaller.getSession(applicationId, userId, locale);
        if (auth == null) {
            LOGGER.error("Could not authenticate at backend!!!!");
            return null;
        }
        session.providerSessionKey = sessionId;
        session.connectionApiKey = auth.getApplication().getApiKey();
        session.shouldTerminateWhenDone = auth.getApplication().getSessionPerExtSession();
        if (auth.getUser() == null) {
            session.userName = null;
            session.userInternalId = null;
        } else {
            session.userName = auth.getUser().getName();
            session.userInternalId = auth.getUser().getInternalId();
        }
        cache.put(sessionId, session);
        return session;
    }

    public void removeSession(final String providerSession) {
        cache.invalidate(providerSession);
    }
}
