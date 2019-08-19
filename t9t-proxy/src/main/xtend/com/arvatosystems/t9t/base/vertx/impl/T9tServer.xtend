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

import com.martiansoftware.jsap.FlaggedOption
import com.martiansoftware.jsap.JSAP
import com.martiansoftware.jsap.Parameter
import com.martiansoftware.jsap.SimpleJSAP
import com.martiansoftware.jsap.Switch
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.StaticHandler
import java.util.ArrayList
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class T9tServer extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tServer)
    private static final long MORE_THAN_ONE_YEAR = 500L * 86400L * 1000L;     // JVM update due at least once per year (at least every 500 days)
    private static boolean cors     = false;
    private static int     port     = 8024           // default for http
    private static int     port28   = 8025           // default for http
    private static String  filePath = null;
    private static String  corsParm = "*";

    def static void parseCommandLine(String[] args) {
        val options = new ArrayList<Parameter>();
        options.add(new FlaggedOption("filePath", JSAP.STRING_PARSER,  filePath,                    JSAP.NOT_REQUIRED, 'f', "filePath", "path to serve files for /fs from"));
        options.add(new FlaggedOption("port",     JSAP.INTEGER_PARSER, Integer.toString(port),      JSAP.NOT_REQUIRED, 'p', "port",     "listener port http (0 to disable)"));
        options.add(new FlaggedOption("port28",   JSAP.INTEGER_PARSER, Integer.toString(port28),    JSAP.NOT_REQUIRED, 'T', "port28",   "listener port for t9t server"));
        options.add(new FlaggedOption("corsParm", JSAP.STRING_PARSER,  "*",                         JSAP.NOT_REQUIRED, 'C', "corsParm", "parameter to the CORS handler"));
        options.add(new Switch       ("cors",                                                                          'X', "cors",     "activate CORS handler"));

        val commandLineOptions = new SimpleJSAP("t9t server", "Runs a simple vert.x / t9t based server", options.toArray(newArrayOfSize(options.size)))
        val cmd = commandLineOptions.parse(args);
        if (commandLineOptions.messagePrinted()) {
            System.err.println("(use option --help for usage)");
            System.exit(1);
        }
        filePath = cmd.getString("filePath");
        port     = cmd.getInt("port");
        port28   = cmd.getInt("port28");
        cors     = cmd.getBoolean("cors")
        corsParm = cmd.getString("corsParm");
    }

    override void start() {
        super.start

        val t9tClient = vertx.createHttpClient(new HttpClientOptions());

        if (port > 0) {
            val router = Router.router(vertx) => [
                // route.handler(BodyHandler.create)     // must be before any possible execBlocking, see https://github.com/vert-x3/vertx-web/issues/198
                HttpUtils.addCorsOptionsRouter(it, "/rpc")

                // forward /rpc to proxy handler 1
                post("/rpc").handler [
                    LOGGER.info("Proxying t9t request for {}", request.uri)
                    HttpUtils.proxy(t9tClient, request, "localhost", port28, false, "")
                ]

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
                    LOGGER.info("Setting up cors handler for origin {}", corsParm)
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
                requestHandler [ router.accept(it) ]
                listen(port)
            ]
        }
    }

    def static void main(String[] args) throws Exception {
        LOGGER.info('''t9t proxy server starting...''')

        parseCommandLine(args);

        Vertx.vertx.deployVerticle(new T9tServer)

        new Thread([Thread.sleep(MORE_THAN_ONE_YEAR)]).start // wait in some other thread
    }
}
