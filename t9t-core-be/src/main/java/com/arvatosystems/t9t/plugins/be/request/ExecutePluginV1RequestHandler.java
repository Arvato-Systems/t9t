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
package com.arvatosystems.t9t.plugins.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.plugins.request.ExecutePluginV1Request;
import com.arvatosystems.t9t.plugins.request.ExecutePluginV1Response;
import com.arvatosystems.t9t.plugins.services.IPluginManager;
import com.arvatosystems.t9t.plugins.services.IRequestHandlerPlugin;

import de.jpaw.dp.Jdp;

/**
 * This request handler is just a wrapper around functionality implemented in plugins.
 * It also serves as a simple example how to invoke a plugin.
 * Its input and output parameters are generic.
 * If pluggable request handlers with more specific parameters are required, it is advised to create a separate wrapper specifically for those.
 */
public class ExecutePluginV1RequestHandler extends AbstractRequestHandler<ExecutePluginV1Request> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutePluginV1RequestHandler.class);

    protected final IPluginManager pluginManager = Jdp.getRequired(IPluginManager.class);

    @Override
    public ExecutePluginV1Response execute(final RequestContext ctx, final ExecutePluginV1Request request) throws Exception {
        final ExecutePluginV1Response result = new ExecutePluginV1Response();
        LOGGER.info("Calling request handler plugin {} with parameters {}", request.getQualifier(), request);
        pluginManager.getPluginMethod(ctx.tenantRef, T9tConstants.PLUGIN_API_ID_REQUEST_HANDLER, request.getQualifier(), IRequestHandlerPlugin.class, false)
          .execute(ctx, request, result);
        LOGGER.info("Returning from request handler plugin {} with response {}", request.getQualifier(), result);
        return result;
    }
}
