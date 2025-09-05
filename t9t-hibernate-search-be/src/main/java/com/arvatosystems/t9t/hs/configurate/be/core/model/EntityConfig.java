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
