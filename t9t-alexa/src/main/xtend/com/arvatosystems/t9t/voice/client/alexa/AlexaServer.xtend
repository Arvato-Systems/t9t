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
package com.arvatosystems.t9t.voice.client.alexa

import com.arvatosystems.t9t.cfg.be.ConfigProvider
import com.arvatosystems.t9t.client.init.AbstractConfigurationProvider
import com.arvatosystems.t9t.client.init.SystemConfigurationProvider
import com.arvatosystems.t9t.jdp.Init
import com.arvatosystems.t9t.voice.VoiceProvider
import com.arvatosystems.t9t.voice.client.SessionCache
import com.arvatosystems.t9t.voice.client.alexa.IGenericIntent
import com.martiansoftware.jsap.FlaggedOption
import com.martiansoftware.jsap.JSAP
import com.martiansoftware.jsap.Parameter
import com.martiansoftware.jsap.SimpleJSAP
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.JsonComposer
import de.jpaw.bonaparte.core.MapParser
import de.jpaw.bonaparte.pojos.api.alexa.AlexaIntentIn
import de.jpaw.bonaparte.pojos.api.alexa.AlexaOutputSpeech
import de.jpaw.bonaparte.pojos.api.alexa.AlexaReprompt
import de.jpaw.bonaparte.pojos.api.alexa.AlexaRequestBody
import de.jpaw.bonaparte.pojos.api.alexa.AlexaRequestType
import de.jpaw.bonaparte.pojos.api.alexa.AlexaResponse
import de.jpaw.bonaparte.pojos.api.alexa.AlexaResponseBody
import de.jpaw.bonaparte.pojos.api.alexa.SpeechType
import de.jpaw.bonaparte.util.ToStringHelper
import de.jpaw.dp.Inject
import de.jpaw.dp.Jdp
import de.jpaw.json.JsonParser
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import java.util.ArrayList
import java.util.Map

import static io.vertx.core.http.HttpHeaders.*

@AddLogger
class AlexaServer extends AbstractVerticle {
    private static final long MORE_THAN_ONE_YEAR = 500L * 86400L * 1000L;     // JVM update due at least once per year (at least every 500 days)
    private static int     port     = 8980           // default for http
    private static String  cfgFile  = null;
    private static String  context  = "/test/*";
    @Inject SessionCache sessionCache

    def static stripCharset(String s) {
        if (s === null)
            return s;
        val ind = s.indexOf(';')
        if (ind >= 0)
            return s.substring(0, ind)      // strip off optional ";Charset..." portion
        else
            return s
    }

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
        options.add(new FlaggedOption("cfg",      JSAP.STRING_PARSER,  cfgFile,                     JSAP.NOT_REQUIRED, 'c', "cfg",      "configuration filename"));
        options.add(new FlaggedOption("context",  JSAP.STRING_PARSER,  context,                     JSAP.NOT_REQUIRED, 'C', "context",  "context path to listen on"));
        options.add(new FlaggedOption("port",     JSAP.INTEGER_PARSER, Integer.toString(port),      JSAP.NOT_REQUIRED, 'p', "port",     "listener port http (0 to disable)"));

