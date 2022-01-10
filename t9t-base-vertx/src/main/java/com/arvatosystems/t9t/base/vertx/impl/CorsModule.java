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

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.vertx.IServiceModule;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("cors")
@Dependent
public class CorsModule implements IServiceModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(CorsModule.class);

    @Override
    public int getExceptionOffset() {
        return -1; // must be before the auth handler
    }

    @Override
    public String getModuleName() {
        return "cors";
    }

    @Override
    public void mountRouters(final Router router, final Vertx vertx, final IMessageCoderFactory<BonaPortable, ServiceResponse, byte[]> coderFactory) {
        LOGGER.info("Registering module {}", getModuleName());
        HttpUtils.addCorsOptionsRouter(router, "rpc");
    }
}
