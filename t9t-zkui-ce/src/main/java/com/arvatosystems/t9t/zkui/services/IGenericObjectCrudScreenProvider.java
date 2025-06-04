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
package com.arvatosystems.t9t.zkui.services;

import jakarta.annotation.Nonnull;

import java.util.List;

public interface IGenericObjectCrudScreenProvider {

    /**
     * Return the location of the .zul file of the CRUD screen
     * @return screen URI
     */
    @Nonnull
    String getScreenURI();

    /**
     * Return list of locations of the .zul file of the CRUD screen
     * @return list screen URI
     */
    @Nonnull
    default List<String> getScreenURIs() {
        return List.of(getScreenURI());
    }
}
