package com.arvatosystems.t9t.hs.configurate.be.core.model;

import java.util.List;

/**
 * Configuration class for a single entity.
 */
public class EntityConfig {

    private String className;
    private List<FieldConfig> fields;
    private List<EmbeddedIndexEntityConfig> embeddedIndexEntities;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<FieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<FieldConfig> fields) {
        this.fields = fields;
    }

    public List<EmbeddedIndexEntityConfig> getEmbeddedIndexEntities() {
        return embeddedIndexEntities;
    }

    public void setEmbeddedIndexEntities(List<EmbeddedIndexEntityConfig> embeddedIndexEntities) {
        this.embeddedIndexEntities = embeddedIndexEntities;
    }
}
