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

import com.arvatosystems.t9t.plugins.PluginMethodInfo;

/**
 * Defines the API to call a method of a plugin. A single plugin can support multiple of these APIs.
 * This is an "abstract" interface, which means that any meaningful plugin must implement some
 * extension of this interface, which also defines the execution / workload methods.
 **/
public interface PluginMethod {
    /** Returns the API implemented. Will usually be provided by a default method. */
    String implementsApi();

    /** Returns the qualifier of this specific method. */
    String getQualifier();

    /** Returns the major revision of the API implemented. */
    int versionMajor();

    /** Returns the minimal minor revision of the API supported. */
    int versionMinMinor();

    /** Returns the information above in a consistent structure. */
    default PluginMethodInfo getInfo() {
        final PluginMethodInfo info = new PluginMethodInfo();

        info.setImplementsApi(implementsApi());
        info.setQualifier(getQualifier());
        info.setVersionMajor(versionMajor());
        info.setVersionMinMinor(versionMinMinor());
        return info;
    }
}
