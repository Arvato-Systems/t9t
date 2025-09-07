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

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.AiPromptDTO;
import com.arvatosystems.t9t.ai.AiPromptParameter;
import com.arvatosystems.t9t.ai.T9tAiMcpConstants;
import com.arvatosystems.t9t.ai.mcp.AiPromptSpecification;
import com.arvatosystems.t9t.ai.mcp.IMcpService;
import com.arvatosystems.t9t.ai.mcp.McpCapabilities;
import com.arvatosystems.t9t.ai.mcp.McpCapabilityPrompts;
import com.arvatosystems.t9t.ai.mcp.McpCapabilityTools;
import com.arvatosystems.t9t.ai.mcp.McpCompleteEntry;
import com.arvatosystems.t9t.ai.mcp.McpCompleteResult;
import com.arvatosystems.t9t.ai.mcp.McpContentElement;
import com.arvatosystems.t9t.ai.mcp.McpError;
import com.arvatosystems.t9t.ai.mcp.McpInitializeResult;
import com.arvatosystems.t9t.ai.mcp.McpPromptResult;
import com.arvatosystems.t9t.ai.mcp.McpPromptsResult;
import com.arvatosystems.t9t.ai.mcp.McpResult;
import com.arvatosystems.t9t.ai.mcp.McpResultPayload;
import com.arvatosystems.t9t.ai.mcp.McpServerInfo;
import com.arvatosystems.t9t.ai.mcp.PromptMessage;
import com.arvatosystems.t9t.ai.mcp.PromptParameter;
import com.arvatosystems.t9t.ai.request.AiGetPromptResponse;
import com.arvatosystems.t9t.ai.request.AiGetPromptsResponse;
import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
import com.arvatosystems.t9t.ai.request.AiRunToolResponse;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import jakarta.annotation.Nonnull;

@Singleton
public class McpService implements IMcpService {
    private static final Logger LOGGER = LoggerFactory.getLogger(McpService.class);

    protected final ObjectMapper objectMapper = JacksonTools.createObjectMapper();

    @Nonnull
    @Override
    public McpInitializeResult getInitializeResult(final String protocolVersion, final String serverName) {
        final McpServerInfo serverInfo = new McpServerInfo(serverName, T9tAiMcpConstants.SERVER_VERSION);
        final McpCapabilities capabilities = new McpCapabilities();
        capabilities.setTools(new McpCapabilityTools()); // tools are supported
        capabilities.setPrompts(new McpCapabilityPrompts());
        final McpInitializeResult result = new McpInitializeResult();
        result.setProtocolVersion(T9tUtil.nvl(protocolVersion, T9tAiMcpConstants.FALLBACK_MCP_PROTOCOL_VERSION));
        result.setCapabilities(capabilities);
        result.setServerInfo(serverInfo);
        return result;
    }

    @Nonnull
    @Override
    public McpResultPayload mapGetToolsResponse(final AiGetToolsResponse response) {
        final McpResultPayload mcpToolsResult = new McpResultPayload();
        mcpToolsResult.setTools(response.getTools());
        return mcpToolsResult;
    }

    @Nonnull
    @Override
    public McpResultPayload mapRunToolResponse(final AiRunToolResponse response) {
        final boolean haveRegularContent = response.getContents() != null && !response.getContents().isEmpty();
        final List<McpContentElement> contentElements = new ArrayList<>(haveRegularContent ? 1 + response.getContents().size() : 1);
        if (haveRegularContent) {
            for (final MediaData md : response.getContents()) {
                final McpContentElement mediaContent = new McpContentElement();
                if (md.getText() != null) {
                    mediaContent.setType(T9tAiMcpConstants.CONTENT_TYPE_TEXT);
                    mediaContent.setText(md.getText());
                } else if (md.getRawData() != null) {
                    // obtain the classification from media type
                    final MediaTypeDescriptor mtd = MediaTypeInfo.getFormatByType(md.getMediaType());
                    final String type = mtd.getFormatCategory().name().toLowerCase();  // text, audio, video, image
                    mediaContent.setType(type);
                    mediaContent.setMimeType(mtd.getMimeType());
                    // convert raw data to base64 string
                    mediaContent.setData(Base64.getEncoder().encodeToString(md.getRawData().getBytes()));
                }
                contentElements.add(mediaContent);
            }
        }
        final McpResultPayload mcpToolsResult = new McpResultPayload();
        mcpToolsResult.setContent(contentElements);
        mcpToolsResult.setIsError(response.getIsError());

        // possible convert structured response to JSON string
        if (response.getStructuredResponseAsString() != null) {
            mcpToolsResult.setStructuredContent(response.getStructuredResponseAsString());
            // also duplicate it for older clients
            final McpContentElement mediaContent = new McpContentElement();
            mediaContent.setType(T9tAiMcpConstants.CONTENT_TYPE_TEXT);
            mediaContent.setText(response.getStructuredResponseAsString());
            contentElements.add(mediaContent);
        } else if (response.getStructuredResponse() != null) {
            mcpToolsResult.setStructuredContent(response.getStructuredResponse());
            // also duplicate it for older clients
            final McpContentElement mediaContent = new McpContentElement();
            mediaContent.setType(T9tAiMcpConstants.CONTENT_TYPE_TEXT);
            try {
                mediaContent.setText(objectMapper.writeValueAsString(response.getStructuredResponse()));
                contentElements.add(mediaContent);
            } catch (final JsonProcessingException e) {
                LOGGER.error("Error serializing structured response: {}", e.getMessage(), e);
                mcpToolsResult.setIsError(true);
            }
        }
        return mcpToolsResult;
    }

