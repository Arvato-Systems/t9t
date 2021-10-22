package com.arvatosystems.t9t.vertx.cluster;

import com.arvatosystems.t9t.base.vertx.impl.T9tServer;
import com.arvatosystems.t9t.jdp.Init;
import com.hazelcast.config.XmlConfigBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class VertxCluster {
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxCluster.class);

    public static void main(String[] args) {
        try {
            final String hostAddress = InetAddress.getLocalHost().getHostAddress();

            T9tServer.configureSystemParameters();

            T9tServer.parseCommandLine(args, (T9tServer server) -> {
                LOGGER.info("t9t vert.x cluster based server starting...");
                LOGGER.info("host address.." + hostAddress);
                server.readConfig();

                HazelcastClusterManager mgr = new HazelcastClusterManager(); // update a possible new location of the config file before we run the startup process

                // check hazelcast config file
                String systemProperty = System.getProperty("vertx.hazelcast.config");
                if (systemProperty != null && !systemProperty.isEmpty()) {
                    LOGGER.info("Attempting system property {}", systemProperty);
                    try {
                        mgr.setConfig(new XmlConfigBuilder(systemProperty).build());
                        LOGGER.info("Hazelcast used from {}", systemProperty);
                    } catch (FileNotFoundException e) {
                        LOGGER.info("Hazelcast.config file could not be found at {}", systemProperty);
                    }
                }

                Init.initializeT9t();

                VertxOptions options = new VertxOptions().setClusterManager(mgr);
                EventBusOptions busOptions = options.getEventBusOptions();
                busOptions.setHost(hostAddress);

                server.checkForMetricsAndInitialize(options);

                Vertx.clusteredVertx(options, new Handler<AsyncResult<Vertx>>() {
                    @Override public void handle(AsyncResult<Vertx> event) {
                        if (event.succeeded()) {
                            server.deployAndRun(event.result(), null);
                        } else {
                            // failed!
                            LOGGER.error("Could not create clustered vert.x", event.cause());
                        }
                    }
                });
            });
        } catch (UnknownHostException e) {
            LOGGER.error("t9t vert.x cluster based server failed to start, host not found.", e);
        }
    }
}
