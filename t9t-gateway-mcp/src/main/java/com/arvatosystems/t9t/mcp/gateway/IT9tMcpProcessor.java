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
package com.arvatosystems.t9t.mcp.gateway;

import com.arvatosystems.t9t.ai.request.AiGetPromptRequest;
import com.arvatosystems.t9t.ai.request.AiGetPromptsResponse;
import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
import com.arvatosystems.t9t.ai.request.AiRunToolRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import jakarta.annotation.Nonnull;

public interface IT9tMcpProcessor {

    AiGetToolsResponse getTools(@Nonnull String authorizationHeader);

    CallToolResult runTool(@Nonnull AiRunToolRequest request, @Nonnull String authorizationHeader);

    @Nonnull
    AiGetPromptsResponse getPrompts(@Nonnull String authorizationHeader);

    @Nonnull
    GetPromptResult getPrompt(@Nonnull AiGetPromptRequest request, @Nonnull String authorizationHeader);

}
