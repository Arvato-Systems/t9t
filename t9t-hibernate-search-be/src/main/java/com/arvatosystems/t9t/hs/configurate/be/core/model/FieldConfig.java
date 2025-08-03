package com.arvatosystems.t9t.hs.configurate.be.core.model;

/**
 * Configuration class for entity field mapping.
 */
public class FieldConfig {

    private String name;
    private String type; // "fullTextField", "keywordField", etc.
    private String analyzer;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }
}
