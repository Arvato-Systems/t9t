/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.schemaLoader.config;

import static java.util.Collections.emptySet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class InstallConfiguration {

    /**
     * <p>
     * Set version to use for baseline after schema installation.
     * </p>
     *
     * <p>
     * Valid values are version strings (e.g. 3.2.5) and following special values:
     *
     * <ul>
     * </li>"latest"=latest available migration version</li>
     * <li>"latest-major"=starting version of latest available major version. (E.g. 3.2.7 &rarr; 3)</li>
     * <li>"latest-minor"=starting version of latest available major version. (E.g. 3.2.7 &rarr; 3.2)</li>
     * <li>NULL=do not perform any baseline</li>
     * </ul>
     * </p>
     */
    private String baselineVersion = "latest-minor";
    public static final String BASELINE_VERSION_LATEST = "latest";
    public static final String BASELINE_VERSION_LATEST_MAJOR = "latest-major";
    public static final String BASELINE_VERSION_LATEST_MINOR = "latest-minor";

    /** List of db object types to use during create phase (in provided order) */
    private final List<String> create = new LinkedList<>();

    /** List of db object types to execute during drop phase (in provided order) */
    private final List<String> drop = new LinkedList<>();

    /** Path of installation scripts by db object type in classpath (will be normally auto-detected from script location) */
    private final Map<String, Set<String>> sqlScriptPathsByType = new HashMap<>();

    public Set<String> getSqlScriptPathsByType(String type) {
        return sqlScriptPathsByType.getOrDefault(type, emptySet());
    }

    public Map<String, Set<String>> getSqlScriptPathsByType() {
        return sqlScriptPathsByType;
    }

    public String getBaselineVersion() {
        return baselineVersion;
    }

    public void setBaselineVersion(String baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

    public List<String> getCreate() {
        return create;
    }

    public List<String> getDrop() {
        return drop;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("baselineVersion",
                                                baselineVersion)
                                        .append("create",
                                                create)
                                        .append("drop",
                                                drop)
                                        .append("sqlScriptPathsByType",
                                                sqlScriptPathsByType)
                                        .toString();
    }

}
