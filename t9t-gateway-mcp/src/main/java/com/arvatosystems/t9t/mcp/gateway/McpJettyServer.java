package com.arvatosystems.t9t.mcp.gateway;

import com.arvatosystems.t9t.jackson.JacksonTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jpaw.api.ConfigurationReader;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ConfigurationReaderFactory;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McpJettyServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpJettyServer.class);

    private static final ConfigurationReader CONFIG_READER = ConfigurationReaderFactory.getConfigReaderForName("t9t.mcp", null);

    private static final int DEFAULT_PORT = 9094;
    private static final int DEFAULT_MIN_THREADS = 4;
    private static final int DEFAULT_MAX_THREADS = 20;
    private static final int DEFAULT_IDLE_TIMEOUT = 5000;
    private static final int DEFAULT_STOP_TIMEOUT = 5000;
    private static final int DEFAULT_CONNECTION_IDLE_TIMEOUT = 300000;
    private static final String DEFAULT_CONTEXT_ROOT = "/";

    public static void main(final String[] args) throws Exception {
        try {
            new McpJettyServer().run();
        } catch (final Throwable t) {
            LOGGER.error("Exception in main method of McpJettyServer: ", t);
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

        LOGGER.info("MCP server: port {}, context path {}, connection idle timeout {} ms, stop timeout {} ms", port, contextPath, connectionIdleTimeout, stopTimeout);
        LOGGER.info("Threadpool: {} min threads, {} max threads, idle timeout {} ms", minThreads, maxThreads, idleTimeout);

        final QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        final Server server = new Server(threadPool);

        final HttpConfiguration hconfig = new HttpConfiguration();
        hconfig.setSendServerVersion(false);

        final HttpConnectionFactory http1 = new HttpConnectionFactory(hconfig);
        final ServerConnector connector = new ServerConnector(server, http1);
        connector.setIdleTimeout(connectionIdleTimeout);
        connector.setPort(port);
        server.addConnector(connector);

        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        final ObjectMapper mapper = JacksonTools.createObjectMapper();
        final HttpServletSseServerTransportProvider transportProvider = new HttpServletSseServerTransportProvider(mapper, "/sse");

        context.addServlet(new ServletHolder(transportProvider), "/*");
        final T9tInitializer initializer = new T9tInitializer();
        context.addEventListener(initializer);

        server.setHandler(context);

        final McpSyncServer syncServer = initializer.initMcpServer(transportProvider);

        server.setStopTimeout(stopTimeout);
        server.setStopAtShutdown(true);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread("mcp-gateway-shutdown") {
            @Override
            public void run() {
                LOGGER.info("Gracefully shutting down the mcp gateway");
                try {
                    syncServer.closeGracefully();
                } catch (final Exception e) {
                    LOGGER.error("Exception stopping Jetty: ", e);
                }
                LOGGER.info("Initiating JDP shutdown sequence (service shutdown)");
                Jdp.shutdown();
                LOGGER.info("Normal end t9t Jetty MCP gateway");
            }
        });

        LOGGER.info("t9t Jetty MCP gateway started");
        server.join();
    }
}
