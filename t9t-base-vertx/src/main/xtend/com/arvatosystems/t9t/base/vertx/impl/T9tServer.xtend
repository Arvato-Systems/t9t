/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.vertx.impl

import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.services.IFileUtil
import com.arvatosystems.t9t.base.vertx.IServiceModule
import com.arvatosystems.t9t.base.vertx.MultiThreadMessageCoderFactory2
import com.arvatosystems.t9t.cfg.be.ConfigProvider
import com.arvatosystems.t9t.cfg.be.StatusProvider
import com.arvatosystems.t9t.jdp.Init
import com.martiansoftware.jsap.FlaggedOption
import com.martiansoftware.jsap.JSAP
import com.martiansoftware.jsap.Parameter
import com.martiansoftware.jsap.SimpleJSAP
import com.martiansoftware.jsap.Switch
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.dp.Jdp
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.StaticHandler
import java.util.ArrayList
import java.util.Collections
import de.jpaw.bonaparte.util.DeprecationWarner
import java.util.function.Consumer
import com.arvatosystems.t9t.auth.jwt.JWT
import com.arvatosystems.t9t.base.T9tConstants

@AddLogger
class T9tServer extends AbstractVerticle {
    public static final long MORE_THAN_ONE_YEAR = 500L * 86400L * 1000L;     // JVM update due at least once per year (at least every 500 days)
    // private fields
    static boolean cors     = false;
    static int     port     = 8024           // default for http
    static int     tcpPort  = 8023           // default for TCP/IP
    static String  cfgFile  = null;
    static String  filePath = null;
    static String  corsParm = "*";
    static boolean migrateDb = false;
    final IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory = new MultiThreadMessageCoderFactory2(BonaPortable, ServiceResponse)

