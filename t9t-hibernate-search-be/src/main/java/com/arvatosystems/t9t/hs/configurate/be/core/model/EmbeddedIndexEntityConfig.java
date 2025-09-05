package com.arvatosystems.t9t.hs.configurate.be.core.model;

import java.util.List;

/**
 * Configuration class for embedded fields.
 */
public class EmbeddedIndexEntityConfig {

    private String targetEntity;

    private List<EmbeddedIncludePathConfig> includePaths;

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public List<EmbeddedIncludePathConfig> getIncludePaths() {
        return includePaths;
    }

    public void setIncludePaths(List<EmbeddedIncludePathConfig> includePaths) {
        this.includePaths = includePaths;
    }
}
