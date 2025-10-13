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
package com.arvatosystems.t9t.jetty.init;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.jetty.ee11.servlet.DefaultServlet;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.http.UriCompliance.Violation;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IKafkaRequestTransmitter;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.jetty.statistics.T9tJettyStatisticsCollector;
import com.arvatosystems.t9t.rest.utils.RestUtils;

import de.jpaw.dp.Jdp;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import jakarta.annotation.Nonnull;

public class JettyServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    static final int DEFAULT_PORT = 8090;
    static final int DEFAULT_MIN_THREADS = 4;
    static final int DEFAULT_MAX_THREADS = 20;
    static final int DEFAULT_IDLE_TIMEOUT = 5000;
    static final int DEFAULT_STOP_TIMEOUT = 5000;
    static final int DEFAULT_CONNECTION_IDLE_TIMEOUT = 300000;
    static final String DEFAULT_CONTEXT_ROOT = "/rest";
    static final String DEFAULT_METRICS_PATH = "/metrics";
    static final String DEFAULT_APPLICATION_PATH = "";

    public static void main(final String[] args) throws Exception {
        // do not access the "internal" ContextFactory (essential for Jakarta 8, fatal for Jakarta 9.1ff)
        // System.setProperty("jakarta.xml.bind.JAXBContextFactory", "com.sun.xml.bind.v2.ContextFactory");

        try {
            new JettyServer().run();
        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }

    public static String getContextPath() {
        return RestUtils.CONFIG_READER.getProperty("jetty.contextRoot", DEFAULT_CONTEXT_ROOT);
    }

    public static String getApplicationPath() {
        return RestUtils.CONFIG_READER.getProperty("jetty.applicationPath", DEFAULT_APPLICATION_PATH);
    }

    public void run() throws Exception {

        final int port        = RestUtils.CONFIG_READER.getIntProperty("jetty.http.port",              DEFAULT_PORT);
        final int minThreads  = RestUtils.CONFIG_READER.getIntProperty("jetty.threadPool.minThreads",  DEFAULT_MIN_THREADS);
        final int maxThreads  = RestUtils.CONFIG_READER.getIntProperty("jetty.threadPool.maxThreads",  DEFAULT_MAX_THREADS);
        final int idleTimeout = RestUtils.CONFIG_READER.getIntProperty("jetty.threadPool.idleTimeout", DEFAULT_IDLE_TIMEOUT);  // in millis
        final int connectionIdleTimeout = RestUtils.CONFIG_READER.getIntProperty("jetty.connection.idleTimeout", DEFAULT_CONNECTION_IDLE_TIMEOUT); // in millis
        final int stopTimeout = RestUtils.CONFIG_READER.getIntProperty("jetty.stopTimeout",            DEFAULT_STOP_TIMEOUT);  // in millis
        final String contextRoot     = RestUtils.CONFIG_READER.getProperty("jetty.contextRoot",        DEFAULT_CONTEXT_ROOT);
        final String applicationPath = RestUtils.CONFIG_READER.getProperty("jetty.applicationPath",    DEFAULT_APPLICATION_PATH);
        final String metricsPath     = RestUtils.CONFIG_READER.getProperty("jetty.metricsPath",        DEFAULT_METRICS_PATH);
        final String uriCompliance   = RestUtils.CONFIG_READER.getProperty("jetty.httpConfig.uriCompliance", null);

        LOGGER.info("Using the following configuration values: port {}, min/max threads = {}/{}", port, minThreads, maxThreads);
        LOGGER.info("  idle timeout = {}, connection idle timeout = {}, context = {}, application path = {}", idleTimeout, connectionIdleTimeout, contextRoot, applicationPath);
        LOGGER.info("  URI compliance is {}", uriCompliance == null ? "NOT set" : "set to " + uriCompliance);

//        // no way to pass it in...
//        final AtomicInteger threadCounter = new AtomicInteger();
//        int executorThreadPoolSize = 10;
//        final ExecutorService executorService = Executors.newFixedThreadPool(executorThreadPoolSize, (r) -> {
//            final String threadName = "t9t-jetty-" + threadCounter.incrementAndGet();
//            LOGGER.info("Launching thread {} of {} for asynchronous http response processing", threadName, executorThreadPoolSize);
//            return new Thread(r, threadName);
//        });

        final QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        final Server server = new Server(threadPool);

        // code below is for adding http/2 (h2c). Works, but also throws sporadic exceptions
        final HttpConfiguration hconfig = new HttpConfiguration();
        hconfig.setSendServerVersion(false); // remove the Jetty version from the error pages
        if (!T9tUtil.isBlank(uriCompliance)) {
            hconfig.setUriCompliance(UriCompliance.from(getAllowedViolations(uriCompliance)));
        }

        // ... configure
        final HttpConnectionFactory http1 = new HttpConnectionFactory(hconfig);
        final HTTP2CServerConnectionFactory http2c = new HTTP2CServerConnectionFactory(hconfig);
        final ServerConnector connector = new ServerConnector(server, http1, http2c);
        connector.setIdleTimeout(connectionIdleTimeout);
        connector.setPort(port);
        server.addConnector(connector);

        // Setup the basic Application "context" at "/".
        // This is also known as the handler tree (in Jetty speak).
        final ServletContextHandler context = new ServletContextHandler(contextRoot);

        // Setup RESTEasy's HttpServletDispatcher at "/{applicationPath}/*".
        final ServletHolder restEasyServlet = new ServletHolder(new HttpServletDispatcher());
        restEasyServlet.setInitParameter("resteasy.servlet.mapping.prefix", applicationPath);
        restEasyServlet.setInitParameter("jakarta.ws.rs.Application", ApplicationConfig.class.getCanonicalName());
        context.addServlet(restEasyServlet, applicationPath + "/*");

        // Setup the DefaultServlet at "/".
        final ServletHolder defaultServlet = new ServletHolder(new DefaultServlet());
        context.addServlet(defaultServlet, contextRoot);
        server.setHandler(context);

        // metrics for prometheus
        final StatisticsHandler statisticsHandler = new StatisticsHandler();
        statisticsHandler.setHandler(server.getHandler());
        server.setHandler(statisticsHandler);
        new T9tJettyStatisticsCollector(statisticsHandler).register();
        DefaultExports.register(CollectorRegistry.defaultRegistry);
        context.addServlet(new ServletHolder(new MetricsServlet()), metricsPath);

        // Add graceful shutdown hook
        server.setStopTimeout(stopTimeout);
        server.setStopAtShutdown(true);

        server.start();

        // A hook that initiates a graceful shutdown if the server has been stopped via SIGTERM / SIGINT
        Runtime.getRuntime().addShutdownHook(new Thread("t9t-gateway-shutdown") {
            @Override
            public void run() {
                LOGGER.info("Gracefully shutting down the gateway");
                try {
                    final IKafkaRequestTransmitter kafkaTransmitter = Jdp.getOptional(IKafkaRequestTransmitter.class);
                    if (kafkaTransmitter != null && kafkaTransmitter.initialized()) {
                        LOGGER.info("Shutting down kafka transmitter");
                        // if we use kafka, flush any pending output after half a second (wait for jetty stop hook to be processed)
                        Thread.sleep(500L);
                        kafkaTransmitter.initiateShutdown();
                        Thread.sleep(1000L);
                        kafkaTransmitter.shutdown();
                        Thread.sleep(1000L);
                    } else {
                        Thread.sleep(3000L); // wait for any pending request to have completed
                    }
                    // sync kafka....
                } catch (final Exception e) {
                    LOGGER.error("Exception stopping Jetty: ", e);
                }
                LOGGER.info("Initiating JDP shutdown sequence (service shutdown)");
                Jdp.shutdown();
                LOGGER.info("Normal end Jetty based t9t gateway");
            }
        });

        server.join();
    }

    private Set<UriCompliance.Violation> getAllowedViolations(@Nonnull final String setting) {
        final Set<UriCompliance.Violation> violations = EnumSet.noneOf(UriCompliance.Violation.class);
        final String[] components = setting.split(",");
        for (int i = 0; i < components.length; ++i) {
            switch (components[i]) {
            case "AMBIGUOUS_PATH_SEGMENT":
                violations.add(Violation.AMBIGUOUS_PATH_SEGMENT);
                break;
            case "AMBIGUOUS_EMPTY_SEGMENT":
                violations.add(Violation.AMBIGUOUS_EMPTY_SEGMENT);
                break;
            case "AMBIGUOUS_PATH_SEPARATOR":
                violations.add(Violation.AMBIGUOUS_PATH_SEPARATOR);
                break;
            case "AMBIGUOUS_PATH_PARAMETER":
                violations.add(Violation.AMBIGUOUS_PATH_PARAMETER);
                break;
            case "AMBIGUOUS_PATH_ENCODING":
                violations.add(Violation.AMBIGUOUS_PATH_ENCODING);
                break;
            case "UTF16_ENCODINGS":
                violations.add(Violation.UTF16_ENCODINGS);
                break;
            case "BAD_UTF8_ENCODING":
                violations.add(Violation.BAD_UTF8_ENCODING);
                break;
            case "SUSPICIOUS_PATH_CHARACTERS":
                violations.add(Violation.SUSPICIOUS_PATH_CHARACTERS);
                break;
            case "ILLEGAL_PATH_CHARACTERS":
                violations.add(Violation.ILLEGAL_PATH_CHARACTERS);
                break;
            case "USER_INFO":
                violations.add(Violation.USER_INFO);
                break;
            default:
                LOGGER.error("Unknown URI compliance violation: {}, ignoring it", components[i]);
            }
        }
        LOGGER.info("%d allowed URI compliance violations parsed", violations.size());
        return violations;
    }
}
