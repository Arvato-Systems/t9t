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

import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;

import edu.emory.mathcs.backport.java.util.Collections;

public class Installation {

    /** List of db object types to execute during drop phase (in provided order) */
    @Parameter
    private List<String> drops;

    /** List of db object types to use during create phase (in provided order) */
    @Parameter
    private List<String> creates;

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
     * </ul>
     * </p>
     */
    @Parameter(defaultValue = "latest-minor", alias = "baseline-version", required = true)
    private String baselineVersion;

    public List<String> getDrops() {
        return drops==null?Collections.emptyList():drops;
    }

    public List<String> getCreates() {
        return creates==null?Collections.emptyList():creates;
    }

    public String getBaselineVersion() {
        return baselineVersion==null?"latest-minor":baselineVersion;
    }

    @Override
    public String toString() {
        return "Installation [drops=" + drops + ", creates=" + creates + ", baselineVersion=" + baselineVersion + "]";
    }


}
