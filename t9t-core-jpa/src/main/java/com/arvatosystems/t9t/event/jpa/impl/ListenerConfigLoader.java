/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.services.impl.ListenerConfigCache;
import com.arvatosystems.t9t.event.jpa.entities.ListenerConfigEntity;
import com.arvatosystems.t9t.event.services.ListenerConfigConverter;

@Startup(55334)
public class ListenerConfigLoader implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerConfigLoader.class);
    private final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);

    /** performs a standard query, but cross tenant. */
    @Override
    public void onStartup() {
        try {
            final PersistenceProviderJPA cp = this.jpaContextProvider.get();
            final TypedQuery<ListenerConfigEntity> query = cp.getEntityManager()
                    .createQuery("SELECT sc FROM ListenerConfigEntity sc WHERE sc.isActive = :isActive", ListenerConfigEntity.class);
            query.setParameter("isActive", true);
            try {
                final List<ListenerConfigEntity> results = query.getResultList();
                LOGGER.debug("{} JPA entity listener configurations loaded for t9t", Integer.valueOf(results.size()));
                for (final ListenerConfigEntity e : results) {
                    ListenerConfigCache.updateRegistration(e.getClassification(), e.getTenantId(), ListenerConfigConverter.convert(e.ret$Data()));
                }
            } catch (final NoResultException e) {
                LOGGER.debug("No JPA entity listener configurations found for t9t");
            }
        } catch (final Exception e) {
            LOGGER.error("Could not load JPA entity listener configurations - {}", ExceptionUtil.causeChain(e));
        }
    }
}
