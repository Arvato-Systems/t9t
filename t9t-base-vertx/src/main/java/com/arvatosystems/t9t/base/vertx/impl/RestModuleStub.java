/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.vertx.IRestModule;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;
import io.vertx.core.Vertx;

@Singleton
@Fallback
public class RestModuleStub implements IRestModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestModuleStub.class);

    @Override
    public void createRestServer(Vertx vertx, int port) {
        LOGGER.error("Cannot create REST module at port {} - implementation not deployed", port);
        throw new RuntimeException("No REST module available");
    }
}
