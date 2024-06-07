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
package com.arvatosystems.t9t.ai.service;

import com.arvatosystems.t9t.ai.tools.AbstractAITool;
import com.arvatosystems.t9t.ai.tools.AbstractAIToolResult;

import de.jpaw.bonaparte.core.BonaPortableClass;
import jakarta.annotation.Nonnull;

/** Descriptor for an AI tool. */
public record AIToolDescriptor<R extends  AbstractAITool, U extends AbstractAIToolResult>(
    @Nonnull String name,
    @Nonnull IAITool<R, U> toolInstance,
    BonaPortableClass<R> requestClass,
    BonaPortableClass<U> resultClass) {
}
