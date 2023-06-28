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
package com.arvatosystems.t9t.plugins.services;

/**
 * Optional methods to be invoked when a plugin is registered, or removed.
 * If a plugin type requires use of these services, it must implement a @singleton instance of this interfcae, @Named by the pluginApiId.
 * An implementation can run tasks such as additional registering by Jdp, refreshing caches, creation of proxy / adapter instances etc.
 */
public interface IPluginMethodLifecycle {
    /** This method is called twice during plugin initialization, once before the method is added to the dispatcher, once afterwards. */
    default void registerPluginMethod(final String tenantId, final Plugin loadedPlugin, final PluginMethod method, final boolean before) { }

    /** This method is called twice during plugin initialization, once before the method is added to the dispatcher, once afterwards. */
    default void unregisterPluginMethod(final String tenantId, final Plugin loadedPlugin, final PluginMethod method, final boolean before) { }
}
