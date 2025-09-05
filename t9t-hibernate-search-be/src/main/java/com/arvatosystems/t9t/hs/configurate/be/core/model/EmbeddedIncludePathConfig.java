package com.arvatosystems.t9t.hs.configurate.be.core.model;

/**
 * Defines a single includePath inside an embedded entity indexing configuration.
 * name:    relative field name inside the embedded entity
 * type:    keywordfield | fulltextfield | genericfield
 * analyzer: optional analyzer name (only relevant for fulltextfield)
 */
public class EmbeddedIncludePathConfig {
    private String name;         // required
    private String type;         // required
    private String analyzer;     // optional (only used when type == fulltextfield)

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

