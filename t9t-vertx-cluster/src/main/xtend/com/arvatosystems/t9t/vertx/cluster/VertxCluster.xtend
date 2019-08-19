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
package com.arvatosystems.t9t.vertx.cluster

import com.arvatosystems.t9t.base.vertx.impl.T9tServer
import com.arvatosystems.t9t.jdp.Init
import com.hazelcast.config.XmlConfigBuilder
import de.jpaw.annotations.AddLogger
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import java.io.FileNotFoundException
import java.net.InetAddress

/**
 * Copy of the previous T9tServer class. This class offers a main method which launches the T9tServer in a clustered environment.
 * It attempts to read a file from the System property vertx.hazelcast.config to configure that cluster.
 * System properties can be set with java -Dvertx.hazelcast.config=PATH.
 */
@AddLogger
class VertxCluster {

    def static void main(String[] args) {
        LOGGER.info('''t9t vert.x cluster based server starting...''')
        LOGGER.info("host address.."+InetAddress.localHost.hostAddress);

        System.setProperty("org.jboss.logging.provider", "slf4j"); // configure hibernate to use slf4j
        // System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
        // System.setProperty("vertx.disableFileCaching", "true");              // disable caching of resources in .vertx (for development)

        T9tServer.parseCommandLine(args) // need to call the T9tServer commandline parsing because its port environment will be used for the vertx server
        T9tServer.readConfig()

        val mgr = new HazelcastClusterManager // update a possible new location of the config file before we run the startup process

        // check hazelcast config file
        val systemProperty = System.getProperty("vertx.hazelcast.config")
        if (!systemProperty.nullOrEmpty) {
                LOGGER.info("Attempting system property {}", systemProperty)
            try {
                mgr.config = new XmlConfigBuilder(systemProperty).build
                LOGGER.info("Hazelcast used from {}", systemProperty)
            } catch (FileNotFoundException e) {
                LOGGER.info("Hazelcast.config file could not be found at {}", systemProperty)
            }
        }

        Init.initializeT9t

        val options = new VertxOptions().setClusterManager(mgr)
        options.clusterHost = InetAddress.localHost.hostAddress;
        options.clustered = true
        T9tServer.mergePoolSizes(options)

        Vertx.clusteredVertx(
            options,
            new Handler<AsyncResult<Vertx>>() {
                override handle(AsyncResult<Vertx> event) {
                    if (event.succeeded()) {
                        T9tServer.deployAndRun(event.result, null)
                    } else {
                        // failed!
                        LOGGER.error("Could not create clustered vert.x", event.cause)
                    }
                }
            }
        );
    }
}
