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
package com.arvatosystems.t9t.base.vertx;

import org.slf4j.Logger;

import com.arvatosystems.t9t.base.api.ServiceResponse;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.util.CharTestsASCII;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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
    default int compareTo(final IServiceModule that) {
        final int num = getExceptionOffset() - that.getExceptionOffset();
        return num != 0 ? num : getModuleName().compareTo(that.getModuleName());
    }

    static String asciiSubString(@Nonnull final String s, final int maxLength) {
        final int max = s.length() < maxLength ? s.length() : maxLength;
        for (int i = 0; i < max; ++i) {
            if (!CharTestsASCII.isAsciiPrintable(s.charAt(i)))
                return s.substring(0, i);
        }
        return s;   // whole string is printable ASCII
    }

    static boolean badOrMissingAuthHeader(@Nonnull final RoutingContext ctx, @Nullable final String authHeader, @Nonnull final Logger logger) {
        if (authHeader == null) {
            logger.debug("Request without authorization header");
            final HttpServerResponse r = ctx.response();
            r.putHeader("WWW-Authenticate", "Basic realm=\"t9t\", charset=\"UTF-8\"");
            r.setStatusCode(401);
            r.setStatusMessage("Unauthorized");
            r.end();
            return true;
        }
        if (authHeader.length() < 8) {
            logger.debug("Request authorization header  too short (length = {})", authHeader.length());
            IServiceModule.error(ctx, 403, "HTTP Authorization header too short");
            return true;
        }
        return false;
    }

    static void error(@Nonnull final RoutingContext ctx, final int errorCode, @Nullable final String msg) {
        final HttpServerResponse r = ctx.response();
        r.setStatusCode(errorCode);
        if (msg != null)
            r.setStatusMessage(asciiSubString(msg, 200));
        r.end();
    }
}
