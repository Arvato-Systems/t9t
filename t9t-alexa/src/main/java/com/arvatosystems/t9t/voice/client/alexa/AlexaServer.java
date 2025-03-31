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
package com.arvatosystems.t9t.voice.client.alexa;

import com.arvatosystems.t9t.cfg.be.ApplicationConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.jdp.Init;
import com.arvatosystems.t9t.voice.VoiceProvider;
import com.arvatosystems.t9t.voice.client.SessionCache;
import com.arvatosystems.t9t.voice.client.VoiceSessionContext;

import de.jpaw.bonaparte.core.JsonComposer;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.pojos.api.alexa.AlexaIntentIn;
import de.jpaw.bonaparte.pojos.api.alexa.AlexaOutputSpeech;
import de.jpaw.bonaparte.pojos.api.alexa.AlexaReprompt;
import de.jpaw.bonaparte.pojos.api.alexa.AlexaRequestBody;
import de.jpaw.bonaparte.pojos.api.alexa.AlexaRequestType;
import de.jpaw.bonaparte.pojos.api.alexa.AlexaResponse;
import de.jpaw.bonaparte.pojos.api.alexa.AlexaResponseBody;
import de.jpaw.bonaparte.pojos.api.alexa.SpeechType;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.json.JsonParser;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.UnmatchedArgumentException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlexaServer extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlexaServer.class);
    private static final long MORE_THAN_ONE_YEAR = 500L * 86400L * 1000L; // JVM update due at least once per year (at least every 500 days)

    @SuppressWarnings("unchecked")
    private SessionCache<VoiceSessionContext> sessionCache;

    @Option(names = { "--port", "-p" }, defaultValue = "8980", description = "listener port http (0 to disable)")
    private int port; // default for http

    @Option(names = { "--cfg", "-c" }, description = "configuration filename")
    private String cfgFile;

    @Option(names = { "--context", "-C" }, defaultValue = "/test/*", description = "context path to listen on")
    private String context;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpRequested;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void start() throws Exception {
        super.start();

        LOGGER.info("T9t voice proxy started");

        if (port > 0) {
            final Router router = Router.router(vertx);
            // must be before any possible execBlocking, see https://github.com/vert-x3/vertx-web/issues/198
            router.route().handler(BodyHandler.create().setBodyLimit(16777100));
            router.route(context).handler((final RoutingContext it) -> {
                final String ct = stripCharset(it.request().headers().get(HttpHeaders.CONTENT_TYPE));
                final String rqBody = it.getBody().toString();
                final HttpServerRequest request = it.request();
                LOGGER.info("\n");
                LOGGER.info("Received {} request to {} with type {} and content <<{}>>", request.method(), request.path(), ct, rqBody);
                for (final Map.Entry<String, String> e : request.headers()) {
                    AlexaServer.LOGGER.debug("HEADER {} is {}", e.getKey(), e.getValue());
                  }
                if (ct == null) {
                    it.response().setStatusCode(415);
                    it.response().setStatusMessage("Content-Type not specified");
                    return;
                }
                try {
                    final Map<String, Object> map = new JsonParser(rqBody, false).parseObject();
                    final AlexaRequestBody rq = new AlexaRequestBody();
                    MapParser.populateFrom(rq, map);
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Request parsed as {}", ToStringHelper.toStringML(rq));
                    }
                    final Object requestType = rq.getRequest().get("type");
                    final String sessionId = rq.getSession() == null ? null : rq.getSession().getSessionId();
                    if (sessionId == null) {
                        LOGGER.error("No session ID provided????");
                    }
                    if (rq.getSession() == null || rq.getSession().getIsNew() == null) {
                        LOGGER.error("No session isNew flag provided????");
                    }
                    VoiceSessionContext session = sessionCache.get(sessionId);
                    if (session != null) {
                        if (rq.getSession().getIsNew()) {
                            // validate none exists, then create a NEW session
                            LOGGER.warn("Found EXISTING session, there should be none because NEW is sent");
                            // CREATE a new one
                        }
                    } else {
                        if (!rq.getSession().getIsNew()) {
                            LOGGER.warn("No EXISTING session, but there should be one because NEW is NOT set");
                        }
                        final Object locale = rq.getRequest() != null ? rq.getRequest().get("locale") : null;
                        session = sessionCache.createSession(sessionId, VoiceProvider.ALEXA, rq.getSession().getApplication().getApplicationId(),
                                rq.getSession().getUser().getUserId(), locale != null ? (String) locale : null);
                    }
                    final AlexaResponseBody respBody = new AlexaResponseBody();
                    respBody.setVersion("1.0");
                    respBody.setSessionAttributes(new HashMap<>());
                    if (AlexaRequestType.SESSION_ENDED_REQUEST.getToken().equals(requestType)) {
                        LOGGER.info("session ended requested - no response expected");
                        if (session.shouldTerminateWhenDone) {
                            sessionCache.removeSession(session.providerSessionKey);
                        }
                    } else {
                        final AlexaResponse resp = new AlexaResponse();
                        respBody.setResponse(resp);
                        resp.setShouldEndSession(false);
                        final AlexaReprompt alexaRepromt = new AlexaReprompt();
                        final AlexaOutputSpeech alexaOutputSpeech = new AlexaOutputSpeech();
                        alexaOutputSpeech.setType(SpeechType.PLAIN_TEXT);
                        alexaOutputSpeech.setText("Und nun?");
                        alexaRepromt.setOutputSpeech(alexaOutputSpeech);
                        resp.setReprompt(alexaRepromt);
                        final AlexaIntentIn intent = new AlexaIntentIn();
                        if (AlexaRequestType.LAUNCH_REQUEST.getToken().equals(requestType)) {
                            intent.setName("launch");
                        } else if (AlexaRequestType.INTENT_REQUEST.getToken().equals(requestType)) {
                            MapParser.populateFrom(intent, (Map<String, Object>) rq.getRequest().get("intent"));
                            LOGGER.debug("Intent parsed as {}", rq);
                        } else {
                            intent.setName("error");
                            LOGGER.error("I do not know how to deal with request type {}", request);
                        }
                        final String executorQualifierStr;
                        if (session.nextCallbacks == null || session.nextCallbacks.get(intent.getName()) == null) {
                            executorQualifierStr = null;
                        } else {
                            executorQualifierStr = session.nextCallbacks.get(intent.getName());
                        }
                        final String executorQualifier = executorQualifierStr == null ? intent.getName() : executorQualifierStr;
                        final IGenericIntent intentExecutor = Jdp.getRequired(IGenericIntent.class, executorQualifier);
                        LOGGER.info("Performing intent {}", intent);
                        session.previousCallbacks = session.nextCallbacks;
                        session.nextCallbacks = null;
                        intentExecutor.execute(session, intent, resp);

                        // get backend
                    }

                    it.response().setStatusCode(200);
                    it.response().putHeader(HttpHeaders.CONTENT_TYPE, ct);
                    it.response().end(Buffer.buffer(JsonComposer.toJsonStringNoPQON(respBody)));
                } catch (final Exception e) {
                    it.response().setStatusCode(500);
                    it.response().setStatusMessage(e.getMessage());
                    return;
                }
            });

            router.get("/favicon.ico").handler((final RoutingContext it) -> {
                LOGGER.info("favicon requested");
                it.response().sendFile("web/favicon.ico");
            });
            // no matching path or method
            router.route().handler((final RoutingContext it) -> {
                final String errorMsg = "Request method " + it.request().method() + " for path " + it.request().path() + " not supported";
                LOGGER.info(errorMsg);
                it.response().setStatusCode(404);
                it.response().setStatusMessage(errorMsg);
                it.response().end();
            });

            final HttpServer httpServer = vertx.createHttpServer();
            httpServer.listen(port);
        }
    }

    public String stripCharset(final String s) {
        if (s == null) {
            return s;
        }
        final int ind = s.indexOf(";");
        if (ind >= 0) {
            return s.substring(0, ind); // strip off optional ";Charset..." portion
        }
        return s;
    }

    // autodetect the port assigned on AWS
    public void portAutoDetect() {
        try {
            final String portBySysProp = System.getProperty("t9t.port.http");
            if (portBySysProp != null) {
                LOGGER.info("Found system property spec for t9t.port.http {}", portBySysProp);
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
        } catch (Exception e) {
            LOGGER.error("Exception {}: {} while retrieving port setting", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public void readConfig() {
        // require this in other modules
        ConfigProvider.readConfiguration(cfgFile);
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
                options.setMaxWorkerExecuteTime(configs.getMaxWorkerExecuteTime() * 1000000000L); // convert seconds to nanoseconds
                LOGGER.info("Setting vert.x max worker execution time to {} seconds via configuration", configs.getMaxWorkerExecuteTime());
            }
            if (configs.getMaxEventLoopExecuteTime() != null) {
                options.setMaxEventLoopExecuteTime(configs.getMaxEventLoopExecuteTime() * 1000000000L); // convert seconds to nanoseconds
                LOGGER.info("Setting vert.x max event loop execution time to {} seconds via configuration", configs.getMaxEventLoopExecuteTime());
            }
        }
        LOGGER.info("vert.x worker pool size is {}", options.getWorkerPoolSize());
        LOGGER.info("vert.x event loop pool size is {}", options.getEventLoopPoolSize());
        LOGGER.info("vert.x max worker execution time is {} seconds", options.getMaxWorkerExecuteTime() / 1000000000L);
        LOGGER.info("vert.x max event loop execution time is {} seconds", options.getMaxEventLoopExecuteTime() / 1000000000L);
    }

    /**
     * Initialize sessionCache after Init.initializeT9t()
     */
    private void initializeSessionCache() {
        sessionCache = Jdp.getRequired(SessionCache.class);
    }

    /**
     * Parse args then run the serverConsumer.
     * @param args
     *            arguments from command line
     * @param serverConsumer
     *            provide T9tServer instance
     */
    public static void parseCommandLine(final String[] args, final Consumer<AlexaServer> serverConsumer) {
        final AlexaServer server = new AlexaServer();
        final CommandLine cmd = new CommandLine(server);

        try {
            cmd.parseArgs(args);

            if (cmd.isUsageHelpRequested()) {
                cmd.usage(cmd.getOut());
                final int exitCode = cmd.getCommandSpec().exitCodeOnUsageHelp();
                System.exit(exitCode);
            }
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

    public static void main(final String[] args) throws Exception {
        LOGGER.info("t9t vert.x based voice proxy starting...");
        parseCommandLine(args, (final AlexaServer server) -> {
            server.readConfig(); // update a possible new location of the config file before we run the startup process

            Init.initializeT9t();
            server.initializeSessionCache();

            final VertxOptions options = new VertxOptions();
            server.mergePoolSizes(options);
            server.portAutoDetect();
            Vertx.vertx(options).deployVerticle(server);

            new Thread(() -> {
                try {
                    for (;;) {
                        Thread.sleep(MORE_THAN_ONE_YEAR);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted.", e);
                }
            }, "alexa-keepalive").start(); // wait in some other thread
        });
    }
}
