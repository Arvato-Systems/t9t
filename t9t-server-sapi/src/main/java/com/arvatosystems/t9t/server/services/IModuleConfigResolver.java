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
package com.arvatosystems.t9t.server.services;

import com.arvatosystems.t9t.base.moduleCfg.ModuleConfigDTO;

import jakarta.annotation.Nonnull;

/**
 * Implementations of this interface should be singletons to ensure one separate instance per module.
 * They are located in the persistence modules.
 */
public interface IModuleConfigResolver<T extends ModuleConfigDTO> {
    /**
     * Reads a module configuration from the database, or a default, in the following preference:
     * <ul>
     * <li>the value in the current cache
     * <li>the value for the specific tenant
     * <li>the value for the global tenant
     * <li>a hardcoded default, as provided by getDefaultModuleConfiguration
     * </ul>
     *
     * This method will never return null.
     * If the result was not taken from the cache, it will be cached for 60 seconds, limiting the number of required database lookups.
     * @return
     */
    @Nonnull
    T getModuleConfiguration();

    /**
     * Reads a module configuration from the database, or a default, in the following preference:
     * <ul>
     * <li>the value for the specific tenant
     * <li>the value for the global tenant
     * <li>a hardcoded default, as provided by getDefaultModuleConfiguration
     * </ul>
     *
     * This method will never return null.
     * @return
     */
    @Nonnull
    T getUncachedModuleConfiguration();

    /**
     * Returns a sensible default for the module configuration.
     * It should be possible to use a system without any configuration made, and it should fall back to meaningful defaults then,
     * which are provided by this method.
     * @return
     */
    @Nonnull
    T getDefaultModuleConfiguration();

    /**
     * Updates module configuration with a new one. Writes to the database and updates the local cache.
     * @return
     */
    void updateModuleConfiguration(@Nonnull T newCfg);
}