    @Nonnull
    @Override
    public McpPromptsResult mapGetPromptsResponse(final AiGetPromptsResponse response) {
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
    public McpPromptResult mapGetPromptResponse(final AiGetPromptResponse response) {
        final McpPromptResult result = new McpPromptResult();
        result.setDescription(response.getDescription());
        final PromptMessage message = new PromptMessage();
        message.setRole(T9tAiMcpConstants.ROLE_USER); // TODO:
        final McpContentElement content = new McpContentElement();
        content.setType(T9tAiMcpConstants.CONTENT_TYPE_TEXT);
        content.setText(response.getPrompt());
        message.setContent(content);
        result.setMessages(List.of(message));
        return result;
    }

    @Nonnull
    @Override
    public String out(final Object id, final BonaPortable result) {
        final McpResult mcpResult = new McpResult();
        mcpResult.setJsonrpc(T9tAiMcpConstants.JSONRPC_VERSION);
        mcpResult.setId(id);
        mcpResult.setResult(result);
        try {
            return objectMapper.writeValueAsString(mcpResult);
        } catch (final JsonProcessingException e) {
            LOGGER.error("Error serializing result for id {}. Error: {}", id, e.getMessage(), e);
            return rawErrorResponse(T9tAiMcpConstants.MCP_PARSE_ERROR, e.getMessage());
        }
    }

    @Nonnull
    @Override
    public String error(final Object id, final int code, final String message) {
        final McpError mcpError = new McpError();
        mcpError.setCode(code);
        mcpError.setMessage(message);
        final McpResult mcpResult = new McpResult();
        mcpResult.setJsonrpc(T9tAiMcpConstants.JSONRPC_VERSION);
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
    public McpResult createMcpError(final ServiceResponse serviceResponse, final Object id) {
        final McpResult errorResult = new McpResult();
        errorResult.setJsonrpc(T9tAiMcpConstants.JSONRPC_VERSION);
        errorResult.setId(id);
        final McpError mcpError = new McpError();
        errorResult.setError(mcpError);
        mcpError.setMessage(serviceResponse.getErrorMessage());
        switch (serviceResponse.getReturnCode() / ApplicationException.CLASSIFICATION_FACTOR) {
        case ApplicationException.CL_PARAMETER_ERROR:
            mcpError.setCode(T9tAiMcpConstants.MCP_INVALID_PARAMS);
            break;
        case ApplicationException.CL_VALIDATION_ERROR:
            mcpError.setCode(T9tAiMcpConstants.MCP_INVALID_REQUEST);
            break;
        case ApplicationException.CL_PARSER_ERROR:
            mcpError.setCode(T9tAiMcpConstants.MCP_PARSE_ERROR);
            break;
        default:
            mcpError.setCode(T9tAiMcpConstants.MCP_INTERNAL_ERROR);
        }
        return errorResult;
    }

    private String rawErrorResponse(final int code, final String message) {
        // fallback to do it "by hand"
        LOGGER.warn("Returning raw error response with code {} and message: {}", code, message);
        return "{\"jsonrpc\":\"" + T9tAiMcpConstants.JSONRPC_VERSION + "\", \"error\":{\"code\":" + code + ", \"message\":\"" + message + "\"}}";
    }

    @Override
    public Map<String, Object> convertArgumentsToMap(final JsonNode argNode) {
        return  objectMapper.convertValue(argNode, new TypeReference<Map<String, Object>>() { });
    }

    @Override
    public McpCompleteResult createDummyCompletionsCompleteResult() {
        final McpCompleteEntry entry = new McpCompleteEntry();
        entry.setValues(Collections.emptyList());
        entry.setHasMore(Boolean.FALSE);
        return new McpCompleteResult(entry);
    }
}
