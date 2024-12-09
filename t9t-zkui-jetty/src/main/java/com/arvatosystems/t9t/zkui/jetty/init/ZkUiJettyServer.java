package com.arvatosystems.t9t.zkui.jetty.init;

import de.jpaw.api.ConfigurationReader;
import de.jpaw.util.ConfigurationReaderFactory;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class ZkUiJettyServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkUiJettyServer.class);

    private static final ConfigurationReader CONFIG_READER = ConfigurationReaderFactory.getConfigReaderForName("t9t.zkui", null);

    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_MIN_THREADS = 4;
    private static final int DEFAULT_MAX_THREADS = 20;
    private static final int DEFAULT_IDLE_TIMEOUT = 5000;
    private static final int DEFAULT_STOP_TIMEOUT = 5000;
    private static final int DEFAULT_CONNECTION_IDLE_TIMEOUT = 300000;
    private static final String DEFAULT_CONTEXT_ROOT = "/";

    public static void main(final String[] args) throws Exception {
        try {
            new ZkUiJettyServer().run();
        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }

    public void run() throws Exception {
        final int port = CONFIG_READER.getIntProperty("jetty.http.port", DEFAULT_PORT);
        final int minThreads = CONFIG_READER.getIntProperty("jetty.threadPool.minThreads", DEFAULT_MIN_THREADS);
        final int maxThreads = CONFIG_READER.getIntProperty("jetty.threadPool.maxThreads", DEFAULT_MAX_THREADS);
        final int idleTimeout = CONFIG_READER.getIntProperty("jetty.threadPool.idleTimeout", DEFAULT_IDLE_TIMEOUT);  // in millis
        final int connectionIdleTimeout = CONFIG_READER.getIntProperty("jetty.connection.idleTimeout", DEFAULT_CONNECTION_IDLE_TIMEOUT);  // in millis
        final int stopTimeout = CONFIG_READER.getIntProperty("jetty.stopTimeout", DEFAULT_STOP_TIMEOUT);  // in millis
        final String contextPath = CONFIG_READER.getProperty("jetty.contextPath", DEFAULT_CONTEXT_ROOT);
        LOGGER.info("Using configuration values: port {}, min/max threads = {}/{}, threadPool idle timeout = {}, connection idle timeout = {},"
                + " stopTimeout = {}, context = {}", port, minThreads, maxThreads, idleTimeout, connectionIdleTimeout, stopTimeout, contextPath);

        final QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        final Server server = new Server(threadPool);

        final HttpConfiguration hconfig = new HttpConfiguration();
        hconfig.setSendServerVersion(false);

        final HttpConnectionFactory http1 = new HttpConnectionFactory(hconfig);
        final ServerConnector connector = new ServerConnector(server, http1);
        connector.setIdleTimeout(connectionIdleTimeout);
        connector.setPort(port);
        server.addConnector(connector);

        final WebAppContext webAppContext = new WebAppContext();
        server.setHandler(webAppContext);
        webAppContext.setContextPath(contextPath);
        final URL webAppDir = ZkUiJettyServer.class.getClassLoader().getResource("/webapp");
        final String descriptor = webAppDir + "/WEB-INF/web.xml";
        LOGGER.info("webapp location: {}. Descriptor location: {} ", webAppDir, descriptor);
        webAppContext.setDescriptor(descriptor);
        final Resource resource = ResourceFactory.of(new ResourceHandler()).newResource(webAppDir);
        webAppContext.setBaseResource(resource);

        server.setStopTimeout(stopTimeout);
        server.setStopAtShutdown(true);
        server.start();
        LOGGER.info("ZK UI Jetty server started on port {} with contextPath {}", port, contextPath);
        server.join();
    }
}
