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
package com.arvatosystems.t9t.ai.mcp;

import com.arvatosystems.t9t.ai.request.AiGetPromptResponse;
import com.arvatosystems.t9t.ai.request.AiGetPromptsResponse;
import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
import com.arvatosystems.t9t.ai.request.AiRunToolResponse;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import de.jpaw.bonaparte.core.BonaPortable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface IMcpService {

    @Nonnull
    McpInitializeResult getInitializeResult(@Nullable String protocolVersion, @Nonnull String serverName);

    @Nonnull
    McpResultPayload mapGetToolsResponse(@Nonnull AiGetToolsResponse response);

    @Nonnull
    McpResultPayload mapRunToolResponse(@Nonnull AiRunToolResponse response);

    @Nonnull
    McpPromptsResult mapGetPromptsResponse(@Nonnull AiGetPromptsResponse response);

    @Nonnull
    McpPromptResult mapGetPromptResponse(@Nonnull AiGetPromptResponse response);

    @Nonnull
    String out(@Nonnull String id, @Nonnull BonaPortable result);

    @Nonnull
    String error(@Nonnull String id, int code, @Nonnull String message);

    @Nonnull
    McpResult createMcpError(@Nonnull ServiceResponse serviceResponse, @Nullable String id);
}
