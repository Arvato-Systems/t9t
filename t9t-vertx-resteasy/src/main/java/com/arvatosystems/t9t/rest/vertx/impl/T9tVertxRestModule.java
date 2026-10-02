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

import jakarta.ws.rs.SeBootstrap;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.base.vertx.IRestModule;

@Singleton
public class T9tVertxRestModule implements IRestModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tVertxRestModule.class);

    private volatile SeBootstrap.Instance instance = null;

    /**
     * Starts the REST (Jakarta-RS) API via {@code SeBootstrap} / {@code VertxEmbeddedServer}.
     * Note: the {@code vertx} parameter is retained for interface compatibility only; the actual Vertx instance used by the
     * embedded server is obtained from JDP via {@link T9tVertxManager}. Requests with a body larger than the default 10 MB
     * limit ({@code dev.resteasy.vertx.server.max.request.size}) will be rejected with HTTP 413 by the embedded server.
     */
    @Override
    public void createRestServer(final Vertx vertx, final int port) {
        LOGGER.info("Starting http server for REST on port {}", port);

        final SeBootstrap.Configuration config = SeBootstrap.Configuration.builder()
                .protocol("HTTP")
                .host("0.0.0.0")
                .port(port)
                .rootPath("/")
                .build();

        SeBootstrap.start(new T9tRestApplication(), config)
            .thenAccept(it -> {
                this.instance = it;
                LOGGER.info("REST API listening on 0.0.0.0:{}", port);
            })
            .exceptionally(t -> {
                LOGGER.error("Could not start REST server on port {}", port, t);
                throw new RuntimeException("REST server start failed", t);
            })
            .toCompletableFuture()
            .join();
    }

    @Override
    public void stopRestServer() {
        final SeBootstrap.Instance it = this.instance;
        if (it != null) {
            it.stop().toCompletableFuture().join();
            this.instance = null;
            LOGGER.info("REST server stopped");
        } else {
            LOGGER.debug("REST server was not started");
        }
    }
}
