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
package com.arvatosystems.t9t.ai.mcp.impl;

import com.arvatosystems.t9t.ai.AiPromptDTO;
import com.arvatosystems.t9t.ai.AiPromptParameter;
import com.arvatosystems.t9t.ai.mcp.AiPromptSpecification;
import com.arvatosystems.t9t.ai.mcp.IMcpService;
import com.arvatosystems.t9t.ai.mcp.McpCapabilities;
import com.arvatosystems.t9t.ai.mcp.McpCapabilityPrompts;
import com.arvatosystems.t9t.ai.mcp.McpCapabilityTools;
import com.arvatosystems.t9t.ai.mcp.McpContentElement;
import com.arvatosystems.t9t.ai.mcp.McpError;
import com.arvatosystems.t9t.ai.mcp.McpInitializeResult;
import com.arvatosystems.t9t.ai.mcp.McpPromptResult;
import com.arvatosystems.t9t.ai.mcp.McpPromptsResult;
import com.arvatosystems.t9t.ai.mcp.McpResult;
import com.arvatosystems.t9t.ai.mcp.McpResultPayload;
import com.arvatosystems.t9t.ai.mcp.McpServerInfo;
import com.arvatosystems.t9t.ai.mcp.McpUtils;
import com.arvatosystems.t9t.ai.mcp.PromptMessage;
import com.arvatosystems.t9t.ai.mcp.PromptParameter;
import com.arvatosystems.t9t.ai.request.AiGetPromptResponse;
import com.arvatosystems.t9t.ai.request.AiGetPromptsResponse;
import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
import com.arvatosystems.t9t.ai.request.AiRunToolResponse;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class McpService implements IMcpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpService.class);

    protected final ObjectMapper objectMapper = JacksonTools.createObjectMapper();

    @Nonnull
    @Override
    public McpInitializeResult getInitializeResult(final String protocolVersion) {
        final McpServerInfo serverInfo = new McpServerInfo(McpUtils.SERVER_NAME, McpUtils.SERVER_VERSION);
        final McpCapabilities capabilities = new McpCapabilities();
        capabilities.setTools(new McpCapabilityTools()); // tools are supported
        capabilities.setPrompts(new McpCapabilityPrompts());
        final McpInitializeResult result = new McpInitializeResult();
        result.setProtocolVersion(protocolVersion);
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
    public McpResultPayload mapRunToolResponse(AiRunToolResponse response) {
        List<McpContentElement> contentElements = null;
        if (response.getContents() != null) {
            contentElements = new ArrayList<>(response.getContents().size());
            for (MediaData content : response.getContents()) {
                contentElements.add(new McpContentElement(null, content.getMediaType().getToken(), content.getText(), content.getRawData().asString()));
            }
        }
        final McpResultPayload mcpToolsResult = new McpResultPayload(null, contentElements, response.getStructuredResponse(), response.getIsError());
        return mcpToolsResult;
    }

    @Nonnull
    @Override
    public McpPromptsResult mapGetPromptsResponse(@Nonnull final AiGetPromptsResponse response) {
        final McpPromptsResult result = new McpPromptsResult();
        final List<AiPromptSpecification> specList = new ArrayList<>(response.getPrompts().size());
        result.setPrompts(specList);
        if (response.getNextOffset() != null) {
            result.setNextCursor(String.valueOf(response.getNextOffset()));
        }
        for (final AiPromptDTO dto: response.getPrompts()) {
            final AiPromptSpecification spec = new AiPromptSpecification();
            specList.add(spec);
            spec.setName(dto.getPromptId());
            spec.setTitle(dto.getTitle());
            spec.setDescription(dto.getDescription());
            final List<PromptParameter> promptParams = new ArrayList<>(dto.getParameters().getParameters().size());
            spec.setArguments(promptParams);
            for (Map.Entry<String, AiPromptParameter> entry: dto.getParameters().getParameters().entrySet()) {
                final PromptParameter promptParam = new PromptParameter();
                promptParam.setName(entry.getKey());
                promptParam.setDescription(entry.getValue().getDescription());
                promptParam.setIsRequired(entry.getValue().getIsRequired());
                promptParams.add(promptParam);
            }
        }
        return result;
    }

    @Nonnull
    @Override
    public McpPromptResult mapGetPromptResponse(@Nonnull final AiGetPromptResponse response) {
        final McpPromptResult result = new McpPromptResult();
        result.setDescription(response.getDescription());
        final PromptMessage message = new PromptMessage();
        message.setRole(McpUtils.ROLE_USER); // TODO:
        final McpContentElement content = new McpContentElement();
        content.setType(McpUtils.CONTENT_TYPE_TEXT);
        content.setText(response.getPrompt());
        message.setContent(content);
        result.setMessages(List.of(message));
        return result;
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
            return rawErrorResponse(McpUtils.MCP_PARSE_ERROR, e.getMessage());
        }
    }

    @Nonnull
    @Override
    public String error(@Nonnull final String id, final int code, @Nonnull final String message) {
        final McpError mcpError = new McpError();
        mcpError.setCode(code);
        mcpError.setMessage(message);
        final McpResult mcpResult = new McpResult();
        mcpResult.setJsonrpc(McpUtils.JSONRPC_VERSION);
        mcpResult.setId(id);
        mcpResult.setError(mcpError);
        try {
            return objectMapper.writeValueAsString(mcpResult);
        } catch (final JsonProcessingException e) {
            LOGGER.error("Error serializing error response for id {}. Error: {}", id, e.getMessage(), e);
            return rawErrorResponse(code, message);
        }
    }

    @Nonnull
    @Override
    public McpResult createMcpError(@Nonnull final ServiceResponse serviceResponse, @Nullable final String id) {
        final McpResult errorResult = new McpResult();
        errorResult.setJsonrpc(McpUtils.JSONRPC_VERSION);
        errorResult.setId(id);
        final McpError mcpError = new McpError();
        errorResult.setError(mcpError);
        mcpError.setMessage(serviceResponse.getErrorMessage());
        switch (serviceResponse.getReturnCode() / ApplicationException.CLASSIFICATION_FACTOR) {
        case ApplicationException.CL_PARAMETER_ERROR:
            mcpError.setCode(McpUtils.MCP_INVALID_PARAMS);
            break;
        case ApplicationException.CL_VALIDATION_ERROR:
            mcpError.setCode(McpUtils.MCP_INVALID_REQUEST);
            break;
        case ApplicationException.CL_PARSER_ERROR:
            mcpError.setCode(McpUtils.MCP_PARSE_ERROR);
            break;
        default:
            mcpError.setCode(McpUtils.MCP_INTERNAL_ERROR);
        }
        return errorResult;
    }

    private String rawErrorResponse(final int code, final String message) {
        // fallback to do it "by hand"
        LOGGER.warn("Returning raw error response with code {} and message: {}", code, message);
        return "{\"jsonrpc\":\"" + McpUtils.JSONRPC_VERSION + "\", \"error\":{\"code\":" + code + ", \"message\":\"" + message + "\"}}";
    }
}
