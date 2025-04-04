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
package com.arvatosystems.t9t.zkui.components.dropdown28.nodb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.zkoss.zul.Combobox;

import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28BasicFactory;

public final class Dropdown28Registry {
    // static fields and methods
    private static final Map<String, IDropdown28BasicFactory<Combobox>> REGISTRY = new ConcurrentHashMap<String, IDropdown28BasicFactory<Combobox>>(50);

    private Dropdown28Registry() { }

    /** Registers a new factory so that it can be retrieved by its ID. */
    public static void register(IDropdown28BasicFactory<Combobox> factory) {
        REGISTRY.put(factory.getDropdownId(), factory);
    }

    /** Returns the dropdown factory of the given type, or throw an Exception if none has been registered for the specified name. */
    public static IDropdown28BasicFactory<Combobox> requireFactoryById(String dropdownId) {
        IDropdown28BasicFactory<Combobox> factory = REGISTRY.get(dropdownId);
        if (factory == null)
            throw new RuntimeException("no dropdown of ID " + dropdownId + " registered");
        return factory;
    }

    /** Factory method: get a dropdown of the specified ID. */
    public static Combobox getDropdownById(String dropdownId) {
        return requireFactoryById(dropdownId).createInstance();
    }

    /** Returns the dropdown factory of the given type, or null if none has been registered for the specified name. */
    public static IDropdown28BasicFactory getFactoryById(String dropdownId) {
        return REGISTRY.get(dropdownId);
    }
}
