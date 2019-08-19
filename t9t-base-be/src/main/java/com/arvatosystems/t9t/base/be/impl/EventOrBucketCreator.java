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
package com.arvatosystems.t9t.base.be.impl;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.event.BucketDataMode;
import com.arvatosystems.t9t.base.event.GeneralRefCreatedEvent;
import com.arvatosystems.t9t.base.event.GeneralRefDeletedEvent;
import com.arvatosystems.t9t.base.event.GeneralRefUpdatedEvent;
import com.arvatosystems.t9t.base.services.IEventOrBucketCreator;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.services.impl.ListenerConfigCache;
import com.arvatosystems.t9t.base.types.ListenerConfig;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;

@Singleton
public class EventOrBucketCreator implements IEventOrBucketCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventOrBucketCreator.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);
    protected final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);

    @Override
    public void createBucketOrEvent(String typeId, Long ref, BucketDataMode mode) {
        createBucketOrEvent(ctxProvider.get(), typeId, ref, mode);
    }

    /** Overridable hook to create special events where required. */
    protected void createEvent(String typeId, Long ref) {
        executor.publishEvent(new GeneralRefCreatedEvent(ref, typeId));
    }

    /** Overridable hook to create special events where required. */
    protected void deleteEvent(String typeId, Long ref) {
        executor.publishEvent(new GeneralRefDeletedEvent(ref, typeId));
    }

    /** Overridable hook to create special events where required. */
    protected void updateEvent(String typeId, Long ref) {
        executor.publishEvent(new GeneralRefUpdatedEvent(ref, typeId));
    }

    @Override
    public void createBucketOrEvent(RequestContext ctx, String typeId, Long ref, BucketDataMode mode) {
        ConcurrentMap<Long, ListenerConfig> settings = ListenerConfigCache.getRegistrationForClassification(typeId);
        if (settings == null)
            return;  // nothing configured
        ListenerConfig cfg = settings.get(ctx.tenantRef);
        if (cfg == null)
            return;  // nothing configured

        switch (mode) {
        case CREATE:
            LOGGER.trace("CREATE for {} on tenant {} / ref {}: cfg = {}", typeId, ctx.tenantRef, ref, cfg);
            if (cfg.getIssueCreatedEvents())
                createEvent(typeId, ref);
            if (cfg.getCreationBuckets() != null)
                executor.writeToBuckets(cfg.getCreationBuckets(), ref, T9tConstants.BUCKET_CREATED);
            break;
        case DELETE:
            LOGGER.trace("REMOVE for {} on tenant {} / ref {}: cfg = {}", typeId, ctx.tenantRef, ref, cfg);
            if (cfg.getIssueDeletedEvents())
                deleteEvent(typeId, ref);
            if (cfg.getDeletionBuckets() != null)
                executor.writeToBuckets(cfg.getDeletionBuckets(), ref, T9tConstants.BUCKET_DELETED);
            break;
        case UPDATE:
            LOGGER.trace("UPDATE for {} on tenant {} / ref {}: cfg = {}", typeId, ctx.tenantRef, ref, cfg);
            if (cfg.getIssueUpdatedEvents())
                updateEvent(typeId, ref);
            if (cfg.getUpdateBuckets() != null)
                executor.writeToBuckets(cfg.getUpdateBuckets(), ref, T9tConstants.BUCKET_UPDATED);
            break;
        }
    }
}
