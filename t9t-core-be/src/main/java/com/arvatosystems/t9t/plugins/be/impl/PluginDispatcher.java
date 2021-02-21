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

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.plugins.services.IPluginDispatcher;
import com.arvatosystems.t9t.plugins.services.IPluginManager;
import com.arvatosystems.t9t.plugins.services.PluginMethod;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class PluginDispatcher<I, O> implements IPluginDispatcher<I, O> {
    protected final IPluginManager pluginManager = Jdp.getRequired(IPluginManager.class);

    @Override
    public void execute(final String pluginApiId, final String pluginApiQualifier, final RequestContext ctx, final I in, final O out) {
        // obtain a tenant specific implementation, if it exists
        final PluginMethod<I, O> plugin = pluginManager.getPluginMethod(ctx.tenantRef, pluginApiId, pluginApiQualifier);

        // invoke the plugin
        plugin.execute(ctx, in, out);
    }
}
