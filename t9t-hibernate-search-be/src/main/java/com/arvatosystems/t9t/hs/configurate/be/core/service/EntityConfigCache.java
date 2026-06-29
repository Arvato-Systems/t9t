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
package com.arvatosystems.t9t.hs.configurate.be.core.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;

import de.jpaw.bonaparte.util.FreezeTools;

import com.arvatosystems.t9t.hs.configurate.model.EmbeddedIndexEntityConfig;
import com.arvatosystems.t9t.hs.configurate.model.EntityConfig;
import com.arvatosystems.t9t.hs.configurate.model.FieldConfig;

public class EntityConfigCache {

    private final List<EntityConfig> entities;
    private final Map<String, List<String>> entityToIndexFieldMap;

    public EntityConfigCache(@Nonnull final List<EntityConfig> entities) {
        this.entities = entities;
        this.entityToIndexFieldMap = new HashMap<>(FreezeTools.getInitialHashMapCapacity(entities.size()));
        for (final EntityConfig entityConfig : entities) {
            if (entityConfig.getFields() != null) {
                for (final FieldConfig fieldConfig: entityConfig.getFields()) {
                    entityToIndexFieldMap.computeIfAbsent(entityConfig.getClassName(),
                        k -> new ArrayList<>(entityConfig.getFields().size())).add(fieldConfig.getName());
                }
            }
            if (entityConfig.getEmbeddedIndexEntities() != null) {
                for (final EmbeddedIndexEntityConfig embeddedConfig : entityConfig.getEmbeddedIndexEntities()) {
                    entityToIndexFieldMap.computeIfAbsent(entityConfig.getClassName(),
                            k -> new ArrayList<>(entityConfig.getEmbeddedIndexEntities().size())).add(embeddedConfig.getTargetEntity());
                }
            }
        }
    }

    @Nonnull
    public List<EntityConfig> getEntities() {
        return entities;
    }

    @Nonnull
    public List<String> getIndexedFields(@Nonnull final String entityClassName) {
        final List<String> fields = entityToIndexFieldMap.get(entityClassName);
        return fields != null ? fields : Collections.emptyList();
    }
}
