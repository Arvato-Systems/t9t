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

import java.util.List;

import com.arvatosystems.t9t.plugins.PluginInfo;
import com.arvatosystems.t9t.plugins.PluginMethodInfo;

/**
 * Defines the API a plugin has to provide.
 **/
public interface Plugin {
    /** Starts the plugin (instantiates implementations). */
    void startup();

    /** Performs a cleanup, before the plugin is unloaded. */
    void shutdown();

    /** Retrieves information about the plugin. */
    PluginInfo getInfo();

    /** get the list of implemented methods. Never returns null. */
    List<PluginMethodInfo> getMethods();
}
