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
package com.arvatosystems.t9t.jetty.init;

import java.util.Map;

import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    static final Integer DEFAULT_PORT = 8090;
    static final Integer DEFAULT_MIN_THREADS = 4;
    static final Integer DEFAULT_MAX_THREADS = 20;
    static final Integer DEFAULT_IDLE_TIMEOUT = 5000;
    static final String DEFAULT_CONTEXT_ROOT = "/rest";
    static final String DEFAULT_APPLICATION_PATH = "";

    public static void main( String[] args ) throws Exception {
//        System.setProperty("java.util.concurrent.ForkJoinPool.common.threadFactory", DirtyHackForkJoinPool.class.getCanonicalName());

        System.setProperty("javax.xml.bind.JAXBContextFactory", "com.sun.xml.bind.v2.ContextFactory");   // do not access the "internal" ContextFactory // UPDATE FOR JAKARTA!

        try {
            new JettyServer().run();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static String getContextPath() {
        return ConfigProvider.getConfig().getOptionalValue("jetty.contextRoot", String.class).orElse(DEFAULT_CONTEXT_ROOT);
    }

    public static String getApplicationPath() {
        return ConfigProvider.getConfig().getOptionalValue("jetty.applicationPath", String.class).orElse(DEFAULT_APPLICATION_PATH);
    }

    public void run() throws Exception {

        Config config = ConfigProvider.getConfig();
        for (ConfigSource cfgSrc: config.getConfigSources()) {
            Map<String,String> values = cfgSrc.getProperties();
            LOGGER.info("Have config source of prio {}: {} with {} values", cfgSrc.getOrdinal(), cfgSrc.getName(), values.size());
        }

        int port = config.getOptionalValue("jetty.http.port", Integer.class).orElse(DEFAULT_PORT);
        int minThreads = config.getOptionalValue("jetty.threadPool.minThreads", Integer.class).orElse(DEFAULT_MIN_THREADS);
        int maxThreads = config.getOptionalValue("jetty.threadPool.maxThreads", Integer.class).orElse(DEFAULT_MAX_THREADS);
        int idleTimeout = config.getOptionalValue("jetty.threadPool.idleTimeout", Integer.class).orElse(DEFAULT_IDLE_TIMEOUT);  // in millis
        String contextRoot = getContextPath();
        String applicationPath = getApplicationPath();

        LOGGER.info("Using the following configuration values: port {}, min/max threads = {}/{}", port, minThreads, maxThreads);
        LOGGER.info("  idle timeout = {}, context = {}, application path = {}", idleTimeout, contextRoot, applicationPath);

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
        HttpConfiguration hconfig = new HttpConfiguration();
        // ... configure
        HttpConnectionFactory http1 = new HttpConnectionFactory(hconfig);
        HTTP2CServerConnectionFactory http2c = new HTTP2CServerConnectionFactory(hconfig);
        ServerConnector connector = new ServerConnector(server, http1, http2c);
        connector.setPort(port);
        server.addConnector(connector);

        // Setup the basic Application "context" at "/".
        // This is also known as the handler tree (in Jetty speak).
        final ServletContextHandler context = new ServletContextHandler(server, contextRoot);

        // Setup RESTEasy's HttpServletDispatcher at "/{applicationPath}/*".
        final ServletHolder restEasyServlet = new ServletHolder(new HttpServletDispatcher());
        restEasyServlet.setInitParameter("resteasy.servlet.mapping.prefix", applicationPath);
        restEasyServlet.setInitParameter("javax.ws.rs.Application", ApplicationConfig.class.getCanonicalName());
        context.addServlet(restEasyServlet, applicationPath + "/*");

        // Setup the DefaultServlet at "/".
        final ServletHolder defaultServlet = new ServletHolder(new DefaultServlet());
        context.addServlet(defaultServlet, contextRoot);

        server.start();
        server.join();
    }
}
