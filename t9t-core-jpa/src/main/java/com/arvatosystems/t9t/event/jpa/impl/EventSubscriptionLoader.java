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
package com.arvatosystems.t9t.event.jpa.impl;

import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor;
import com.arvatosystems.t9t.base.services.IEventHandler;
import com.arvatosystems.t9t.base.services.impl.EventSubscriptionCache;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.event.jpa.entities.SubscriberConfigEntity;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;
import de.jpaw.util.ExceptionUtil;

import java.util.List;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup(55333)
public class EventSubscriptionLoader implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventSubscriptionLoader.class);

    private final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);
    private final IAsyncRequestProcessor asyncProcessor = Jdp.getRequired(IAsyncRequestProcessor.class);

    /** performs a standard query, but cross tenant. */
    @Override
    public void onStartup() {
        try {
            final String myEnvironment = ConfigProvider.getConfiguration().getEventEnvironment();
            final PersistenceProviderJPA cp = jpaContextProvider.get();
            final TypedQuery<SubscriberConfigEntity> query = cp.getEntityManager()
                    .createQuery("SELECT sc FROM SubscriberConfigEntity sc WHERE sc.isActive = :isActive", SubscriberConfigEntity.class);
            query.setParameter("isActive", true);
            try {
                final List<SubscriberConfigEntity> results = query.getResultList();
                LOGGER.debug("{} event subscriptions loaded for t9t", results.size());
                for (final SubscriberConfigEntity e : results) {
                    if (myEnvironment == null || e.getEnvironment() == null || myEnvironment.equals(e.getEnvironment())) {
                        LOGGER.debug("Subscribing to event {}", e.getHandlerClassName());
                        final IEventHandler eventHandler = Jdp.getOptional(IEventHandler.class, e.getHandlerClassName());
                        if (eventHandler == null) {
                            LOGGER.error("No event handler found for qualifier {} - ignoring subscriptions", e.getHandlerClassName());
                        } else {
                            asyncProcessor.registerSubscriber(e.getEventID(), e.getTenantRef(), eventHandler);
                            EventSubscriptionCache.updateRegistration(e.getEventID(), e.getHandlerClassName(), e.getTenantRef(), e.getIsActive());
                        }
                    } else {
                        LOGGER.debug("Not subscribing to event {}, is for {} and not relevant for this server {}", e.getHandlerClassName(), e.getEnvironment(),
                                myEnvironment);
                    }
                }
            } catch (final NoResultException e) {
                LOGGER.debug("No event subscriptions found for t9t");
            }
        } catch (final Exception e) {
            LOGGER.error("Could not load event handler subscriptions - {}", ExceptionUtil.causeChain(e));
        }
    }
}
