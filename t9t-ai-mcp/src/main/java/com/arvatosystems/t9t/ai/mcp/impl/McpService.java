package com.arvatosystems.t9t.ai.mcp.impl;

import com.arvatosystems.t9t.ai.T9tAiException;
import com.arvatosystems.t9t.ai.mcp.IMcpService;
import com.arvatosystems.t9t.ai.mcp.McpCapabilities;
import com.arvatosystems.t9t.ai.mcp.McpCapabilityTools;
import com.arvatosystems.t9t.ai.mcp.McpContentElement;
import com.arvatosystems.t9t.ai.mcp.McpInitializeResult;
import com.arvatosystems.t9t.ai.mcp.McpResult;
import com.arvatosystems.t9t.ai.mcp.McpResultPayload;
import com.arvatosystems.t9t.ai.mcp.McpServerInfo;
import com.arvatosystems.t9t.ai.mcp.McpUtils;
import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
import com.arvatosystems.t9t.ai.request.AiRunToolResponse;
import com.arvatosystems.t9t.ai.tools.AiToolMediaDataResult;
import com.arvatosystems.t9t.ai.tools.AiToolNoResult;
import com.arvatosystems.t9t.ai.tools.AiToolStringResult;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class McpService implements IMcpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpService.class);

    protected final ObjectMapper objectMapper = JacksonTools.createObjectMapper();

    @Nonnull
    @Override
    public McpInitializeResult getInitializeResult() {
        final McpServerInfo serverInfo = new McpServerInfo(McpUtils.SERVER_NAME, McpUtils.SERVER_VERSION);
        final McpCapabilities capabilities = new McpCapabilities();
        capabilities.setTools(new McpCapabilityTools()); // tools are supported
        final McpInitializeResult result = new McpInitializeResult();
        result.setProtocolVersion(McpUtils.PROTOCOL_VERSION);
        result.setCapabilities(capabilities);
        result.setServerInfo(serverInfo);
        return result;
    }

    @Nonnull
    @Override
    public McpResultPayload mapGetToolsResponse(@Nonnull final AiGetToolsResponse response) {
        final McpResultPayload mcpToolsResult = new McpResultPayload();
        mcpToolsResult.setTools(response.getTools());
        return mcpToolsResult;
    }

    @Nonnull
    @Override
    public McpResultPayload mapRunToolsResponse(@Nonnull final AiRunToolResponse response) {
        final BonaPortable result = response.getResponse();
        final McpResultPayload mcpToolsResult = new McpResultPayload();
        if (result instanceof AiToolStringResult sr) {
            final McpContentElement textContent = new McpContentElement();
            textContent.setType("text");
            textContent.setText(sr.getText());
            mcpToolsResult.setContent(List.of(textContent));
        } else if (result instanceof AiToolMediaDataResult mdr) {
            final McpContentElement mediaContent = new McpContentElement();
            // obtain the classification from media type
            final MediaData md = mdr.getMediaData();
            final MediaTypeDescriptor mtd = MediaTypeInfo.getFormatByType(md.getMediaType());

            mediaContent.setType(mtd.getFormatCategory().name().toLowerCase());  // text, audio, video, image
            if (md.getText() != null) {
                mediaContent.setText(md.getText());
            } else if (md.getRawData() != null) {
                // convert raw data to base64 string
                mediaContent.setMimeType(mtd.getMimeType());
                mediaContent.setData(Base64.getEncoder().encodeToString(md.getRawData().getBytes()));
            }
            mcpToolsResult.setContent(List.of(mediaContent));
        } else if (result instanceof AiToolNoResult) {
            return null;
        } else {
            // return as structured Result
            mcpToolsResult.setStructuredContent(result);
            // since it's very new and VS Code (1.102) and Eclipse do not yet understand it, also provide the classical format
            final McpContentElement textContent = new McpContentElement();
            textContent.setType("text");
            try {
                textContent.setText(objectMapper.writeValueAsString(result));
            } catch (JsonProcessingException e) {
                LOGGER.error("Jackson exception: ", e);
                textContent.setData("Jackson error");
                mcpToolsResult.setIsError(Boolean.TRUE);
            }
            mcpToolsResult.setContent(List.of(textContent));
        }
        return mcpToolsResult;
    }

    @Nonnull
    @Override
    public String out(@Nonnull final String id, @Nonnull final BonaPortable result) {
        final McpResult mcpResult = new McpResult();
        mcpResult.setJsonrpc(McpUtils.JSONRPC_VERSION);
        mcpResult.setId(id);
        mcpResult.setResult(result);
        try {
            return objectMapper.writeValueAsString(mcpResult);
        } catch (final JsonProcessingException e) {
            LOGGER.error("Error serializing result for id {}. Error: {}", id, e.getMessage(), e);
            throw new T9tException(T9tAiException.MCP_SERIALIZATION_ERROR, e.getMessage());
        }
    }

    @Nonnull
    @Override
    public String error(@Nonnull final String id, final int code, @Nonnull final String message) {
        final Map<String, Object> error = new HashMap<>(2);
        error.put(McpUtils.KEY_JSONRPC, McpUtils.JSONRPC_VERSION);
        if (T9tUtil.isNotBlank(id)) {
            error.put(McpUtils.KEY_ID, id);
        }
        error.put("error", Map.of("code", code, "message", message));
        try {
            return objectMapper.writeValueAsString(error);
        } catch (final JsonProcessingException e) {
            LOGGER.error("Error serializing error response for id {}. Error: {}", id, e.getMessage(), e);
            // fallback to do it "by hand"
            return "{\"jsonrpc\":\"" + McpUtils.JSONRPC_VERSION + "\", \"error\":{\"code\":" + code + ", \"message\":\"" + message + "\"}}";
        }
    }
}
