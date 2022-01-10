/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import static io.vertx.core.http.HttpHeaders.ACCEPT;
import static io.vertx.core.http.HttpHeaders.ACCEPT_CHARSET;
import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;
import static io.vertx.core.http.HttpHeaders.CONTENT_LENGTH;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpHeaders.USER_AGENT;

import io.vertx.core.AsyncResult;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpUtils {

    private HttpUtils() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    public static final String ALLOWED_CORS_HEADERS;
    public static final String SEPARATOR = ", ";
    public static final String CHARSET = "Charset";
    public static final String X_INVOKER_REF = "X-Invoker-Ref";
    public static final String X_PLAN_DATE = "X-Plan-Date";
    public static final String EXT_CT = "application/json";
    public static final String INT_CT_42 = "application/bonjson"; // must propagate a different content type to fortytwo

    static {
        List<CharSequence> headers = Arrays.asList(CONTENT_TYPE, CHARSET, ACCEPT_CHARSET, CONTENT_LENGTH,
                AUTHORIZATION, ACCEPT, USER_AGENT, X_INVOKER_REF, X_PLAN_DATE);
        ALLOWED_CORS_HEADERS = String.join(SEPARATOR, headers);
    }

    public static void addHttpHeaderForCorsOptionsRequest(HttpServerResponse resp, String origin) {
        resp.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        resp.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST");
        resp.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_CORS_HEADERS);
        resp.putHeader(HttpHeaders.CONTENT_TYPE, "text/html; charset=utf-8");
    }

    public static void addCorsOptionsRouter(Router router, String path) {
        router.options(path).handler((RoutingContext ctx) -> {
            final String origin      = ctx.request().headers().get(HttpHeaders.ORIGIN);
            final String rqMethod      = ctx.request().headers().get(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
            final String rqHeaders      = ctx.request().headers().get(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS) == null
                    ? "" : ctx.request().headers().get(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
            LOGGER.debug("CORS preflight request to {} for origin {}, method {}, headers {} received", path, origin, rqMethod, rqHeaders);
            if (origin != null && rqMethod != null) {
                addHttpHeaderForCorsOptionsRequest(ctx.response(), origin);
            }
            ctx.response().end();
        });
    }

    public static String stripCharset(String s) {
        if (s == null) {
            return s;
        }
        final int ind = s.indexOf(';');
        if (ind >= 0) {
            return s.substring(0, ind); // strip off optional ";Charset..." portion
        }
        return s;
    }

    private static void map(MultiMap map, String from, String to) {
        final String ct = map.get(HttpHeaders.CONTENT_TYPE);
        if (ct != null && ct.equals(from)) {
            // exchange
            map.set(HttpHeaders.CONTENT_TYPE, to);

            // TODO: have to map ACCEPT as well?
            final String acc = map.get(HttpHeaders.ACCEPT);
            if (acc != null)
                map.set(HttpHeaders.ACCEPT, to);
        }
    }

    public static void proxy(HttpClient client, HttpServerRequest req, String host, int port, boolean mapContentType, String uriPrefix) {
        client.request(req.method(), port, host, uriPrefix + req.uri(), (AsyncResult<HttpClientRequest> a_req) -> {
            final HttpClientRequest cReq = a_req.result();
            if (cReq == null) {
                return;
            }

            cReq.setChunked(true);
            cReq.headers().setAll(req.headers());
            if (mapContentType) {
                HttpUtils.map(cReq.headers(), EXT_CT, INT_CT_42);
            }

            cReq.response().onComplete((AsyncResult<HttpClientResponse> a_resp) -> {
                final HttpClientResponse cRes = a_resp.result();
                System.out.println("Proxying response: " + cRes.statusCode());

                req.response().setChunked(true);
                req.response().setStatusCode(cRes.statusCode());
                req.response().headers().setAll(cRes.headers());
                if (mapContentType)
                    HttpUtils.map(req.response().headers(), INT_CT_42, EXT_CT);
                cRes.handler((buffer) -> req.response().write(buffer));
                cRes.endHandler((v) -> req.response().end());
            });
        });
    }

}
