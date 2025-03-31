/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jpa.rl.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.jpa.IPersistenceProviderJPAShadow;
import com.arvatosystems.t9t.base.jpa.ormspecific.IEMFCustomizer;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Startup(12000)
public class InitJpa implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitJpa.class);

    @Override
    public void onStartup() {
        final EntityManagerFactory emf;
        EntityManagerFactory shadowEmf = null;
        IEMFCustomizer cst = null;
        final T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);
        final String defaultPuName = cfg.getPersistenceUnitName();

        // main persistence unit
        final RelationalDatabaseConfiguration dbConfig = cfg.getDatabaseConfiguration();
        if (dbConfig != null) {
            final String puName = T9tUtil.nvl(dbConfig.getPersistenceUnitName(), defaultPuName);
            LOGGER.info("Creating customized JPA EMF for PU name {}", puName);
            cst = Jdp.getRequired(IEMFCustomizer.class);
            try {
                emf = cst.getCustomizedEmf(puName, dbConfig);
            } catch (final Exception e1) {
                LOGGER.error("Exception calling EMF customizer: ", e1);
                throw new RuntimeException(e1);
            }
        } else {
            LOGGER.info("Creating default JPA EMF for PU name {}", defaultPuName);
            emf = Persistence.createEntityManagerFactory(defaultPuName);
        }

        // shadow persistence unit
        final RelationalDatabaseConfiguration shadowDbConfig = cfg.getShadowDatabaseConfig();
        if (shadowDbConfig != null) {
            if (T9tUtil.isBlank(shadowDbConfig.getPersistenceUnitName())) {
                // field is optional to not produce conflict with legacy configuration, but here it is required
                LOGGER.error("Exception during configuration of shadow database: missing persistencUnitName");
                throw new RuntimeException("ATTENTION: configuration of shadow database is not correct");
            }
            final String puName = shadowDbConfig.getPersistenceUnitName();
            LOGGER.info("Creating customized SHADOW JPA EMF for PU name {}", puName);
            cst = cst != null ? cst : Jdp.getRequired(IEMFCustomizer.class);
            try {
                shadowEmf = cst.getCustomizedEmf(puName, shadowDbConfig);
            } catch (final Exception e1) {
                LOGGER.error("Exception calling EMF customizer for shadow EMF: ", e1);
                throw new RuntimeException(e1);
            }
        }

        Jdp.bindInstanceTo(emf, EntityManagerFactory.class); // bind only the main persistence unit
        Jdp.registerWithCustomProvider(PersistenceProviderJPA.class, new PersistenceProviderJPAProvider(emf, shadowEmf));
        // in addition, register an additional provider for readonly access
        Jdp.registerWithCustomProvider(IPersistenceProviderJPAShadow.class, new PersistenceProviderJPAShadowProvider(shadowEmf != null ? shadowEmf : emf));
    }
}
