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
package com.arvatosystems.t9t.ai.tools.impl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.ai.service.IAiTool;
import com.arvatosystems.t9t.ai.service.IAiToolService;
import com.arvatosystems.t9t.ai.tools.AbstractAiTool;
import com.arvatosystems.t9t.ai.tools.AbstractAiToolResult;
import com.arvatosystems.t9t.base.T9tUtil;

@Fallback
@Singleton
public class AiToolService implements IAiToolService {

    @Override
    public <R extends AbstractAiTool, U extends AbstractAiToolResult> String getToolIdentifier(@Nonnull final IAiTool<R, U> tool,
        @Nonnull final BonaPortableClass<R> requestClass) {
        return T9tUtil.getSimpleName(requestClass.getPqon());
    }

    @Override
    public String getToolIdentifier(@Nonnull final String name, @Nullable final String arguments) {
        return name;
    }
}
