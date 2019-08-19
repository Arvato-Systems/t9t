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
package com.arvatosystems.t9t.base.jpa.entityListeners;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.event.EventParameters;
import com.arvatosystems.t9t.base.event.GeneralRefCreatedEvent;
import com.arvatosystems.t9t.base.event.GeneralRefDeletedEvent;
import com.arvatosystems.t9t.base.event.GeneralRefUpdatedEvent;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IsTenantSpecific;
import com.arvatosystems.t9t.base.services.impl.ListenerConfigCache;
import com.arvatosystems.t9t.base.types.ListenerConfig;

import de.jpaw.bonaparte.jpa.BonaPersistableNoData;
import de.jpaw.dp.Jdp;

public abstract class AbstractEntityListenerForEvents<
    E extends BonaPersistableNoData<Long, FullTrackingWithVersion> & IsTenantSpecific
  > {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityListenerForEvents.class);
    protected final String typeId;
    protected final String typeId2;
    protected final String typeId3;
    protected final ConcurrentMap<Long, ListenerConfig> settings;

    protected AbstractEntityListenerForEvents(String typeId, String typeId2, String typeId3) {
        this.typeId  = typeId;
        this.typeId2 = typeId2;
        this.typeId3 = typeId3;
        settings = ListenerConfigCache.getRegistrationForClassification(typeId);
    }

    static private final AtomicReference<MoreLazyReferences> moreRefs = new AtomicReference<MoreLazyReferences>();

    // a simpler logic would work in EclipseLink, but Hibernate creates all EntityListeners already when the EntityManagerFactory is set up,
    // and then the providers may not yet be known

    static private class MoreLazyReferences {
        private final IExecutor executor = Jdp.getRequired(IExecutor.class);
    }

    private static MoreLazyReferences getMoreRefs() {
        MoreLazyReferences myRef = moreRefs.get();
        if (myRef != null)
            return myRef;
        // init it
        myRef = new MoreLazyReferences();
        moreRefs.compareAndSet(null, myRef);
        return moreRefs.get();
    }

    /** Retrieves a secondary ref (for example sales order for a return or delivery order, or null if not applicable. */
    protected Long getSecondRef(E entity) {
        return null;
    }
    /** Retrieves a tertiary ref (for example customer for a return or order. */
    protected Long getThirdRef(E entity) {
        return null;
    }

    protected EventParameters createEvent(E entity) {
        return new GeneralRefCreatedEvent(entity.ret$Key(), typeId);
    }
    protected EventParameters deleteEvent(E entity) {
        return new GeneralRefDeletedEvent(entity.ret$Key(), typeId);
    }
    protected EventParameters updateEvent(E entity) {
        return new GeneralRefUpdatedEvent(entity.ret$Key(), typeId);
    }
    protected EventParameters secondEvent(Long ref2, String classification2) {
        return new GeneralRefUpdatedEvent(ref2, classification2);
    }
    protected EventParameters thirdEvent(Long ref3, String classification3) {
        return new GeneralRefUpdatedEvent(ref3, classification3);
    }

    // @PostPersist
    protected void postPersist(E entity) {
        ListenerConfig cfg = settings.get(entity.getTenantRef());
        LOGGER.trace("CREATE for {} on tenant {} / ref {}: cfg = {}", typeId, entity.getTenantRef(), entity.ret$Key(), cfg);
        if (cfg == null)
            return;
        if (cfg.getIssueCreatedEvents())
            getMoreRefs().executor.publishEvent(createEvent(entity));
        if (cfg.getCreationBuckets() != null)
            getMoreRefs().executor.writeToBuckets(cfg.getCreationBuckets(), entity.ret$Key(), T9tConstants.BUCKET_CREATED);
    }

    // @PostRemove
    protected void postRemove(E entity) {
        ListenerConfig cfg = settings.get(entity.getTenantRef());
        LOGGER.trace("REMOVE for {} on tenant {} / ref {}: cfg = {}", typeId, entity.getTenantRef(), entity.ret$Key(), cfg);
        if (cfg == null)
            return;
        if (cfg.getIssueDeletedEvents())
            getMoreRefs().executor.publishEvent(deleteEvent(entity));
        if (cfg.getDeletionBuckets() != null)
            getMoreRefs().executor.writeToBuckets(cfg.getDeletionBuckets(), entity.ret$Key(), T9tConstants.BUCKET_DELETED);
    }

    // @PostUpdate
    protected void postUpdate(E entity) {
        ListenerConfig cfg = settings.get(entity.getTenantRef());
        LOGGER.trace("UPDATE for {} on tenant {} / ref {}: cfg = {}", typeId, entity.getTenantRef(), entity.ret$Key(), cfg);
        if (cfg == null)
            return;
        final MoreLazyReferences moreRefs = getMoreRefs();
        if (cfg.getIssueUpdatedEvents())
            moreRefs.executor.publishEvent(updateEvent(entity));
        if (cfg.getUpdateBuckets() != null)
            moreRefs.executor.writeToBuckets(cfg.getUpdateBuckets(), entity.ret$Key(), T9tConstants.BUCKET_UPDATED);

        // check for secondary
        final Long ref2 = getSecondRef(entity);
        if (ref2 != null && typeId2 != null) {
            if (cfg.getIssueSecondEvents())
                moreRefs.executor.publishEvent(secondEvent(ref2, typeId2));
            if (cfg.getSecondBuckets() != null)
                moreRefs.executor.writeToBuckets(cfg.getSecondBuckets(), ref2, T9tConstants.BUCKET_UPDATED);
        }

        // check for tertiary
        final Long ref3 = getThirdRef(entity);
        if (ref3 != null && typeId3 != null) {
            if (cfg.getIssueThirdEvents())
                moreRefs.executor.publishEvent(thirdEvent(ref3, typeId3));
            if (cfg.getThirdBuckets() != null)
                moreRefs.executor.writeToBuckets(cfg.getThirdBuckets(), ref3, T9tConstants.BUCKET_UPDATED);
        }
    }
}
