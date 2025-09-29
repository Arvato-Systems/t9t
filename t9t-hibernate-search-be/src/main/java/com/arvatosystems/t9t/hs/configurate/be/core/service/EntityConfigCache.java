package com.arvatosystems.t9t.hs.configurate.be.core.service;

import com.arvatosystems.t9t.hs.configurate.model.EmbeddedIndexEntityConfig;
import com.arvatosystems.t9t.hs.configurate.model.EntityConfig;
import com.arvatosystems.t9t.hs.configurate.model.FieldConfig;
import de.jpaw.bonaparte.util.FreezeTools;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
