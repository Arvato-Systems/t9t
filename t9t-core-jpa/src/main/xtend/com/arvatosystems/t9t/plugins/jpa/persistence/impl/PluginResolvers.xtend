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
package com.arvatosystems.t9t.plugins.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.active.AutoResolver42
import com.arvatosystems.t9t.plugins.LoadedPluginRef
import com.arvatosystems.t9t.plugins.PluginLogRef
import com.arvatosystems.t9t.plugins.jpa.entities.LoadedPluginEntity
import com.arvatosystems.t9t.plugins.jpa.entities.PluginLogEntity

@AutoResolver42
class PluginResolvers {
    @AllCanAccessGlobalTenant
    def LoadedPluginEntity     getLoadedPluginEntity        (LoadedPluginRef ref) {}
    def PluginLogEntity        getPluginLogEntity           (PluginLogRef ref) {}
}
