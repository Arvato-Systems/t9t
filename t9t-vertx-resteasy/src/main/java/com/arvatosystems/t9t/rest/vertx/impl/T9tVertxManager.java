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
package com.arvatosystems.t9t.rest.vertx.impl;

import dev.resteasy.vertx.VertxManager;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Jdp;

/**
 * Bridges RESTEasy's {@code VertxManager} lookup to the shared Vertx instance which is bound in JDP by {@code T9tServer}.
 * Registered via {@code META-INF/services/dev.resteasy.vertx.VertxManager}. Priority is lower than the default (1000) so
 * this implementation takes precedence over {@code DefaultVertxManager}.
 */
public class T9tVertxManager implements VertxManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tVertxManager.class);

    @Override
    public Vertx vertx() {
        return Jdp.getRequired(Vertx.class);
    }

    @Override
    public void close() {
        // no-op: the Vertx instance is owned by the application, not by RESTEasy; closing it would kill the main HTTP server,
        // event bus, and all verticles.
        LOGGER.debug("VertxManager.close() - no-op, Vertx instance is shared with the application");
    }

    @Override
    public int priority() {
        return 0;
    }
}
