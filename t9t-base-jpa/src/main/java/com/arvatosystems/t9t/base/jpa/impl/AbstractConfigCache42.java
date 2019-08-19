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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.jpa.IResolverAnyKey42;
import com.arvatosystems.t9t.base.jpa.ormspecific.IQueryHintSetter;
import com.arvatosystems.t9t.base.services.ICacheInvalidationRegistry;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.util.ExceptionUtil;

public abstract class AbstractConfigCache42<DTO extends Ref, TRACKING extends TrackingBase, ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfigCache42.class);
    /** The cache is a 2 level map. The index of the first level is the tenantRef. A cache always contains all entries for a given tenant or none. */
    protected final Cache<Long,Map<Ref, DTO>> configCache = CacheBuilder.newBuilder().expireAfterWrite(15L, TimeUnit.MINUTES).build();
    protected final IResolverAnyKey42<Long, TRACKING, ENTITY> resolver;
    protected final Class<DTO> dtoClass;
    protected final boolean fallbackDefaultTenant;

    protected final Provider<RequestContext>   ctxProvider = Jdp.getProvider(RequestContext.class);
    protected final ICacheInvalidationRegistry cacheInvalidationRegistry = Jdp.getOptional(ICacheInvalidationRegistry.class);
    protected final IQueryHintSetter           queryHintSetter = Jdp.getOptional(IQueryHintSetter.class);

    protected AbstractConfigCache42(IResolverAnyKey42<Long, TRACKING, ENTITY> resolver, Class<DTO> dtoClass, boolean fallbackDefaultTenant) {
        LOGGER.info("Creating a new Cache for {}" , dtoClass.getSimpleName());
        this.resolver = resolver;
        this.dtoClass = dtoClass;
        this.fallbackDefaultTenant = fallbackDefaultTenant;
        if (cacheInvalidationRegistry != null)
            cacheInvalidationRegistry.registerInvalidator(dtoClass.getSimpleName(), (x) -> configCache.invalidateAll());
    }

    /** Retrieve DTO by key. key is known to be not null. Return null if no data is available. */
    protected DTO getOrNull(final Ref key) {
        final Long tenantRef = ctxProvider.get().tenantRef;
        final DTO myCfg = getConfigForTenant(tenantRef, key);
        return myCfg != null ? myCfg : getConfigForTenant(T9tConstants.GLOBAL_TENANT_REF42, key);
    }

    /** Retrieve DTO by key. key is known to be not null. Throw an exception if no data is available. */
    protected DTO get(final Ref key) {
        final DTO myCfg = getOrNull(key);
        if (myCfg == null) {
            LOGGER.error("Cannot read a record of type {} for key {}", dtoClass.getSimpleName(), key);
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, key);
        }
        return myCfg;
    }

    protected DTO getConfigForTenant(final Long tenantRef, final Ref key) {
        Map<Ref, DTO> tenantCache;
        try {
            tenantCache = configCache.get(tenantRef, () -> readWholeTenant(tenantRef));
        } catch (ExecutionException e) {
            LOGGER.error("Cannot read {} for tenant {}: {}", dtoClass.getSimpleName(), tenantRef, e);
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, ExceptionUtil.causeChain(e));
        }
        return tenantCache.get(key);
    }

    abstract protected void populateCache(Map<Ref, DTO> cache, ENTITY e);

    protected Map<Ref, DTO> readWholeTenant(Long tenantRef) {
        TypedQuery<ENTITY> query = resolver.getEntityManager().createQuery(
                "SELECT s FROM " + resolver.getBaseJpaEntityClass().getSimpleName()
                + " s WHERE s.isActive = :active AND s.tenantRef = :tenantRef", resolver.getEntityClass());
        query.setParameter("active", true);
        query.setParameter("tenantRef", tenantRef);
        queryHintSetter.setComment(query, "RefreshCache" + dtoClass.getSimpleName());
        queryHintSetter.setReadOnly(query);
        List<ENTITY> results = query.getResultList();
        LOGGER.debug("Filling cache for {} for tenantRef {}: got {} entries", dtoClass.getSimpleName(), tenantRef, results.size());
        // create a hashMap for concurrent access, with size known, it will never be modified later, so use the 3 arg constructor
        ConcurrentMap<Ref, DTO> resultMap = new ConcurrentHashMap<Ref, DTO>(results.size() * 4, 0.75f, 1);
        for (ENTITY r: results) {
            populateCache(resultMap, r);
        }
        return resultMap;
    }
}
