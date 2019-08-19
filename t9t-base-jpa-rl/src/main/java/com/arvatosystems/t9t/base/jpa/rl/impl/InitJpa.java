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
package com.arvatosystems.t9t.base.jpa.rl.impl;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.MappedSuperclass;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jpa.ormspecific.IEMFCustomizer;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;
import com.arvatosystems.t9t.init.InitContainers;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;

@Startup(12000)
public class InitJpa {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitJpa.class);

    public static EntityManagerFactory presetEntityManagerFactory = null;  // can be defined in scenarios inside a Java EE server

    public static void onStartup() throws Exception {
        if (presetEntityManagerFactory == null) {
            EntityManagerFactory emf;
            T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);
            String puName = cfg.getPersistenceUnitName();

            if (cfg.getDatabaseConfiguration() != null) {
                LOGGER.info("Creating customized JPA EMF for PU name {}", puName);
                IEMFCustomizer cst = Jdp.getRequired(IEMFCustomizer.class);
                emf = cst.getCustomizedEmf(puName, cfg.getDatabaseConfiguration());
            } else {
                LOGGER.info("Creating default JPA EMF for PU name {}", puName);
                emf = Persistence.createEntityManagerFactory(puName);
            }
            Jdp.bindInstanceTo(emf, EntityManagerFactory.class);
            Jdp.registerWithCustomProvider(PersistenceProviderJPA.class, new PersistenceProviderJPAProvider(emf));
        } else {
            LOGGER.info("Using preset JPA EMF");
            Jdp.bindInstanceTo(presetEntityManagerFactory, EntityManagerFactory.class);
            Jdp.registerWithCustomProvider(PersistenceProviderJPA.class, new PersistenceProviderJPAProvider(presetEntityManagerFactory));
        }

        // next lines are just for user info
        final Set<Class<?>> mcl = InitContainers.getClassesAnnotatedWith(MappedSuperclass.class);
        for (Class<?> e : mcl)
            LOGGER.info("Found mapped Superclass class {}", e.getCanonicalName());

        final Set<Class<?>> entities = InitContainers.getClassesAnnotatedWith(Entity.class);
        for (Class<?> e : entities)
            LOGGER.info("Found entity class {}", e.getCanonicalName());
    }
}
