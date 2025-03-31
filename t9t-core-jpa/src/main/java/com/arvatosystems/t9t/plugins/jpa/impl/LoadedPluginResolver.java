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
package com.arvatosystems.t9t.plugins.jpa.impl;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractJpaResolver;
import com.arvatosystems.t9t.plugins.LoadedPluginDTO;
import com.arvatosystems.t9t.plugins.LoadedPluginRef;
import com.arvatosystems.t9t.plugins.jpa.entities.LoadedPluginEntity;
import com.arvatosystems.t9t.plugins.jpa.mapping.ILoadedPluginDTOMapper;
import com.arvatosystems.t9t.plugins.jpa.persistence.ILoadedPluginEntityResolver;
import com.arvatosystems.t9t.plugins.services.ILoadedPluginResolver;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class LoadedPluginResolver extends AbstractJpaResolver<LoadedPluginRef, LoadedPluginDTO, FullTrackingWithVersion, LoadedPluginEntity>
  implements ILoadedPluginResolver {

    public LoadedPluginResolver() {
        super("LoadedPlugin", Jdp.getRequired(ILoadedPluginEntityResolver.class), Jdp.getRequired(ILoadedPluginDTOMapper.class));
    }

    @Override
    public LoadedPluginRef createKey(final Long ref) {
        return ref == null ? null : new LoadedPluginRef(ref);
    }
}
