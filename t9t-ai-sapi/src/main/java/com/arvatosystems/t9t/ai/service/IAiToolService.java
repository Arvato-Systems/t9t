/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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
package com.arvatosystems.t9t.ai.service;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import de.jpaw.bonaparte.core.BonaPortableClass;

import com.arvatosystems.t9t.ai.tools.AbstractAiTool;
import com.arvatosystems.t9t.ai.tools.AbstractAiToolResult;

public interface IAiToolService {

    /**
     * Determine tool identifier for a given tool and request class.
     *
     * @param tool the tool impl class for which to get the identifier
     * @param requestClass tool request class
     * @return the tool identifier
     */
    <R extends AbstractAiTool, U extends AbstractAiToolResult> String getToolIdentifier(@Nonnull IAiTool<R, U> tool,
    @Nonnull BonaPortableClass<R> requestClass);

    /**
     * Determine tool identifier from tool name and arguments.
     *
     * @param name the tool name
     * @param arguments the tool arguments as JSON string
     * @return the tool identifier
     */
    String getToolIdentifier(@Nonnull String name, @Nullable String arguments);
}
