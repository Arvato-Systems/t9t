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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.ServerConfiguration;
import com.arvatosystems.t9t.plugins.PluginInfo;
import com.arvatosystems.t9t.plugins.request.UploadPluginRequest;
import com.arvatosystems.t9t.plugins.request.UploadPluginResponse;
import com.arvatosystems.t9t.plugins.services.IPluginManager;

import de.jpaw.dp.Jdp;

public class UploadPluginRequestHandler extends AbstractRequestHandler<UploadPluginRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadPluginRequestHandler.class);

    private final IPluginManager pluginManager = Jdp.getRequired(IPluginManager.class);

    @Override
    public UploadPluginResponse execute(final RequestContext ctx, final UploadPluginRequest rq) throws Exception {
        final ServerConfiguration serverConfig = ConfigProvider.getConfiguration().getServerConfiguration();
        if (serverConfig == null || !Boolean.TRUE.equals(serverConfig.getEnablePlugins())) {
            LOGGER.info("Plugin upload not enabled via configuration");
            throw new T9tException(T9tException.PLUGINS_NOT_ENABLED);
        }
        final PluginInfo info = pluginManager.loadPlugin(ctx.tenantId, rq.getJarFile());
        final UploadPluginResponse resp = new UploadPluginResponse();
        LOGGER.info("Temporarily loaded plugin {} of version {}", info.getPluginId(), info.getVersion());
        resp.setPluginInfo(info);
        return resp;
    }
}
