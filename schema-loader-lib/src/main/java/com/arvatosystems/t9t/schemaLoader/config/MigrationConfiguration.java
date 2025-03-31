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
package com.arvatosystems.t9t.schemaLoader.config;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MigrationConfiguration {

    /** List of db object types to use for drop before migration (in provided order) */
    private final List<String> preDrop = new LinkedList<>();

    /** List of db object types to use for creation after migration (in provided order) */
    private final List<String> postCreate = new LinkedList<>();

    /** Path of migration scripts in classpath (will be normally auto-detected from script location) */
    private final Set<String> migrationScriptPaths = new HashSet<>();

    public Set<String> getMigrationScriptPaths() {
        return migrationScriptPaths;
    }

    public List<String> getPreDrop() {
        return preDrop;
    }

    public List<String> getPostCreate() {
        return postCreate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("preDrop",
                                                preDrop)
                                        .append("postCreate",
                                                postCreate)
                                        .append("migrationScriptPaths",
                                                migrationScriptPaths)
                                        .toString();
    }

}
