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
package com.arvatosystems.t9t.vertx.cluster;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.vertx.impl.T9tServer;
import com.arvatosystems.t9t.jdp.Init;
import com.hazelcast.config.XmlConfigBuilder;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public final class VertxCluster {
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxCluster.class);

    private VertxCluster() { }

    public static void main(final String[] args) {
        try {
            final String hostAddress = InetAddress.getLocalHost().getHostAddress();

            T9tServer.configureSystemParameters();

            T9tServer.parseCommandLine(args, (final T9tServer server) -> {
                LOGGER.info(T9tServer.PRINTED_NAME + " (cluster based) starting...");
                LOGGER.info("host address.." + hostAddress);
                server.readConfig();

                // update a possible new location of the config file before we run the startup process
                final HazelcastClusterManager mgr = new HazelcastClusterManager();

                // check hazelcast config file
                final String systemProperty = System.getProperty("vertx.hazelcast.config");
                if (systemProperty != null && !systemProperty.isEmpty()) {
                    LOGGER.info("Attempting system property {}", systemProperty);
                    try {
                        mgr.setConfig(new XmlConfigBuilder(systemProperty).build());
                        LOGGER.info("Hazelcast used from {}", systemProperty);
                    } catch (final FileNotFoundException e) {
                        LOGGER.info("Hazelcast.config file could not be found at {}", systemProperty);
                    }
                }

                Init.initializeT9t();

                final VertxOptions options = new VertxOptions().setClusterManager(mgr);
                final EventBusOptions busOptions = options.getEventBusOptions();
                busOptions.setHost(hostAddress);

                server.checkForMetricsAndInitialize(options);

                Vertx.clusteredVertx(options, new Handler<AsyncResult<Vertx>>() {
                    @Override public void handle(final AsyncResult<Vertx> event) {
                        if (event.succeeded()) {
                            server.deployAndRun(event.result(), null);
                        } else {
                            // failed!
                            LOGGER.error("Could not create clustered vert.x", event.cause());
                        }
                    }
                });
            });
        } catch (final UnknownHostException e) {
            LOGGER.error(T9tServer.PRINTED_NAME + " (cluster based) failed to start, host not found.", e);
        }
    }
}
