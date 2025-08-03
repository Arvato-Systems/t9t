package com.arvatosystems.t9t.hs.configurate.be.core.model;

import java.util.List;

/**
 * Configuration class for Hibernate Search entity mapping.
 */
public class EntitySearchConfiguration {

    private List<EntityConfig> entities;

    public List<EntityConfig> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityConfig> entities) {
        this.entities = entities;
    }
}
