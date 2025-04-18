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
package com.arvatosystems.t9t.base.vertx.impl;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.vertx.IServiceModule;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This path provides for server side connection closes in order to avoid messages such as
 * Feb 27, 2016 2:44:53 PM io.vertx.core.net.impl.ConnectionBase
 * SEVERE: java.io.IOException: Connection reset by peer
 * which occur when the client closes the connection.
 * It can be used to invalidate the JWT as well.
 */
@Named("logout")
@Dependent
public class LogoutModule implements IServiceModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutModule.class);

    @Override
    public int getExceptionOffset() {
        return 10_000;
    }

    @Override
    public String getModuleName() {
        return "logout";
    }

    @Override
    public void mountRouters(final Router router, final Vertx vertx, final IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory) {
        LOGGER.info("Registering module {}", getModuleName());
        router.post("/logout").handler((final RoutingContext ctx) -> {
            LOGGER.debug("POST /logout received");
            ctx.response().end();
            ctx.response().close();
        });
    }
}
