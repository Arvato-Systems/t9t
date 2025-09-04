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
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class McpJettyServer {
    private boolean enableMetrics = false;
    private final List<AutoCloseable> metricsToClose = new ArrayList<>();

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
        boolean enableMetrics = false;
        for (String arg : args) {
            if ("--metrics".equals(arg) || "-M".equals(arg)) {
                enableMetrics = true;
                break;
            }
        }
        try {
            McpJettyServer server = new McpJettyServer();
            server.enableMetrics = enableMetrics;
            server.run();
        } catch (final Throwable t) {
            LOGGER.error("Exception in main method of McpJettyServer: ", t);
        }
    }

    public void run() throws Exception {
        final int port = CONFIG_READER.getIntProperty("jetty.http.port", DEFAULT_PORT);
        final int minThreads = CONFIG_READER.getIntProperty("jetty.threadPool.minThreads", DEFAULT_MIN_THREADS);
        final int maxThreads = CONFIG_READER.getIntProperty("jetty.threadPool.maxThreads", DEFAULT_MAX_THREADS);
        final int idleTimeout = CONFIG_READER.getIntProperty("jetty.threadPool.idleTimeout", DEFAULT_IDLE_TIMEOUT); // in millis
        final int connectionIdleTimeout = CONFIG_READER.getIntProperty("jetty.connection.idleTimeout", DEFAULT_CONNECTION_IDLE_TIMEOUT); // in millis
        final int stopTimeout = CONFIG_READER.getIntProperty("jetty.stopTimeout", DEFAULT_STOP_TIMEOUT); // in millis
        final String contextPath = CONFIG_READER.getProperty("jetty.contextPath", DEFAULT_CONTEXT_ROOT);

        LOGGER.info("MCP server: port {}, context path {}, connection idle timeout {} ms, stop timeout {} ms", port, contextPath, connectionIdleTimeout,
                stopTimeout);
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

        if (enableMetrics) {
            ContextHandlerCollection handlerCollection = createMetricsHandler(threadPool, connector, context);
            server.setHandler(handlerCollection);
            addShutdownHook();
        } else {
            server.setHandler(context);
        }

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
     *
     * This method sets up Prometheus metrics collection for the Jetty server, including JVM, thread pool, connection, and uptime metrics. It registers various
     * Micrometer metrics binders to a {@link PrometheusMeterRegistry}, and exposes the metrics at the "/metrics" endpoint. The metrics endpoint returns metrics
     * in the Prometheus text format.
     *
     * @param threadPool     the Jetty {@link QueuedThreadPool} to monitor and collect thread pool metrics from
     * @param connector      the Jetty {@link ServerConnector} to monitor and collect connection metrics from
     * @param servletContext the main {@link ServletContextHandler} for the application, to be included in the handler collection
     * @return a {@link ContextHandlerCollection} containing the metrics endpoint and the main web application context
     */
    private ContextHandlerCollection createMetricsHandler(final QueuedThreadPool threadPool, final ServerConnector connector,
            final ServletContextHandler servletContext) {
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
        handlerCollection.addHandler(servletContext);
        return handlerCollection;
    }
}
