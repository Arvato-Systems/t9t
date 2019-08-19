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
package com.arvatosystems.t9t.base.vertx.impl;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;

/*
 * sample code the server is based on (plain Java, for comparison)
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Proxy extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        HttpClient client = vertx.createHttpClient(new HttpClientOptions());
        vertx.createHttpServer().requestHandler(req -> {
            System.out.println("Proxying request: " + req.uri());
            HttpClientRequest c_req = client.request(req.method(), 8282, "localhost", req.uri(), c_res -> {
                System.out.println("Proxying response: " + c_res.statusCode());
                req.response().setChunked(true);
                req.response().setStatusCode(c_res.statusCode());
                req.response().headers().setAll(c_res.headers());
                c_res.handler(data -> {
                    System.out.println("Proxying response body: " + data.toString("ISO-8859-1"));
                    req.response().write(data);
                });
                c_res.endHandler((v) -> req.response().end());
            });
            c_req.setChunked(true);
            c_req.headers().setAll(req.headers());
            req.handler(data -> {
                System.out.println("Proxying request body " + data.toString("ISO-8859-1"));
                c_req.write(data);
            });
            req.endHandler((v) -> c_req.end());
        }).listen(8080);
    }
}
