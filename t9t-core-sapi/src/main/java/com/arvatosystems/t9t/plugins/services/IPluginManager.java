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
package com.arvatosystems.t9t.plugins.services;

import com.arvatosystems.t9t.plugins.PluginInfo;

import de.jpaw.util.ByteArray;

/**
 * Defines the Plugin Manager for loading and unloading Plugins and PluginMethods
 **/
public interface IPluginManager {
    /**
     * Loads all classes provided by the plugin and return its info structure.
     */
    PluginInfo loadPlugin(String tenantId, ByteArray pluginData);

    /**
     * Removes all classes provided by the plugin and close its classloader.
     */
    boolean removePlugin(String tenantId, String pluginId);

    /**
     * Retrieves a reference to a preloaded plugin method instance and checks its expected type.
     * May return null if no method has been registered for this tenant.
     */
    <R extends PluginMethod> R getPluginMethod(String tenantId, String pluginId, String qualifier, Class<R> requiredType, boolean allowNulls);

    /**
     * Retrieves a reference to a preloaded plugin method instance and checks its expected type.
     * May return null if no method has been registered for this tenant.
     * Convenience method without tenantId. Retrieves the tenantId from RequestContext and invokes the method above.
     */
    <R extends PluginMethod> R getPluginMethod(String pluginId, String qualifier, Class<R> requiredType, boolean allowNulls);
}
