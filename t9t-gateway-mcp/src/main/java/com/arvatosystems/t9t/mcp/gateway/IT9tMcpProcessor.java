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
