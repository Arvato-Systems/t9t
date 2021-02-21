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
package com.arvatosystems.t9t.plugins.services;

import com.arvatosystems.t9t.plugins.PluginInfo;

import de.jpaw.util.ByteArray;

/**
 * Defines the Plugin Manager for loading and unloading Plugins and PluginMethods
 **/
public interface IPluginManager {
    /** Load all classes provided by the plugin and return its info structure. */
    PluginInfo loadPlugin(Long tenantRef, ByteArray pluginData);

    /** Remove all classes provided by the plugin and close its classloader. */
    boolean removePlugin(Long tenantRef, String pluginId);

    /** Retrieve a reference to a preloaded plugin method instance. */ 
    PluginMethod getPluginMethod(Long tenantRef, String pluginId, String qualifier);
}
