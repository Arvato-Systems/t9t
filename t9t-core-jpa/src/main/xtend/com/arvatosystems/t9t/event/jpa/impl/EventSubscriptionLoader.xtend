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
package com.arvatosystems.t9t.event.jpa.impl

import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor
import com.arvatosystems.t9t.base.services.IEventHandler
import com.arvatosystems.t9t.base.services.impl.EventSubscriptionCache
import com.arvatosystems.t9t.event.jpa.entities.SubscriberConfigEntity
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA
import de.jpaw.dp.Inject
import de.jpaw.dp.Jdp
import de.jpaw.dp.Provider
import de.jpaw.dp.Startup
import de.jpaw.dp.StartupOnly
import de.jpaw.util.ExceptionUtil
import javax.persistence.NoResultException
import com.arvatosystems.t9t.cfg.be.ConfigProvider

@Startup(55333)
@AddLogger
class EventSubscriptionLoader implements StartupOnly {
    @Inject Provider<PersistenceProviderJPA> jpaContextProvider
    @Inject IAsyncRequestProcessor asyncProcessor

    /** performs a standard query, but cross tenant. */
    override onStartup() {
        try {
            val myEnvironment = ConfigProvider.configuration.eventEnvironment
            val cp = jpaContextProvider.get
            val query = cp.entityManager.createQuery(
                "SELECT sc FROM SubscriberConfigEntity sc WHERE sc.isActive = :isActive",
                SubscriberConfigEntity);
            query.setParameter("isActive", true)
            try {
                val results = query.resultList
                LOGGER.debug("{} event subscriptions loaded for t9t", results.size)
                for (e : results) {
                    if (myEnvironment === null || e.environment === null || myEnvironment == e.environment) {
                        LOGGER.debug("Subscribing to event {}", e.handlerClassName)
                        val eventHandler = Jdp.getOptional(IEventHandler, e.handlerClassName)
                        if (eventHandler === null) {
                            LOGGER.error("No event handler found for qualifier {} - ignoring subscriptions", e.handlerClassName)
                        } else {
                            asyncProcessor.registerSubscriber(e.eventID, e.tenantRef, eventHandler)
                            EventSubscriptionCache.updateRegistration(e.eventID, e.handlerClassName, e.tenantRef, e.isActive)
                        }
                    } else {
                        LOGGER.debug("Not subscribing to event {}, is for {} and not relevant for this server {}", e.handlerClassName, e.environment, myEnvironment)
                    }
                }
            } catch (NoResultException e) {
                LOGGER.debug("No event subscriptions found for t9t")
            }
        } catch (Exception e) {
            LOGGER.error("Could not load event handler subscriptions - {}", ExceptionUtil.causeChain(e))
        }
    }
}
