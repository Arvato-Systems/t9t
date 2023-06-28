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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;

/*
 * sample code the server is based on (plain Java, for comparison)
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Proxy extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        final HttpClient client = vertx.createHttpClient(new HttpClientOptions());
        vertx.createHttpServer().requestHandler(req -> {
            System.out.println("Proxying request: " + req.uri());
            client.request(req.method(), 8282, "localhost", req.uri(), asyncRequestHandler -> {

                final HttpClientRequest cReq = asyncRequestHandler.result();
                if (cReq == null) {
                    System.out.println("Proxying request is NULL!");
                    return;
                }

                cReq.setChunked(true);
                cReq.headers().setAll(req.headers());

                cReq.response().onComplete(responseHandler -> {
                    final HttpClientResponse response = responseHandler.result();
                    if (response == null) {
                        System.out.println("Proxying response is NULL!");
                        return;
                    }

                    System.out.println("Proxying response: " + response.statusCode());
                    req.response().setChunked(true);
                    req.response().setStatusCode(response.statusCode());
                    req.response().headers().setAll(response.headers());

                    response.handler(data -> {
                        System.out.println("Proxying response body: " + data.toString("ISO-8859-1"));
                        req.response().write(data);
                    });

                    response.endHandler((v) -> req.response().end());
                });

                req.handler(data -> {
                    System.out.println("Proxying request body " + data.toString("ISO-8859-1"));
                    cReq.write(data);
                });
                req.endHandler((v) -> cReq.end());
            });
        }).listen(8080);
    }
}