    // autodetect the port assigned on AWS
    def static void portAutoDetect() {
        try {
            val portBySysProp = System.getProperty("t9t.port.http")
            if (portBySysProp !== null) {
                LOGGER.info("Found system property spec for t9t.port.http {}", portBySysProp)
                port = Integer.parseInt(portBySysProp)
            } else {
                val portByEnv = System.getenv("PORT")
                if (portByEnv !== null) {
                    LOGGER.info("Found environment spec for PORT: {}, assuming we run on AWS Elastic Beanstalk", portByEnv)
                    port = Integer.parseInt(portByEnv)
                } else {
                    LOGGER.info("No PORT environment setting found, assuming we are not on AWS, using port {}", port)
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception {}: {} while retrieving port setting", e.class.simpleName, e.message)
        }
    }

    def static void parseCommandLine(String[] args) {
        val options = new ArrayList<Parameter>();
        options.add(new FlaggedOption("filePath", JSAP.STRING_PARSER,  filePath,                    JSAP.NOT_REQUIRED, 'f', "filePath", "path to serve files for /fs from"));
        options.add(new FlaggedOption("cfg",      JSAP.STRING_PARSER,  cfgFile,                     JSAP.NOT_REQUIRED, 'c', "cfg",      "configuration filename"));
        options.add(new FlaggedOption("port",     JSAP.INTEGER_PARSER, Integer.toString(port),      JSAP.NOT_REQUIRED, 'p', "port",     "listener port http (0 to disable)"));
        options.add(new FlaggedOption("tcpport",  JSAP.INTEGER_PARSER, Integer.toString(tcpPort),   JSAP.NOT_REQUIRED, 'P', "tcpport",  "listener port for plain socket (0 to disable)"));
        options.add(new FlaggedOption("corsParm", JSAP.STRING_PARSER,  "*",                         JSAP.NOT_REQUIRED, 'C', "corsParm", "parameter to the CORS handler"));
        options.add(new Switch       ("cors",                                                                          'X', "cors",     "activate CORS handler"));
        options.add(new FlaggedOption("migrateDb",JSAP.BOOLEAN_PARSER, migrateDb.toString,          JSAP.NOT_REQUIRED, 'm', "migrateDb","Flag whether to migrate db"));

        val commandLineOptions = new SimpleJSAP("t9t server", "Runs a simple vert.x / t9t based server", options.toArray(newArrayOfSize(options.size)))
        val cmd = commandLineOptions.parse(args);
        if (commandLineOptions.messagePrinted()) {
            System.err.println("(use option --help for usage)");
            System.exit(1);
        }
        filePath = cmd.getString("filePath");
        cfgFile  = cmd.getString("cfg");
        port     = cmd.getInt("port");
        tcpPort  = cmd.getInt("tcpport");
        cors     = cmd.getBoolean("cors")
        corsParm = cmd.getString("corsParm");
        migrateDb= cmd.getBoolean("migrateDb");

        // SQL migration: flag must be stored as a system property to allow execution via Jdp startup
        if (migrateDb) {
            LOGGER.info("Migrate DB option has been set to true. Migration/upgrade of database planned.")
            System.setProperty(T9tConstants.START_MIGRATION_PROPERTY, "true")
        }
    }

    override void start() {
        super.start

        DeprecationWarner.setWarner = null  // no noise in the logs on the server side - warnings are primarily intended for clients!

        val modules = Jdp.getOneInstancePerQualifier(IServiceModule)
        Collections.sort(modules)
        LOGGER.info("T9t Vert.x server started, modules found are: " + modules.map[moduleName].join(', '))
        val uploadsFolder = Jdp.getRequired(IFileUtil).getAbsolutePath("file-uploads");
        LOGGER.info("Pathname for file uploads will be {}", uploadsFolder)

        if (port > 0) {
            val router = Router.router(vertx) => [
                route.handler(BodyHandler.create(uploadsFolder).setBodyLimit(16777100))     // must be before any possible execBlocking, see https://github.com/vert-x3/vertx-web/issues/198

                // register the web paths of the injected modules
                for (m : modules) {
                    m.mountRouters(it, vertx, coderFactory)
                }

                route("/static/*").handler(StaticHandler.create => [
                    webRoot = "web"
                    filesReadOnly = true
                    maxAgeSeconds = 12 * 60 * 60  // 12 hours (1 working day)
                ])
                if (filePath !== null) {
                    route("/fs/*").handler(StaticHandler.create => [
                        webRoot                   = filePath
                        filesReadOnly             = false
                        cachingEnabled            = false
                        allowRootFileSystemAccess = true
                        maxAgeSeconds             = 5  // no caching while testing
                    ])
                }
                if (cors) {
                    LOGGER.info("Setting up CORS handler for origin {}", corsParm)
                    route().handler(CorsHandler.create(corsParm)
                      .allowedMethod(HttpMethod.GET)
                      .allowedMethod(HttpMethod.POST)
                      .allowedMethod(HttpMethod.OPTIONS)
                      .allowedHeader(HttpHeaders.CONTENT_TYPE.toString()
                    )
                      .allowedHeader(HttpHeaders.ACCEPT.toString())
                      .allowedHeader("Charset")
                      .allowedHeader(HttpHeaders.ACCEPT_CHARSET.toString())
                      .allowedHeader(HttpHeaders.CONTENT_LENGTH.toString())
                      .allowedHeader(HttpHeaders.AUTHORIZATION.toString())
                    );
                }
                get("/favicon.ico").handler [
                    LOGGER.info("favicon requested")
                    response.sendFile("web/favicon.ico")
                ]
                route.handler [           // no matching path or method
                    val errorMsg = '''Request method «request.method» for path «request.path» not supported'''
                    LOGGER.info(errorMsg)
                    response.statusMessage = errorMsg
                    response.statusCode = 404
                    response.end
                ]
            ]
            vertx.createHttpServer => [
                requestHandler [ router.handle(it) ]
                listen(port)
            ]
        }
        if (tcpPort > 0) {
            // compact format (requires ServiceRequest wrapper)
            vertx.createNetServer => [
                connectHandler [ new TcpSocketHandler(it) ]
                listen(tcpPort)
            ]
        }
        Jdp.bindInstanceTo(vertx, Vertx)
        Jdp.bindInstanceTo(vertx.eventBus, EventBus)
        AsyncProcessor.register(vertx)
    }

    /** Reads some external configuration file, updates the JWT with the keystore, if required. */
    def static void readConfig() {
        // require this in other modules
        ConfigProvider.readConfiguration(cfgFile);
        val serverCfg = ConfigProvider.getConfiguration().serverConfiguration
        if (serverCfg !== null && (serverCfg.keyStorePassword !== null || serverCfg.keyStorePath !== null)) {
            JWT.setKeyStore(serverCfg.keyStorePath, serverCfg.keyStorePassword, serverCfg.keyStorePath === null)
            LOGGER.info("Using environment specific keystore and/or password from local config file. Good.")
        } else {
            LOGGER.warn("No environment specific keystore parameters. Using defaults, do not use this in production!")
        }
    }

    def static void mergePoolSizes(VertxOptions options) {
        val configs = ConfigProvider.configuration.applicationConfiguration
        if (configs !== null) {
            if (configs.workerPoolSize !== null) {
                options.workerPoolSize = configs.workerPoolSize
                LOGGER.info("Setting vert.x worker pool size to {} via configuration", configs.workerPoolSize)
            }
            if (configs.eventLoopPoolSize !== null) {
                options.eventLoopPoolSize = configs.eventLoopPoolSize
                LOGGER.info("Setting vert.x event loop pool size to {} via configuration", configs.eventLoopPoolSize)
            }
            if (configs.maxWorkerExecuteTime !== null) {
                options.maxWorkerExecuteTime = configs.maxWorkerExecuteTime * 1000000000L // convert seconds to nanoseconds
                LOGGER.info("Setting vert.x max worker execution time to {} seconds via configuration", configs.maxWorkerExecuteTime)
            }
            if (configs.maxEventLoopExecuteTime !== null) {
                options.maxEventLoopExecuteTime = configs.maxEventLoopExecuteTime * 1000000000L // convert seconds to nanoseconds
                LOGGER.info("Setting vert.x max event loop execution time to {} seconds via configuration", configs.maxEventLoopExecuteTime)
            }
            if (configs.blockedThreadCheckInterval !== null) {
                options.blockedThreadCheckInterval = configs.blockedThreadCheckInterval * 1000L // convert seconds to nanoseconds
                LOGGER.info("Setting vert.x blocked thread check interval to {} seconds via configuration", configs.blockedThreadCheckInterval)
            }
        }
        LOGGER.info("vert.x worker pool size is {}", options.workerPoolSize)
        LOGGER.info("vert.x event loop pool size is {}", options.eventLoopPoolSize)
        LOGGER.info("vert.x max worker execution time is {} seconds", options.maxWorkerExecuteTime / 1000000000L)
        LOGGER.info("vert.x max event loop execution time is {} seconds", options.maxEventLoopExecuteTime / 1000000000L)
        LOGGER.info("vert.x blocked thread check interval is {} seconds", options.blockedThreadCheckInterval / 1000L)
    }

    def static void addShutdownHook(Vertx myVertx) {
        LOGGER.info("Registering shutdown hook");

        // A hook that initiates a graceful shutdown if the server has been stopped via SIGTERM / SIGINT
        Runtime.getRuntime().addShutdownHook(new Thread("t9t-shutdown-hook") {
            override void run() {
                LOGGER.info("Gracefully shutting down the t9t vert.x server");
                StatusProvider.setShutdownInProgress()

                // obtaining the deployment IDs
                val idsToUndeploy = myVertx.deploymentIDs
                LOGGER.info("Undeploying {} verticles", idsToUndeploy.size());
                for (id : idsToUndeploy) {
                    myVertx.undeploy(id, [
                        if (succeeded)
                            LOGGER.debug("Undeployment of ID {} completed", id)
                        else
                            LOGGER.error("Undeployment of ID {} FAILED", id)
                    ])
                }
                Thread.sleep(250L)  // settle down...

                LOGGER.info("Initiating JDP shutdown sequence (service shutdown)");
                Jdp.shutdown();
                LOGGER.info("Normal end t9t vert.x server");
            }
        });
    }

    def static void deployAndRun(Vertx myVertx, Consumer<Vertx> additionalInits) {
        Jdp.bindInstanceTo(myVertx, Vertx)  // make the Vertx instance available to other services (required by email client)
        myVertx.deployVerticle(new T9tServer => [ portAutoDetect ], [
            if (succeeded) {
                LOGGER.info("Successfully deploy single instance verticle")
                additionalInits?.accept(myVertx)

                addShutdownHook(myVertx);
                new Thread([for (;;) Thread.sleep(MORE_THAN_ONE_YEAR)], "t9t-keepalive").start // wait in some other thread
            } else {
                LOGGER.error("Could not deploy T9tServer verticle", cause)
            }
        ])
    }

    /** Set system parameter which otherwise must be set via -D command line property. */
    def static void configureSystemParameters() {
        System.setProperty("java.awt.headless", "true");                        // required for operation of java image libraries (t9t-doc-be) and Jasper reports
        System.setProperty("org.jboss.logging.provider", "slf4j");              // configure hibernate to use slf4j
        System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
        System.setProperty("vertx.disableFileCaching", "true");                 // disable caching of resources in .vertx (for development)
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");   // prevent Illegal reflection access with Java 10 (fixed with jaxb 2.3.1)
    }

    def static void main(String[] args) throws Exception {
        LOGGER.info('''t9t vert.x single node based server starting...''')

        configureSystemParameters

        parseCommandLine(args);

        readConfig       // update a possible new location of the config file before we run the startup process

        Init.initializeT9t

        val options = new VertxOptions
        options.mergePoolSizes
        Vertx.vertx(options).deployAndRun(null)
    }
}
