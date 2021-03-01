/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.plugins.jpa.impl;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.plugins.jpa.entities.LoadedPluginEntity;
import com.arvatosystems.t9t.plugins.services.IPluginManager;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;
import de.jpaw.util.ExceptionUtil;

@Startup(50083) // must be run after the static workflow steps have been initialized (50080), because plugins could contribute with additional steps
public class InitialPluginsLoader implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitialPluginsLoader.class);
    private final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);
    private final IPluginManager pluginManager = Jdp.getRequired(IPluginManager.class);

    /** performs a standard query, but cross tenant. */
    @Override
    public void onStartup() {
        try {
            final PersistenceProviderJPA cp = jpaContextProvider.get();
            final TypedQuery<LoadedPluginEntity> query = cp.getEntityManager().createQuery(
                "SELECT p FROM " + LoadedPluginEntity.class.getSimpleName() + " p WHERE p.isActive = :isActive ORDER BY p.tenantRef, p.priority DESC",
                LoadedPluginEntity.class);
            query.setParameter("isActive", true);
            List<LoadedPluginEntity> plugins = query.getResultList();
            LOGGER.info("Preloading {} plugins", plugins.size());
            for (LoadedPluginEntity pe: plugins) {
                pluginManager.loadPlugin(pe.getTenantRef(), pe.getJarFile());
            }
            cp.getEntityManager().clear();  // get rid of big copies of JAR images inside cached JPA entities
        } catch (Exception e) {
            LOGGER.error("Could not load persisted plugins - {}", ExceptionUtil.causeChain(e));
        }
    }
}
