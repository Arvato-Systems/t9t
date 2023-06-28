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
package com.arvatosystems.t9t.base.jpa.rl.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jpa.ormspecific.IEMFCustomizer;
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
        final T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);
        final String puName = cfg.getPersistenceUnitName();

        if (cfg.getDatabaseConfiguration() != null) {
            LOGGER.info("Creating customized JPA EMF for PU name {}", puName);
            final IEMFCustomizer cst = Jdp.getRequired(IEMFCustomizer.class);
            try {
                emf = cst.getCustomizedEmf(puName, cfg.getDatabaseConfiguration());
            } catch (final Exception e1) {
                LOGGER.error("Exception calling EMF customizer: ", e1);
                throw new RuntimeException(e1);
            }
        } else {
            LOGGER.info("Creating default JPA EMF for PU name {}", puName);
            emf = Persistence.createEntityManagerFactory(puName);
        }
        Jdp.bindInstanceTo(emf, EntityManagerFactory.class);
        Jdp.registerWithCustomProvider(PersistenceProviderJPA.class, new PersistenceProviderJPAProvider(emf));
    }
}
