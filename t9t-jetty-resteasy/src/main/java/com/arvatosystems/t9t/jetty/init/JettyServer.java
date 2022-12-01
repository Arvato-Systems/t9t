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
package com.arvatosystems.t9t.jetty.init;

import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.rest.utils.RestUtils;

public class JettyServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    static final int DEFAULT_PORT = 8090;
    static final int DEFAULT_MIN_THREADS = 4;
    static final int DEFAULT_MAX_THREADS = 20;
    static final int DEFAULT_IDLE_TIMEOUT = 5000;
    static final String DEFAULT_CONTEXT_ROOT = "/rest";
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
        final String contextRoot     = RestUtils.CONFIG_READER.getProperty("jetty.contextRoot",     DEFAULT_CONTEXT_ROOT);
        final String applicationPath = RestUtils.CONFIG_READER.getProperty("jetty.applicationPath", DEFAULT_APPLICATION_PATH);

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
        final HttpConfiguration hconfig = new HttpConfiguration();
        hconfig.setSendServerVersion(false); // remove the Jetty version from the error pages

        // ... configure
        final HttpConnectionFactory http1 = new HttpConnectionFactory(hconfig);
        final HTTP2CServerConnectionFactory http2c = new HTTP2CServerConnectionFactory(hconfig);
        final ServerConnector connector = new ServerConnector(server, http1, http2c);
        connector.setPort(port);
        server.addConnector(connector);

        // Setup the basic Application "context" at "/".
        // This is also known as the handler tree (in Jetty speak).
        final ServletContextHandler context = new ServletContextHandler(server, contextRoot);

        // Setup RESTEasy's HttpServletDispatcher at "/{applicationPath}/*".
        final ServletHolder restEasyServlet = new ServletHolder(new HttpServletDispatcher());
        restEasyServlet.setInitParameter("resteasy.servlet.mapping.prefix", applicationPath);
        restEasyServlet.setInitParameter("jakarta.ws.rs.Application", ApplicationConfig.class.getCanonicalName());
        context.addServlet(restEasyServlet, applicationPath + "/*");

        // Setup the DefaultServlet at "/".
        final ServletHolder defaultServlet = new ServletHolder(new DefaultServlet());
        context.addServlet(defaultServlet, contextRoot);

        server.start();
        server.join();
    }
}
