/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;

public interface ILeanGridConfigContainer {
    public static final Map<String, UILeanGridPreferences> LEAN_GRID_CONFIG_REGISTRY = new ConcurrentHashMap<String, UILeanGridPreferences>(100);

    /** Method to return the list of resources (JSON files in the file system) which contain the default grid configuration.
     * @return List of configuration names.
     */
    List<String> getResourceNames();
}
