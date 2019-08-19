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
package com.arvatosystems.t9t.base.vertx;

import com.arvatosystems.t9t.base.api.ServiceResponse;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.util.CharTestsASCII;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/** A ServiceModule is a microservice like portion which owns its own
 * UI and provides http based paths.
 *
 * Modules are detected via Jdp
 * @author mbi
 *
 */
public interface IServiceModule extends Comparable<IServiceModule> {
    int getExceptionOffset();
    String getModuleName();
    void mountRouters(Router router, Vertx vertx, IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory);

    @Override
    default int compareTo(IServiceModule that) {
        int num = getExceptionOffset() - that.getExceptionOffset();
        return num != 0 ? num : getModuleName().compareTo(that.getModuleName());
    }

    static String asciiSubString(String s, int maxLength) {
        int max = s.length() < maxLength ? s.length() : maxLength;
        for (int i = 0; i < max; ++i) {
            if (!CharTestsASCII.isAsciiPrintable(s.charAt(i)))
                return s.substring(0, i);
        }
        return s;   // whole string is printable ASCII
    }

    static void error(RoutingContext ctx, int errorCode) {
        HttpServerResponse r = ctx.response();
        r.setStatusCode(errorCode);
        r.end();
    }

    static void error(RoutingContext ctx, int errorCode, String msg) {
        HttpServerResponse r = ctx.response();
        r.setStatusCode(errorCode);
        if (msg != null)
            r.setStatusMessage(asciiSubString(msg, 200));
        r.end();
    }
}
