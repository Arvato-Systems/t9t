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
package com.arvatosystems.t9t.base.vertx.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.jwt.JWT;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.vertx.IRestModule;
import com.arvatosystems.t9t.base.vertx.IServiceModule;
import com.arvatosystems.t9t.base.vertx.IVertxMetricsProvider;
import com.arvatosystems.t9t.base.vertx.MultiThreadMessageCoderFactory2;
import com.arvatosystems.t9t.cfg.be.ApplicationConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.ServerConfiguration;
import com.arvatosystems.t9t.cfg.be.StatusProvider;
import com.arvatosystems.t9t.jdp.Init;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.util.DeprecationWarner;
import de.jpaw.dp.Jdp;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.UnmatchedArgumentException;

public class T9tServer extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tServer.class);
    public static final long MORE_THAN_ONE_YEAR = 500L * 86400L * 1000L;     // JVM update due at least once per year (at least every 500 days)
    public static final String PRINTED_NAME = "t9t vert.x server";

    private IVertxMetricsProvider metricsProvider = null;
    private final IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory
      = new MultiThreadMessageCoderFactory2(BonaPortable.class, ServiceResponse.class);

    @Option(names = { "--filePath", "-f" }, description = "path to serve files for /fs from")
    private String filePath;

    @Option(names = { "--uploadsPath", "-U" }, description = "path to store uploaded files in (for example file-uploads)")
    private String uploadsPath;

    @Option(names = { "--cfg", "-c" }, description = "configuration filename")
    private String cfgFile;

    @Option(names = { "--port", "-p" }, defaultValue = "8024", description = "listener port http (0 to disable)")
    private int port;

    @Option(names = { "--tcpport", "-P" }, description = "listener port for plain socket (0 to disable)")
    private int tcpPort;

    @Option(names = { "--restport", "-R" }, description = "listener port for REST (Jakarta-RS) (0 to disable)")
    private int restPort;

    @Option(names = { "--native", "-N" }, description = "prefer Netty's native transort (epoll / unix common)")
    private boolean preferNative;

    @Option(names = { "--corsParm", "-C" }, defaultValue = "*", description = "parameter to the CORS handler")
    private String corsParm;

    @Option(names = { "--static", "-W" }, description = "activate static handler of path /static")
    private boolean allowStatic;

    @Option(names = { "--cors", "-X" }, description = "activate CORS handler")
    private boolean cors;

    @Option(names = { "--metrics", "-M" }, description = "activate (micro)metrics handler")
    private boolean metrics;

    @Option(names = { "--migrateDb", "-m" }, description = "Flag whether to migrate db")
    private boolean migrateDb;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpRequested;

    // autodetect the port assigned on AWS
    public void portAutoDetect() {
        try {
            final String portBySysProp = System.getProperty("t9t.port.http");
            if (portBySysProp != null) {
                LOGGER.info("Found system property for PORT: t9t.port.http {}", portBySysProp);
                port = Integer.parseInt(portBySysProp);
            } else {
                final String portByEnv = System.getenv("PORT");
                if (portByEnv != null) {
                    LOGGER.info("Found environment spec for PORT: {}, assuming we run on AWS Elastic Beanstalk", portByEnv);
                    port = Integer.parseInt(portByEnv);
                } else {
                    LOGGER.info("No PORT environment setting found, assuming we are not on AWS, using port {}", port);
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Exception {}: {} while retrieving PORT setting", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void start() throws Exception {
        super.start();

        // FIXME: bonaparte: Write to static field de.jpaw.bonaparte.util.DeprecationWarner.setWarner from instance method
        DeprecationWarner.setWarner = null;  // no noise in the logs on the server side - warnings are primarily intended for clients!

        final List<IServiceModule> modules = Jdp.getOneInstancePerQualifier(IServiceModule.class);
        Collections.sort(modules);

        String moduleNames = "";
        for (final IServiceModule module: modules) {
            if (moduleNames.isEmpty()) {
                moduleNames += module.getModuleName();
            } else {
                moduleNames += ", " + module.getModuleName();
            }
        }

        LOGGER.info(PRINTED_NAME + " started, modules found are: " + moduleNames);
        final String uploadsFolder = uploadsPath == null ? null : Jdp.getRequired(IFileUtil.class).getAbsolutePath(uploadsPath);
        LOGGER.info("Pathname for file uploads will be {}, CORS is {}, metrics is {}", T9tUtil.nvl(uploadsFolder, "(DISABLED)"), cors, metrics);

        if (port > 0) {
            final Router router = Router.router(vertx);
            // must be before any possible execBlocking, see https://github.com/vert-x3/vertx-web/issues/198
            final BodyHandler bodyHandler = uploadsFolder == null ? BodyHandler.create(false) : BodyHandler.create(uploadsFolder);
            router.route().handler(bodyHandler.setBodyLimit(16777100));

            // register the web paths of the injected modules
            for (final IServiceModule module : modules) {
                module.mountRouters(router, vertx, coderFactory);
            }

            if (allowStatic) {
                LOGGER.info("Activating static file handler from /static to path web");
                final StaticHandler staticHandler = StaticHandler.create();
                staticHandler.setWebRoot("web");
                staticHandler.setFilesReadOnly(true);
                staticHandler.setAllowRootFileSystemAccess(false);
                staticHandler.setMaxAgeSeconds(12 * 60 * 60); // 12 hours (1 working day)
                router.route("/static/*").handler(staticHandler);
            }

            if (filePath != null) {
                LOGGER.info("Also serving static files from {}", filePath);
                final StaticHandler filePathHandler = StaticHandler.create();
                filePathHandler.setWebRoot(filePath);
                filePathHandler.setFilesReadOnly(false);
                filePathHandler.setCachingEnabled(false);
                filePathHandler.setAllowRootFileSystemAccess(true);
                filePathHandler.setMaxAgeSeconds(5); // no caching while testing
                router.route("/fs/*").handler(filePathHandler);
            }

            if (metricsProvider != null) {
                router.route("/metrics").handler(metricsProvider.getMetricsHandler());
            }

            if (cors) {
                LOGGER.info("Setting up CORS handler for origin {}", corsParm);
                router.route().handler(CorsHandler.create(corsParm)
                        .allowedMethod(HttpMethod.GET)
                        .allowedMethod(HttpMethod.POST)
                        .allowedMethod(HttpMethod.OPTIONS)
                        .allowedHeader(HttpHeaders.CONTENT_TYPE.toString())
                        .allowedHeader(HttpHeaders.ACCEPT.toString())
                        .allowedHeader("Charset")
                        .allowedHeader(HttpHeaders.ACCEPT_CHARSET.toString())
                        .allowedHeader(HttpHeaders.CONTENT_LENGTH.toString())
                        .allowedHeader(HttpHeaders.AUTHORIZATION.toString())
                );
            }

            router.get("/ping").handler((final RoutingContext it) -> {
                it.response().setStatusMessage("OK");
                it.response().setStatusCode(200);
                it.response().end();
            });

            router.get("/favicon.ico").handler((final RoutingContext it) -> {
                LOGGER.debug("favicon requested");
                it.response().sendFile("web/favicon.ico");
            });

            // no matching path or method
            router.route().handler((final RoutingContext it) -> {
                final String errorMsg = "Request method " + it.request().method().toString() + " for path " + it.request().path() + " not supported";
                LOGGER.info(errorMsg);
                it.response().setStatusMessage(errorMsg);
                it.response().setStatusCode(404);
                it.response().end();
            });

            LOGGER.info("Listening on http port {}", port);
            final HttpServer httpServer = vertx.createHttpServer();
            httpServer.requestHandler((final HttpServerRequest it) -> router.handle(it));
            httpServer.listen(port);
        }

        if (tcpPort > 0) {
            // compact format (requires ServiceRequest wrapper)
            LOGGER.info("Listening on TCP port {} (low level socket I/O)", tcpPort);
            final NetServer netServer = vertx.createNetServer();
            netServer.connectHandler((final NetSocket it) -> new TcpSocketHandler(it));
            netServer.listen(tcpPort);
        }

        Jdp.bindInstanceTo(vertx, Vertx.class);
        Jdp.bindInstanceTo(vertx.eventBus(), EventBus.class);
        if (restPort > 0) {
            Jdp.getRequired(IRestModule.class).createRestServer(vertx, restPort);
        }
        AsyncProcessor.register(vertx);
    }

    /** Reads some external configuration file, updates the JWT with the keystore, if required. */
    public void readConfig() {
        // require this in other modules
        ConfigProvider.readConfiguration(cfgFile);
        final ServerConfiguration serverCfg = ConfigProvider.getConfiguration().getServerConfiguration();
        if (serverCfg != null && (serverCfg.getKeyStorePassword() != null || serverCfg.getKeyStorePath() != null)) {
            JWT.setKeyStore(serverCfg.getKeyStorePath(), serverCfg.getKeyStorePassword(), serverCfg.getKeyStorePath() == null);
            LOGGER.info("Using environment specific keystore and/or password from local config file. Good.");
        } else {
            LOGGER.warn("No environment specific keystore parameters. Using defaults, do not use this in production!");
        }
    }

    public void mergePoolSizes(final VertxOptions options) {
        final ApplicationConfiguration configs = ConfigProvider.getConfiguration().getApplicationConfiguration();
        if (configs != null) {
            if (configs.getWorkerPoolSize() != null) {
                options.setWorkerPoolSize(configs.getWorkerPoolSize());
                LOGGER.info("Setting vert.x worker pool size to {} via configuration", configs.getWorkerPoolSize());
            }
            if (configs.getEventLoopPoolSize() != null) {
                options.setEventLoopPoolSize(configs.getEventLoopPoolSize());
                LOGGER.info("Setting vert.x event loop pool size to {} via configuration", configs.getEventLoopPoolSize());
            }
            if (configs.getMaxWorkerExecuteTime() != null) {
                final Long maxWorkerExecuteTime = configs.getMaxWorkerExecuteTime() * 1000000000L; // convert seconds to nanoseconds
                options.setMaxWorkerExecuteTime(maxWorkerExecuteTime);
                LOGGER.info("Setting vert.x max worker execution time to {} seconds via configuration", configs.getMaxWorkerExecuteTime());
            }
            if (configs.getMaxEventLoopExecuteTime() != null) {
                final Long maxEventLoopExecuteTime = configs.getMaxEventLoopExecuteTime() * 1000000000L; // convert seconds to nanoseconds
                options.setMaxEventLoopExecuteTime(maxEventLoopExecuteTime);
                LOGGER.info("Setting vert.x max event loop execution time to {} seconds via configuration", configs.getMaxEventLoopExecuteTime());
            }
            if (configs.getBlockedThreadCheckInterval() != null) {
                final Long blockedThreadCheckInterval = configs.getBlockedThreadCheckInterval() * 1000L; // convert seconds to nanoseconds
                options.setBlockedThreadCheckInterval(blockedThreadCheckInterval);
                LOGGER.info("Setting vert.x blocked thread check interval to {} seconds via configuration", configs.getBlockedThreadCheckInterval());
            }
        }
        LOGGER.info("vert.x worker pool size is {}", options.getWorkerPoolSize());
        LOGGER.info("vert.x event loop pool size is {}", options.getEventLoopPoolSize());
        LOGGER.info("vert.x max worker execution time is {} seconds", options.getMaxWorkerExecuteTime() / 1000000000L);
        LOGGER.info("vert.x max event loop execution time is {} seconds", options.getMaxEventLoopExecuteTime() / 1000000000L);
        LOGGER.info("vert.x blocked thread check interval is {} seconds", options.getBlockedThreadCheckInterval() / 1000L);
    }

    public void addShutdownHook(final Vertx myVertx) {
        LOGGER.info("Registering shutdown hook");

        // A hook that initiates a graceful shutdown if the server has been stopped via SIGTERM / SIGINT
        Runtime.getRuntime().addShutdownHook(new Thread("t9t-shutdown-hook") {
            @Override
            public void run() {
                LOGGER.info("Gracefully shutting down the " + PRINTED_NAME);
                StatusProvider.setShutdownInProgress();

                // obtaining the deployment IDs
                final Set<String> idsToUndeploy = myVertx.deploymentIDs();
                LOGGER.info("Undeploying {} verticles", idsToUndeploy.size());
                for (final String id : idsToUndeploy) {
                    myVertx.undeploy(id, (final AsyncResult<Void> it) -> {
                        if (it.succeeded()) {
                            LOGGER.debug("Undeployment of ID {} completed", id);
                        } else {
                            LOGGER.error("Undeployment of ID {} FAILED", id);
                        }
                    });
                }
                try {
                    Thread.sleep(250L);  // settle down...
                } catch (final InterruptedException e) {
                    LOGGER.error("Interrupted.", e);
                }

                LOGGER.info("Initiating JDP shutdown sequence (service shutdown)");
                Jdp.shutdown();
                LOGGER.info("Normal end " + PRINTED_NAME);
            }
        });
    }

    public void deployAndRun(final Vertx myVertx, final Consumer<Vertx> additionalInits) {
        Jdp.bindInstanceTo(myVertx, Vertx.class);  // make the Vertx instance available to other services (required by email client)
        portAutoDetect();
        myVertx.deployVerticle(this, (final AsyncResult<String> it) -> {
            if (it.succeeded()) {
                LOGGER.info("Successfully deploy single instance verticle");
                if (additionalInits != null) {
                    additionalInits.accept(myVertx);
                }

                addShutdownHook(myVertx);
                if (metricsProvider != null) {
                    metricsProvider.installMeters(myVertx);
                }

                new Thread(() -> {
                    for (;;) {
                        try {
                            Thread.sleep(MORE_THAN_ONE_YEAR);
                        } catch (final InterruptedException e) {
                            LOGGER.error("Interrupted.", e);
                        }
                    }
                }, "t9t-keepalive").start(); // wait in some other thread

                T9tUtil.logWithLines(LOGGER, PRINTED_NAME + " (" + T9tServer.class.getSimpleName() + ") started");
            } else {
                LOGGER.error("Could not deploy T9tServer verticle", it.cause());
            }
        });
    }

    public void checkForMetricsAndInitialize(final VertxOptions options) {
        mergePoolSizes(options);
        if (preferNative) {
            options.setPreferNativeTransport(true);
        }
        if (metrics) {
            metricsProvider = Jdp.getOptional(IVertxMetricsProvider.class);
            if (metricsProvider != null) {
                metricsProvider.setOptions(options);
            } else {
                LOGGER.warn("metrics requested via command line, but no metrics provider part of JAR");
            }
        }
    }

    /**
     * Trigger after the parsing the args.
     */
    public void postAction() {
        // SQL migration: flag must be stored as a system property to allow execution via Jdp startup
        if (migrateDb) {
            LOGGER.info("Migrate DB option has been set to true. Migration/upgrade of database planned.");
            System.setProperty(T9tConstants.START_MIGRATION_PROPERTY, "true");
        }
    }

    /**
     * Set system parameter which otherwise must be set via -D command line property.
     */
    public static void configureSystemParameters() {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("org.jboss.logging.provider", "slf4j");
        System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
        System.setProperty("vertx.disableFileCaching", "true");
        // do not access the "internal" ContextFactory (essential for Jakarta 8, fatal for Jakarta 9.1ff)
        // System.setProperty("jakarta.xml.bind.JAXBContextFactory", "com.sun.xml.bind.v2.ContextFactory"); // FATAL for jakarta namespace
        // System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true"); // very likely also no longer good for Jakarta
    }

    /**
     * Parse args then run the serverConsumer.
     * @param args arguments from command line
     * @param serverConsumer provide T9tServer instance
     */
    public static void parseCommandLine(final String[] args, final Consumer<T9tServer> serverConsumer) {
        final T9tServer server = new T9tServer();
        final CommandLine cmd = new CommandLine(server);

        try {
            cmd.parseArgs(args);

            if (cmd.isUsageHelpRequested()) {
                cmd.usage(cmd.getOut());
                final int exitCode = cmd.getCommandSpec().exitCodeOnUsageHelp();
                System.exit(exitCode);
            }

            server.postAction();

            serverConsumer.accept(server);

        } catch (final ParameterException e) {
            cmd.getErr().println(e.getMessage());
            if (!UnmatchedArgumentException.printSuggestions(e, cmd.getErr())) {
                e.getCommandLine().usage(cmd.getErr());
            }

            final int exitCode = cmd.getCommandSpec().exitCodeOnInvalidInput();
            System.exit(exitCode);
        }
    }

    public static void main(final String[] args) {
        configureSystemParameters();

        parseCommandLine(args, (final T9tServer server) -> {
            LOGGER.info(PRINTED_NAME + " (single node based) starting...");

            server.readConfig(); // update a possible new location of the config file before we run the startup process

            Init.initializeT9t();

            final VertxOptions options = new VertxOptions();
            server.checkForMetricsAndInitialize(options);
            server.deployAndRun(Vertx.vertx(options), null);
        });
    }
}
