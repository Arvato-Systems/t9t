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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public final class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    private static final String ALLOWED_CORS_HEADERS = HttpHeaders.CONTENT_TYPE + ", Charset, "
                                                     + HttpHeaders.ACCEPT_CHARSET + ", "
                                                     + HttpHeaders.CONTENT_LENGTH + ", "
                                                     + HttpHeaders.AUTHORIZATION + ", "
                                                     + HttpHeaders.ACCEPT + ", "
                                                     + HttpHeaders.USER_AGENT + ", X-Invoker-Ref, X-Plan-Date";

    private HttpUtils() { }

    public static void addHttpHeaderForCorsOptionsRequest(final HttpServerResponse it, final String origin) {
        it.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        it.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST");
        it.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpUtils.ALLOWED_CORS_HEADERS);
        it.putHeader(HttpHeaders.CONTENT_TYPE, "text/html; charset=utf-8");
    }

    public static void addCorsOptionsRouter(final Router router, final String moduleName) {
        router.options("/" + moduleName).handler((final RoutingContext ctx) -> {
            final String origin = ctx.request().headers().get(HttpHeaders.ORIGIN);
            final String rqMethod = ctx.request().headers().get(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
            final String rqHeaders = ctx.request().headers().get(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS) == null ? ""
                    : ctx.request().headers().get(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
            LOGGER.debug("CORS preflight request to /{} for origin {}, method {}, headers {} received", moduleName, origin, rqMethod, rqHeaders);
            if (origin != null && rqMethod != null) {
                addHttpHeaderForCorsOptionsRequest(ctx.response(), origin);
            }
            ctx.response().end();
        });
    }

    public static String stripCharset(final String s) {
        if (s == null) {
            return s;
        }
        final int ind = s.indexOf(";");
        if (ind >= 0) {
            return s.substring(0, ind); // strip off optional ";Charset..." portion
        } else {
            return s;
        }
    }
}
