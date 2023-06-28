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
package com.arvatosystems.t9t.plugins.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.plugins.request.ExecutePluginV1Request;
import com.arvatosystems.t9t.plugins.request.ExecutePluginV1Response;
import com.arvatosystems.t9t.plugins.services.IRequestHandlerPlugin;

/**
 * Demo-Plugin for request handlers: returns the square of its numeric parameter, prints the text parameter to server log.
 */
public class DemoRequestHandler implements IRequestHandlerPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoRequestHandler.class);

    @Override
    public void execute(RequestContext ctx, ExecutePluginV1Request in, ExecutePluginV1Response out) {
        LOGGER.info("demo request handler plugin: text={}, num={}", in.getTextParameter(), in.getNumParameter());
        out.setNumResult(in.getNumParameter() * in.getNumParameter());
        LOGGER.info("demo request handler plugin: returning num={}", out.getNumResult());
    }

    @Override
    public String getQualifier() {
        return "demo";
    }

    @Override
    public int versionMajor() {
        return 1;
    }

    @Override
    public int versionMinMinor() {
        return 0;
    }
}
