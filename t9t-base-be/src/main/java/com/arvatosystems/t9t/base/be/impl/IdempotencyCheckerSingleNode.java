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
package com.arvatosystems.t9t.base.be.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.RetryAdviceType;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.IIdempotencyChecker;
import com.arvatosystems.t9t.cfg.be.ApplicationConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Singleton
@Fallback
public class IdempotencyCheckerSingleNode implements IIdempotencyChecker {
    // private static final Logger LOGGER = LoggerFactory.getLogger(IdempotencyCheckerSingleNode.class);

    protected final Map<String, Cache<UUID, ServiceResponse>> requestCache;
    protected final Integer idempotencyCacheMaxEntries;
    protected final Integer idempotencyCacheExpiry;

    public IdempotencyCheckerSingleNode() {
        // constructor code is required to initialize the cache as specified by the config file
        final ApplicationConfiguration applCfg = ConfigProvider.getConfiguration().getApplicationConfiguration();
        if (applCfg == null || applCfg.getIdempotencyCacheMaxEntries() == null) {
            requestCache = null;
            idempotencyCacheMaxEntries = null;
            idempotencyCacheExpiry = null;
        } else {
            // there should be a cache
            requestCache = new ConcurrentHashMap<>(8);
            idempotencyCacheMaxEntries = applCfg.getIdempotencyCacheMaxEntries();
            idempotencyCacheExpiry = applCfg.getIdempotencyCacheExpiry();
        }
    }

    protected Cache<UUID, ServiceResponse> buildNewCache() {
        final Caffeine<Object, Object> builder = Caffeine.newBuilder().maximumSize(idempotencyCacheMaxEntries);
        if (idempotencyCacheExpiry != null) {
            builder.expireAfterWrite(idempotencyCacheExpiry, TimeUnit.SECONDS);
        }
        return builder.<UUID, ServiceResponse>build();
    }

    @Override
    public ServiceResponse runIdempotencyCheck(final String tenantId, final UUID messageId, final RetryAdviceType idempotencyBehaviour,
            final RequestParameters rp) {
        if (requestCache == null) {
            return null;
        }
        final Cache<UUID, ServiceResponse> tenantCache = requestCache.computeIfAbsent(tenantId, id -> buildNewCache());
        final AtomicBoolean justSetNow = new AtomicBoolean(false);
        final ServiceResponse r = tenantCache.get(messageId, unused -> {
            justSetNow.set(true);
            final ServiceResponse newTimeout = new ServiceResponse();
            newTimeout.setMessageId(messageId);
            newTimeout.setTenantId(tenantId);
            newTimeout.setReturnCode(T9tException.REQUEST_STILL_PROCESSING);
            newTimeout.setErrorMessage(T9tException.MSG_REQUEST_STILL_PROCESSING);
            newTimeout.freeze();
            return newTimeout;
        });
        if (justSetNow.get()) {
            return null;
        }
        return r;
    }

    @Override
    public void storeIdempotencyResult(final String tenantId, final UUID messageId, final RetryAdviceType idempotencyBehaviour,
      final RequestParameters rp, final ServiceResponse resp) {
        if (requestCache == null) {
            return;
        }
        final Cache<UUID, ServiceResponse> tenantCache = requestCache.computeIfAbsent(tenantId, id -> buildNewCache());
        if (idempotencyBehaviour == RetryAdviceType.RETRY_ON_ERROR && resp.getReturnCode() >= 2 * ApplicationException.CLASSIFICATION_FACTOR) {
            // this is an error. We should retry, so remove the cache entry
            tenantCache.invalidate(messageId);
        } else {
            // the result should be updated
            tenantCache.put(messageId, resp);
        }
    }
}