        val commandLineOptions = new SimpleJSAP("t9t voice proxy", "Runs a simple vert.x / t9t based voice proxy", options.toArray(newArrayOfSize(options.size)))
        val cmd = commandLineOptions.parse(args);
        if (commandLineOptions.messagePrinted()) {
            System.err.println("(use option --help for usage)");
            System.exit(1);
        }
        cfgFile  = cmd.getString("cfg");
        context  = cmd.getString("context");
        port     = cmd.getInt("port");
    }

    override void start() {
        super.start

//        val modules = Jdp.getOneInstancePerQualifier(IServiceModule)
//        Collections.sort(modules)
//        LOGGER.info("T9t voice proxy started, modules found are: " + modules.map[moduleName].join(', '))
        LOGGER.info("T9t voice proxy started")

        if (port > 0) {
            val router = Router.router(vertx) => [
                route.handler(BodyHandler.create().setBodyLimit(16777100))     // must be before any possible execBlocking, see https://github.com/vert-x3/vertx-web/issues/198

                // register the web paths of the injected modules
//                for (m : modules) {
//                    m.mountRouters(it, vertx, coderFactory)
//                }

                route(context).handler [
                    val ct = request.headers.get(CONTENT_TYPE).stripCharset
                    val rqBody = body.toString
                    LOGGER.info("\n")
                    LOGGER.info("Received {} request to {} with type {} and content <<{}>>", request.method, request.path, ct, rqBody)
                    for (e : request.headers) {
                        LOGGER.debug("HEADER {} is {}", e.key, e.value)
                    }
                    if (ct === null) {
                        response.statusCode = 415
                        response.statusMessage = "Content-Type not specified"
                        return
                    }
                    try {
                        val map = new JsonParser(rqBody, false).parseObject
                        val rq = new AlexaRequestBody
                        MapParser.populateFrom(rq, map);
                        if (LOGGER.isTraceEnabled())
                            LOGGER.trace("Request parsed as {}", ToStringHelper.toStringML(rq))
                        val requestType = rq.request.get("type")
                        val sessionId = rq.session?.sessionId
                        if (sessionId === null)
                            LOGGER.error("No session ID provided????")
                        if (rq.session?.isNew === null)
                            LOGGER.error("No session isNew flag provided????")
                        var session = sessionCache.get(sessionId)
                        if (session !== null) {
                            if (rq.session.isNew)
                                  // validate none exists, then create a NEW session
                                LOGGER.warn("Found EXISTING session, there should be none because NEW is sent")
                            // CREATE a new one
                        } else {
                            if (!rq.session.isNew)
                                LOGGER.warn("No EXISTING session, but there should be one because NEW is NOT set")
                            val locale = if (rq.request !== null) rq.request.get("locale")
                            session = sessionCache.createSession(sessionId, VoiceProvider.ALEXA, rq.session.application.applicationId, rq.session.user.userId, if (locale !== null) locale as String)
                        }
                        val respBody = new AlexaResponseBody => [
                            version = "1.0"
                            sessionAttributes = #{}
                        ]
                        if (requestType == AlexaRequestType.SESSION_ENDED_REQUEST.token) {
                            LOGGER.info("session ended requested - no response expected")
                            if (session.shouldTerminateWhenDone)
                                sessionCache.removeSession(session.providerSessionKey)
                        } else {
                            val resp = new AlexaResponse
                            respBody.response = resp
                            resp.shouldEndSession = false
                            resp.reprompt = new AlexaReprompt => [
                                outputSpeech = new AlexaOutputSpeech => [
                                    type = SpeechType.PLAIN_TEXT
                                    text = "Und nun?"
                                ]
                            ]
                            val intent = new AlexaIntentIn
                            if (requestType == AlexaRequestType.LAUNCH_REQUEST.token) {
                                intent.name = "launch"
                            } else if (requestType == AlexaRequestType.INTENT_REQUEST.token) {
                                MapParser.populateFrom(intent, rq.request.get("intent") as Map<String, Object>);
                                LOGGER.debug("Intent parsed as {}", rq)
                            } else {
                                intent.name = "error"
                                LOGGER.error("I do not know how to deal with request type {}", request)
                            }
                            val executorQualifier = session.nextCallbacks?.get(intent.name) ?: intent.name
                            val intentExecutor = Jdp.getRequired(IGenericIntent, executorQualifier)
                            LOGGER.info("Performing intent {}", intent)
                            session.previousCallbacks = session.nextCallbacks
                            session.nextCallbacks = null
                            intentExecutor.execute(session, intent, resp)

                            // get backend

                        }
                        response.statusCode = 200
                        response.putHeader(CONTENT_TYPE, ct)
                        response.end(Buffer.buffer(JsonComposer.toJsonStringNoPQON(respBody)))
                    } catch (Exception e) {
                        LOGGER.error("Exception: ", e)
                        response.statusCode = 500
                        response.statusMessage = e.message
                        return
                    }
                ]
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
                requestHandler [ router.accept(it) ]
                listen(port)
            ]
        }
    }

    def static void readConfig() {
        // require this in other modules
        ConfigProvider.readConfiguration(cfgFile);
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
        }
        LOGGER.info("vert.x worker pool size is {}", options.workerPoolSize)
        LOGGER.info("vert.x event loop pool size is {}", options.eventLoopPoolSize)
        LOGGER.info("vert.x max worker execution time is {} seconds", options.maxWorkerExecuteTime / 1000000000L)
        LOGGER.info("vert.x max event loop execution time is {} seconds", options.maxEventLoopExecuteTime / 1000000000L)
    }

    def static void main(String[] args) throws Exception {
        LOGGER.info('''t9t vert.x based voice proxy starting...''')

        // System.setProperty("vertx.disableFileCaching", "true");              // disable caching of resources in .vertx (for development)

        parseCommandLine(args);

        readConfig       // update a possible new location of the config file before we run the startup process

        Init.initializeT9t
        Jdp.bindInstanceTo(new SystemConfigurationProvider, AbstractConfigurationProvider);

        val options = new VertxOptions
        options.mergePoolSizes
        Vertx.vertx(options).deployVerticle(new AlexaServer => [ portAutoDetect ])

        new Thread([for (;;) Thread.sleep(MORE_THAN_ONE_YEAR)], "alexa-keepalive").start // wait in some other thread
    }
}
