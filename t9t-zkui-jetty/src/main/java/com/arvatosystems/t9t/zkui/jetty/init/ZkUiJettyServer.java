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
package com.arvatosystems.t9t.zkui.jetty.init;

import de.jpaw.api.ConfigurationReader;
import de.jpaw.util.ConfigurationReaderFactory;
import org.eclipse.jetty.ee11.webapp.WebAppContext;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.binder.jetty.JettyConnectionMetrics;
import io.micrometer.core.instrument.binder.jetty.JettyServerThreadPoolMetrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class ZkUiJettyServer {
    private boolean enableMetrics = false;
    private final List<AutoCloseable> metricsToClose = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkUiJettyServer.class);

    private static final ConfigurationReader CONFIG_READER = ConfigurationReaderFactory.getConfigReaderForName("t9t.zkui", null);

    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_MIN_THREADS = 4;
    private static final int DEFAULT_MAX_THREADS = 20;
    private static final int DEFAULT_IDLE_TIMEOUT = 5000;
    private static final int DEFAULT_STOP_TIMEOUT = 5000;
    private static final int DEFAULT_CONNECTION_IDLE_TIMEOUT = 300000;
    // Default maximum form content size: 5 MB
    private static final int DEFAULT_MAX_FORM_CONTENT_SIZE = 5 * 1024 * 1024;
    private static final String DEFAULT_CONTEXT_ROOT = "/";

    public static void main(final String[] args) throws Exception {
        boolean enableMetrics = false;
        for (String arg : args) {
            if ("--metrics".equals(arg) || "-M".equals(arg)) {
                enableMetrics = true;
                break;
            }
        }
        try {
            ZkUiJettyServer server = new ZkUiJettyServer();
            server.enableMetrics = enableMetrics;
            server.run();
        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }

    public void run() throws Exception {
        final int port = CONFIG_READER.getIntProperty("jetty.http.port", DEFAULT_PORT);
        final int minThreads = CONFIG_READER.getIntProperty("jetty.threadPool.minThreads", DEFAULT_MIN_THREADS);
        final int maxThreads = CONFIG_READER.getIntProperty("jetty.threadPool.maxThreads", DEFAULT_MAX_THREADS);
        final int idleTimeout = CONFIG_READER.getIntProperty("jetty.threadPool.idleTimeout", DEFAULT_IDLE_TIMEOUT); // in millis
        final int connectionIdleTimeout = CONFIG_READER.getIntProperty("jetty.connection.idleTimeout", DEFAULT_CONNECTION_IDLE_TIMEOUT); // in millis
        final int stopTimeout = CONFIG_READER.getIntProperty("jetty.stopTimeout", DEFAULT_STOP_TIMEOUT); // in millis
        final int maxFormContentSize = CONFIG_READER.getIntProperty("jetty.maxFormContentSize", DEFAULT_MAX_FORM_CONTENT_SIZE); // in bytes
        final String contextPath = CONFIG_READER.getProperty("jetty.contextPath", DEFAULT_CONTEXT_ROOT);

        LOGGER.info("Web server: port {}, context path {}, connection idle timeout {} ms", port, contextPath, connectionIdleTimeout);
        LOGGER.info("Threadpool: {} min threads, {} max threads, idle timeout {} ms", minThreads, maxThreads, idleTimeout);

        final QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        final Server server = new Server(threadPool);

        final HttpConfiguration hconfig = new HttpConfiguration();
        hconfig.setSendServerVersion(false);

        final HttpConnectionFactory http1 = new HttpConnectionFactory(hconfig);
        final ServerConnector connector = new ServerConnector(server, http1);
        connector.setIdleTimeout(connectionIdleTimeout);
        connector.setPort(port);

        final WebAppContext webAppContext = new WebAppContext();
        webAppContext.clearAliasChecks();
        webAppContext.setContextPath(contextPath);
        final URL webAppDir = ZkUiJettyServer.class.getClassLoader().getResource("/webapp");
        final String descriptor = webAppDir + "/WEB-INF/web.xml";
        LOGGER.info("webapp location: {}, descriptor location: {}, stopTimeout: {}", webAppDir, descriptor, stopTimeout);
        webAppContext.setDescriptor(descriptor);
        final Resource resource = ResourceFactory.of(new ResourceHandler()).newResource(webAppDir);
        webAppContext.setBaseResource(resource);
        webAppContext.setMaxFormContentSize(maxFormContentSize);

        if (enableMetrics) {
            ContextHandlerCollection handlerCollection = createMetricsHandler(threadPool, connector, webAppContext);
            server.setHandler(handlerCollection);
            addShutdownHook();
        } else {
            server.setHandler(webAppContext);
        }
        server.addConnector(connector);
        server.setStopTimeout(stopTimeout);
        server.setStopAtShutdown(true);
        server.start();
        LOGGER.info("ZK UI Jetty server started");
        server.join();
    }

    /**
     * Registers a shutdown hook with the JVM to ensure that all resources in the {@code metricsToClose} collection are properly closed when the application is
     * shutting down. Each resource is closed in turn, and any exceptions encountered during the closing process are logged as warnings.
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (AutoCloseable metric : metricsToClose) {
                try {
                    metric.close();
                } catch (Exception e) {
                    LOGGER.warn("Error closing metric: {}", e);
                }
            }
        }));
    }

    /**
     * Creates a {@link ContextHandlerCollection} that includes a Prometheus metrics endpoint and the main web application context.
     * <p>
     * This method sets up Prometheus metrics collection for the Jetty server, including JVM, thread pool, connection, and uptime metrics. It registers various
     * Micrometer metrics binders to a {@link PrometheusMeterRegistry}, and exposes the metrics at the "/metrics" endpoint. The metrics endpoint returns metrics
     * in the Prometheus text format.
     * </p>
     *
     * @param threadPool    the Jetty {@link QueuedThreadPool} to monitor and collect thread pool metrics from
     * @param connector     the Jetty {@link ServerConnector} to monitor and collect connection metrics from
     * @param webAppContext the main {@link WebAppContext} for the application, to be included in the handler collection
     * @return a {@link ContextHandlerCollection} containing the metrics endpoint and the main web application context
     */
    private ContextHandlerCollection createMetricsHandler(final QueuedThreadPool threadPool, final ServerConnector connector,
            final WebAppContext webAppContext) {
        LOGGER.info("Prometheus metrics enabled via --metrics");
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        new ClassLoaderMetrics().bindTo(prometheusRegistry);
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        JvmGcMetrics gcMetrics = new JvmGcMetrics();
        gcMetrics.bindTo(prometheusRegistry);
        metricsToClose.add(gcMetrics);
        new ProcessorMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);
        new UptimeMetrics().bindTo(prometheusRegistry);

        JettyServerThreadPoolMetrics threadPoolMetrics = new JettyServerThreadPoolMetrics(threadPool, Tags.of("server", "jetty"));
        threadPoolMetrics.bindTo(prometheusRegistry);
        metricsToClose.add(threadPoolMetrics);
        JettyConnectionMetrics connectionMetrics = new JettyConnectionMetrics(prometheusRegistry, Tags.of("server", "jetty"));
        connector.addBean(connectionMetrics);

        ContextHandler metricsContext = new ContextHandler("/metrics");
        metricsContext.setHandler(new Handler.Abstract() {
            @Override
            public boolean handle(final Request request, final Response response, final Callback callback) throws Exception {
                response.setStatus(200);
                response.getHeaders().put("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
                response.write(true, java.nio.ByteBuffer.wrap(prometheusRegistry.scrape().getBytes(StandardCharsets.UTF_8)), callback);
                return true;
            }
        });

        ContextHandlerCollection handlerCollection = new ContextHandlerCollection();
        handlerCollection.addHandler(metricsContext);
        handlerCollection.addHandler(webAppContext);
        return handlerCollection;
    }
}
