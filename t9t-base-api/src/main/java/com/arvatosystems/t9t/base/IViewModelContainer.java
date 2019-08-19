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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Marker interface used by reflections. */
public interface IViewModelContainer {
    /** A lookup to retrieve the factory classes for CRUD operations by ID. */
    public static final Map<String, CrudViewModel<?,?>> CRUD_VIEW_MODEL_REGISTRY = new ConcurrentHashMap<String, CrudViewModel<?,?>>(100);

    /** A lookup to retrieve the view model ID by grid ID. */
    public static final Map<String, String> VIEW_MODEL_BY_GRID_ID_REGISTRY = new ConcurrentHashMap<String, String>(100);
}
