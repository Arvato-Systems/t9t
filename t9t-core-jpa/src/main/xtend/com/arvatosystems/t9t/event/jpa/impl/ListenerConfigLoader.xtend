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

import com.arvatosystems.t9t.base.services.impl.ListenerConfigCache
import com.arvatosystems.t9t.event.jpa.entities.ListenerConfigEntity
import com.arvatosystems.t9t.event.services.ListenerConfigConverter
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA
import de.jpaw.dp.Inject
import de.jpaw.dp.Provider
import de.jpaw.dp.Startup
import de.jpaw.dp.StartupOnly
import de.jpaw.util.ExceptionUtil
import javax.persistence.NoResultException

@Startup(55334)
@AddLogger
class ListenerConfigLoader implements StartupOnly {
    @Inject Provider<PersistenceProviderJPA> jpaContextProvider

    /** performs a standard query, but cross tenant. */
    override onStartup() {
        try {
            val cp = jpaContextProvider.get
            val query = cp.entityManager.createQuery(
                "SELECT sc FROM ListenerConfigEntity sc WHERE sc.isActive = :isActive",
                ListenerConfigEntity);
            query.setParameter("isActive", true)
            try {
                val results = query.resultList
                LOGGER.debug("{} JPA entity listener configurations loaded for t9t", results.size)
                for (e : results) {
                    ListenerConfigCache.updateRegistration(e.classification, e.tenantRef, ListenerConfigConverter.convert(e.ret$Data))
                }
            } catch (NoResultException e) {
                LOGGER.debug("No JPA entity listener configurations found for t9t")
            }
        } catch (Exception e) {
            LOGGER.error("Could not load JPA entity listener configurations - {}", ExceptionUtil.causeChain(e))
        }
    }
}
