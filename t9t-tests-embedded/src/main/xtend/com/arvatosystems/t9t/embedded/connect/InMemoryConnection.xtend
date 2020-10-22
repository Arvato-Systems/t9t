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
package com.arvatosystems.t9t.embedded.connect

import com.arvatosystems.t9t.base.jpa.ormspecific.IEMFCustomizer
import com.arvatosystems.t9t.cfg.be.ConfigProvider
import com.arvatosystems.t9t.cfg.be.DatabaseBrandType
import com.arvatosystems.t9t.cfg.be.KeyPrefetchConfiguration
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration
import com.arvatosystems.t9t.orm.jpa.hibernate.impl.EMFCustomizer
import com.arvatosystems.t9t.ssm.be.impl.IQuartzPropertyProvider
import java.util.Map
import org.h2.tools.Server
import java.util.Properties
import de.jpaw.dp.Alternative
import com.arvatosystems.t9t.jdp.Init
import static extension de.jpaw.dp.JdpExtensions.*

/**
 * In-Memory connection which created an in memory db and automatically generates the necessary tables.
 *
 * A connection to the in memory db is possible via web or pgAdmin and might be setup via following environment properties:
 * <ul>
 *  <li>t9t.in-memory.h2.web (default=true) : Enable web console</li>
 *  <li>t9t.in-memory.h2.web-port (default=8082) : Port of web console</li>
 *  <li>t9t.in-memory.h2.pg (default=false) : Enable connection via pgAdmin</li>
 *  <li>t9t.in-memory.h2.pg-port (default=9092) : Port of pgAdmin</li>
 * </ul>
 */
class InMemoryConnection extends AbstractConnection {

    static final Object STATIC_INITIALIZER = {
        val h2args = newLinkedList()

        if ("true".equalsIgnoreCase(System.getProperty("t9t.in-memory.h2.web", "true"))) {
            h2args.add("-web")

            val webPort = System.getProperty("t9t.in-memory.h2.web-port");
            if (webPort !== null) {
                h2args.add('''-webPort «webPort»''')
            }
        }

        if ("true".equalsIgnoreCase(System.getProperty("t9t.in-memory.h2.pg", "false"))) {
            h2args.add("-pg")

            val pgPort = System.getProperty("t9t.in-memory.h2.pg-port");
            if (pgPort !== null) {
                h2args.add('''-pgPort «pgPort»''')
            }
        }

        if (!h2args.isEmpty)
            Server.main(h2args)

        ConfigProvider.configuration => [
            databaseConfiguration = new RelationalDatabaseConfiguration => [
                username                = "fortytwo"
                password                = ""
                databaseBrand           = DatabaseBrandType.H2
                jdbcDriverClass         = "org.h2.Driver"
                jdbcConnectString       = "jdbc:h2:mem:fortytwo;SELECT_FOR_UPDATE_MVCC=FALSE"
            ]
            keyPrefetchConfiguration = new KeyPrefetchConfiguration => [
                strategy                = ""    // Use fallback NoopRefGenerator
            ]
        ]

        Init.initializeT9t [
            IEMFCustomizer         .isNow(new InMemoryEMFCustomizer)
            IQuartzPropertyProvider.isNow(new InMemoryQuartzPropertyProvider)
        ]
        return null
    }

    public static class InMemoryQuartzPropertyProvider implements IQuartzPropertyProvider {

        override getProperties() {
            val properties = new Properties

            properties.put("org.quartz.scheduler.instanceName", "DefaultQuartzScheduler")
            properties.put("org.quartz.scheduler.rmi.export", "false")
            properties.put("org.quartz.scheduler.rmi.proxy", "false")
            properties.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", "false")

            properties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool")
            properties.put("org.quartz.threadPool.threadCount", "10")
            properties.put("org.quartz.threadPool.threadPriority", "5")
            properties.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true")

            properties.put("org.quartz.jobStore.misfireThreshold", "60000")

            // only in-memory
            properties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore")

            return properties
        }
    }

    @Alternative
    static class InMemoryEMFCustomizer extends EMFCustomizer {

        override protected configureProperties(Map<String, Object> properties) {
            super.configureProperties(properties)
            // autogenerate db schema
            properties.put("hibernate.hbm2ddl.auto", "create")
        }
    }

    new() {
        auth(INITIAL_USER_ID, INITIAL_PASSWORD)
    }

    new(String userId, String password) {
        auth(userId, password)
    }
}
