/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

public class Migration {

    /** List of db object types to use for drop before migration (in provided order) */
    @Parameter(alias = "pre-drops")
    private List<String> preDrops;

    /** List of db object types to use for creation after migration (in provided order) */
    @Parameter(alias = "post-creates")
    private List<String> postCreates;

    public List<String> getPreDrops() {
        return preDrops==null?Collections.emptyList():preDrops;
    }

    public List<String> getPostCreates() {
        return postCreates==null?Collections.emptyList():postCreates;
    }

    @Override
    public String toString() {
        return "Migration [preDrops=" + preDrops + ", postCreates=" + postCreates + "]";
    }



}
