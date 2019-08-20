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
package com.arvatosystems.t9t.monitoring.services.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.monitoring.services.IDebugFlags;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;

@Singleton
public class DebugFlags implements IDebugFlags {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugFlags.class);
    protected final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);

    protected final Cache<Long, ConcurrentMap<String, String>> settingsStore
        = CacheBuilder.newBuilder().expireAfterWrite(15L, TimeUnit.MINUTES).<Long,ConcurrentMap<String, String>>build();

    protected Long getSession(RequestContext ctx) {
        return ctx.internalHeaderParameters.getJwtInfo().getSessionRef();
    }

    @Override
    public ConcurrentMap<String, String> getAllFlags() {
        return getAllFlags(ctxProvider.get());
    }

    @Override
    public ConcurrentMap<String, String> getAllFlags(RequestContext ctx) {
        return settingsStore.getIfPresent(getSession(ctx));
    }

    @Override
    public String getFlag(String flag) {
        return getFlag(ctxProvider.get(), flag);
    }

    @Override
    public String getFlag(RequestContext ctx, String flag) {
        final ConcurrentMap<String, String> map = getAllFlags(ctx);
        if (map == null)
            return null;
        return map.get(flag);
    }

    @Override
    public void setFlags(final RequestContext ctx, final Map<String, String> newFlags) throws Exception {
        final Long sessionRef = getSession(ctx);
        if (newFlags.isEmpty()) {
            LOGGER.debug("Clearing all DebugFlags");
            settingsStore.invalidate(sessionRef);
        }
        // create a new store, if none exists
        final ConcurrentMap<String, String> map = settingsStore.get(sessionRef, () -> new ConcurrentHashMap<String, String>(2 * newFlags.size()));
        for (Map.Entry<String, String> entry: newFlags.entrySet()) {
            if (entry.getValue() == null) {
                LOGGER.debug("Clearing DebugFlag {}", entry.getKey());
                map.remove(entry.getKey());
            } else {
                LOGGER.debug("Setting DebugFlag {} to {}", entry.getKey(), entry.getValue());
                map.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
