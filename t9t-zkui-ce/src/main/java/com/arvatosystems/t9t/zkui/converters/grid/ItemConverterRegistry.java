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
package com.arvatosystems.t9t.zkui.converters.grid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import jakarta.annotation.Nonnull;

public final class ItemConverterRegistry {
    private static final Map<String, IItemConverter<?>> REGISTRY = new ConcurrentHashMap<String, IItemConverter<?>>(50);

    private ItemConverterRegistry() { }


    public static void register(@Nonnull final String key, @Nonnull IItemConverter<?> itemConverter) {
        REGISTRY.put(key, itemConverter);
    }

    @Nonnull
    public static IItemConverter<?> getConverter(@Nonnull final String fieldName, @Nonnull final FieldDefinition meta) {
        final String className = "ref".equals(meta.getBonaparteType()) ? meta.getDataType() : meta.getBonaparteType();
        final IItemConverter<?> classConverterWithFieldName = REGISTRY.get(className + ":" + fieldName);
        if (classConverterWithFieldName != null) {
            return classConverterWithFieldName.getInstance(fieldName, meta);
        }
        final IItemConverter<?> classConverter = REGISTRY.get(className);
        if (classConverter != null) {
            return classConverter.getInstance(fieldName, meta);
        }
        return DefaultConverter.INSTANCE;
    }
}
