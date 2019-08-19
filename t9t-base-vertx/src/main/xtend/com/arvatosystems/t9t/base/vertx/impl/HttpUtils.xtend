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

import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router

import static io.vertx.core.http.HttpHeaders.*
import de.jpaw.annotations.AddLogger

@AddLogger
class HttpUtils {
    static final String ALLOWED_CORS_HEADERS = '''«CONTENT_TYPE», Charset, «ACCEPT_CHARSET», «CONTENT_LENGTH», «AUTHORIZATION», «ACCEPT», «USER_AGENT», X-Invoker-Ref, X-Plan-Date''';

    def static void addHttpHeaderForCorsOptionsRequest(HttpServerResponse it, String origin) {
        putHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin)
        putHeader(ACCESS_CONTROL_ALLOW_METHODS, "POST")
        putHeader(ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_CORS_HEADERS)
        putHeader(CONTENT_TYPE, "text/html; charset=utf-8")
    }

    def static void addCorsOptionsRouter(Router router, String moduleName) {
        router.options("/" + moduleName).handler [
            val origin      = request.headers.get(ORIGIN)
            val rqMethod    = request.headers.get(ACCESS_CONTROL_REQUEST_METHOD)
            val rqHeaders   = request.headers.get(ACCESS_CONTROL_REQUEST_HEADERS) ?: ""
            LOGGER.debug("CORS preflight request to /{} for origin {}, method {}, headers {} received", moduleName, origin, rqMethod, rqHeaders)
            if (origin !== null && rqMethod !== null) {
                response.addHttpHeaderForCorsOptionsRequest(origin)
            }
            response.end
        ]
    }

    def static stripCharset(String s) {
        if (s === null)
            return s;
        val ind = s.indexOf(';')
        if (ind >= 0)
            return s.substring(0, ind)      // strip off optional ";Charset..." portion
        else
            return s
    }
}
