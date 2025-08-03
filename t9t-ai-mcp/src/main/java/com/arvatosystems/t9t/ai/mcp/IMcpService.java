package com.arvatosystems.t9t.ai.mcp;

import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
import com.arvatosystems.t9t.ai.request.AiRunToolResponse;
import de.jpaw.bonaparte.core.BonaPortable;
import jakarta.annotation.Nonnull;

public interface IMcpService {

    @Nonnull
    McpInitializeResult getInitializeResult();

    @Nonnull
    McpResultPayload mapGetToolsResponse(@Nonnull AiGetToolsResponse response);

    @Nonnull
    McpResultPayload mapRunToolsResponse(@Nonnull AiRunToolResponse response);

    @Nonnull
    String out(@Nonnull String id, @Nonnull BonaPortable result);

    @Nonnull
    String error(@Nonnull String id, int code, @Nonnull String message);
}
