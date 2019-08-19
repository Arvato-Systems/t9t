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
package com.arvatosystems.t9t.base.jpa.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.jpa.IResolverAnyKey42;
import com.arvatosystems.t9t.base.jpa.ormspecific.IQueryHintSetter;
import com.arvatosystems.t9t.base.misc.SomeCacheKey42;
import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ExceptionUtil;

public abstract class AbstractConfigCacheFieldwiseInvalidation42<DTO extends Ref, TRACKING extends TrackingBase, ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfigCacheFieldwiseInvalidation42.class);
    /** The cache consists of 2 single level map caches, one by objectRef, one by tenantRef / id. */
    protected final Cache<Long, DTO> cacheByObjectRef = CacheBuilder.newBuilder().expireAfterWrite(15L, TimeUnit.MINUTES).build();
    protected final Cache<SomeCacheKey42, Long> cacheById = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.HOURS).build();
    protected final IResolverAnyKey42<Long, TRACKING, ENTITY> resolver;
    protected final Class<DTO> dtoClass;
    protected final ICacheInvalidationRegistry cacheInvalidationRegistry = Jdp.getOptional(ICacheInvalidationRegistry.class);
    protected final IQueryHintSetter           queryHintSetter = Jdp.getOptional(IQueryHintSetter.class);

    protected AbstractConfigCacheFieldwiseInvalidation42(IResolverAnyKey42<Long, TRACKING, ENTITY> resolver, Class<DTO> dtoClass) {
        LOGGER.info("Creating a new Cache for {}" , dtoClass.getSimpleName());
        this.resolver = resolver;
        this.dtoClass = dtoClass;
        if (cacheInvalidationRegistry != null) {
            cacheInvalidationRegistry.registerInvalidator(dtoClass.getSimpleName(), (x) -> {
                if (x == null) {
                    LOGGER.debug("Full cache invalidation for {}", dtoClass.getSimpleName());
                    cacheByObjectRef.invalidateAll();
                    cacheById.invalidateAll();
                    return;
                }
                if (x instanceof Ref) {
                    final Long ref = ((Ref)x).getObjectRef();
                    LOGGER.debug("Cache invalidation for {} for objectRef {}", dtoClass.getSimpleName(), ref);
                    cacheByObjectRef.invalidate(ref);
                } else if (x instanceof SomeCacheKey42) {
                    LOGGER.debug("Cache invalidation for {} for {}", dtoClass.getSimpleName(), x);
                    cacheById.invalidate(x);
                } else {
                    LOGGER.error("Received cache invalidation on {} with key type {} - cannot handle, ignoring", dtoClass.getSimpleName(), x.getClass().getCanonicalName());
                }
            });
        }
    }

    protected abstract DTO map(ENTITY e);
    protected abstract Long read(String id);

    protected DTO get(final Long key) {
        try {
            return cacheByObjectRef.get(key, () -> {
                LOGGER.debug("Filling cache {} for objectRef {}", dtoClass.getSimpleName(), key);
                final DTO dto = map(resolver.find(key));
                dto.freeze();
                return dto;
            });
        } catch (ExecutionException e) {
            LOGGER.error("Cannot read {} for objectRef {}: {}", dtoClass.getSimpleName(), key, e);
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, ExceptionUtil.causeChain(e));
        }
    }

    protected Long resolve(final String id) {
        final SomeCacheKey42 key = new SomeCacheKey42(resolver.getSharedTenantRef(), id);
        key.freeze();
        try {
            return cacheById.get(key, () -> {
                final Long ref = read(id);
                LOGGER.debug("Filling resolver cache {} for tenantRef {}, id {} to resolve to {}", dtoClass.getSimpleName(), key.getTenantRef(), key.getId(), ref);
                return ref;
            });
        } catch (ExecutionException e) {
            LOGGER.error("Cannot read {} for tenantRef {}, id {}: {}", dtoClass.getSimpleName(), key.getTenantRef(), key.getId(), e);
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, ExceptionUtil.causeChain(e));
        }
    }
}
