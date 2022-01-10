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
package com.arvatosystems.t9t.base.jpa.jta.impl;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;
import com.arvatosystems.t9t.init.InitContainers;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Startup(12000)
public class InitJpa implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitJpa.class);

    @PersistenceContext(unitName = "t9t-DS")
    private EntityManager presetEntityManager;  // HAS TO be defined in scenarios inside a Java EE server

    @Override
    public void onStartup() {
        T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);
        LOGGER.info("Binding preset JPA EM for PU name {}", cfg.getPersistenceUnitName());
        Jdp.bindInstanceTo(presetEntityManager, EntityManager.class);
        Jdp.registerWithCustomProvider(PersistenceProviderJPA.class, new PersistenceProviderJPAProvider(presetEntityManager));
        // next lines are just for user info
        final Set<Class<?>> mcl = InitContainers.getClassesAnnotatedWith(MappedSuperclass.class);
        for (Class<?> e : mcl) {
            LOGGER.info("Found mapped Superclass class {}", e.getCanonicalName());
        }

        final Set<Class<?>> entities = InitContainers.getClassesAnnotatedWith(Entity.class);
        for (Class<?> e : entities) {
            LOGGER.info("Found entity class {}", e.getCanonicalName());
        }
    }
}
