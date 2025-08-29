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
    McpInitializeResult getInitializeResult(@Nullable String protocolVersion);

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
