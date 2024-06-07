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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.arvatosystems.t9t.ai.tools.AbstractAITool;
import com.arvatosystems.t9t.ai.tools.AbstractAIToolResult;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.core.BonaPortableClass;
import jakarta.annotation.Nonnull;

public final class AIToolRegistry {
    private static final Map<String, AIToolDescriptor<?, ?>> TOOLS = new ConcurrentHashMap<>();

    private AIToolRegistry() {
        // prevent instantiation
    }

    /** Registers a tool, currently needs to be fed with the request class. */
    public static <R extends AbstractAITool, U extends AbstractAIToolResult> void register(final IAITool<R, U> tool, BonaPortableClass<R> requestClass) {
        // final String modifiedName = requestClass.getPqon().replace(".", "-");  // path names are garbled by sime LLMs
        final String modifiedName = T9tUtil.getSimpleName(requestClass.getPqon());
        TOOLS.put(modifiedName, new AIToolDescriptor(modifiedName, tool, requestClass, requestClass.getReturns()));
    }

    public static AIToolDescriptor<?, ?> get(final String name) {
        return TOOLS.get(name);
    }

    public static int size() {
        return TOOLS.size();
    }

    public static void forEach(@Nonnull final Consumer<AIToolDescriptor> consumer) {
        for (final AIToolDescriptor<?, ?> tool : TOOLS.values()) {
            consumer.accept(tool);
        }
    }

    /** Executes a tool by name. */
    public static <R extends AbstractAITool, U extends AbstractAIToolResult> U executeTool(@Nonnull final RequestContext ctx,
      @Nonnull final String name, @Nonnull final R request) {
        final AIToolDescriptor<R, U> tool = (AIToolDescriptor<R, U>) TOOLS.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("Tool " + name + " not found");
        }
        return tool.toolInstance().performToolCall(ctx, request);
    }
}
