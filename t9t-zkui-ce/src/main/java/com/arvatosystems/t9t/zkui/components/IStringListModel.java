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
package com.arvatosystems.t9t.zkui.components;

import jakarta.annotation.Nonnull;

import java.util.List;

public interface IStringListModel {

    /**
     * Returns a list of strings that can be used as a model for components like dropdowns.
     *
     * @return a list of strings
     */
    @Nonnull
    List<String> getListModel();

    /**
     * Returns the delimiter used for the list.
     *
     * @return the delimiter
     */
    @Nonnull
    default String getListDelimiter() {
        return ",";
    }

    /**
     * If a new entry can be added into the list model.
     *
     * @return boolean
     */
    default boolean createNewEntry() {
        return false;
    };
}
