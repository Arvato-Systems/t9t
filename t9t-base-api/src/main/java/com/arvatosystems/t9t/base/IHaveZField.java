/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import jakarta.annotation.Nullable;

/**
 * Marker interface for classes which support getZ() and setZ().
 * Works for DTOs as well as entities, but primarily intended for entities to simplify interaction with the performance sensitive Z field getter and setters.
 */
public interface IHaveZField {
    /** Standard Java setter for z. */
    void setZ(@Nullable Map<String, Object> z);

    /** Standard Java getter for z. */
    @Nullable Map<String, Object> getZ();
}
