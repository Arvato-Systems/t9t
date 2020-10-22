/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.plugins.be.impl;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.plugins.services.PluginDispatcher;
import com.arvatosystems.t9t.plugins.services.PluginManager;
import com.arvatosystems.t9t.plugins.services.PluginMethod;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class PluginDispatcherImpl<I,O> implements PluginDispatcher {
    protected final PluginManager pluginManager = Jdp.getRequired(PluginManager.class);

    @Override
    public void execute(final String pluginApiId, final String pluginApiQualifier, final RequestContext ctx, final Object in, final Object out) {
        // obtain a tenant specific implementation, if it exists
        PluginMethod<I,O> plugin;
        try {
             plugin = pluginManager.getPluginMethod(ctx.tenantRef , pluginApiId, pluginApiQualifier);
        } catch (Exception e) {
            try {
                plugin = pluginManager.getPluginMethod(T9tConstants.GLOBAL_TENANT_REF42, pluginApiId, pluginApiQualifier);
            } catch (Exception e2) {
                throw new T9tException(T9tException.NO_PLUGIN_AVAILABLE, ctx.tenantId + pluginApiQualifier == null ? ":" + pluginApiId : ":" + pluginApiId + pluginApiQualifier );
            }
        }

        // invoke the plugin
        plugin.execute(ctx, (I)in, (O)out);
    }
}
