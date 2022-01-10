/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.schemaLoader.mavenPlugin.config;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.Parameter;

import com.arvatosystems.t9t.schemaLoader.mavenPlugin.helper.SqlDependencyHelper;

public class SqlArtifact {

    /** Group id used for filtering */
    @Parameter(required = true)
    protected String groupId;

    /** Artifact id used for filtering */
    @Parameter(required = true)
    protected String artifactId;

    public boolean isMatch(Artifact artifact) {
        boolean idMatches = artifact.getGroupId().equals(groupId) && artifact.getArtifactId().equals(artifactId);
        boolean classifierMatches = artifact.hasClassifier() && artifact.getClassifier().equals(getClassifier());
        return idMatches && classifierMatches;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getClassifier() {
        return SqlDependencyHelper.SQL_CLASSIFIER;
    }

    @Override
    public String toString() {
        return "SqlArtifact [groupId=" + groupId + ", artifactId=" + artifactId + ", classifier=" + getClassifier() + "]";
    }

}
