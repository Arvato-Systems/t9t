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
package com.arvatosystems.t9t.mcp.gateway.impl;

import com.arvatosystems.t9t.ai.T9tAiMcpConstants;
import com.arvatosystems.t9t.ai.T9tAiException;
import com.arvatosystems.t9t.ai.request.AiGetPromptRequest;
import com.arvatosystems.t9t.ai.request.AiGetPromptResponse;
import com.arvatosystems.t9t.ai.request.AiGetPromptsRequest;
import com.arvatosystems.t9t.ai.request.AiGetPromptsResponse;
import com.arvatosystems.t9t.ai.request.AiGetToolsRequest;
import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
import com.arvatosystems.t9t.ai.request.AiRunToolRequest;
import com.arvatosystems.t9t.ai.request.AiRunToolResponse;
import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.RandomNumberGenerators;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.api.TransactionOriginType;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.arvatosystems.t9t.mcp.gateway.IT9tMcpProcessor;
import com.arvatosystems.t9t.rest.services.IGatewayStringSanitizerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;
import io.modelcontextprotocol.spec.McpSchema.AudioContent;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.ImageContent;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import jakarta.annotation.Nonnull;
import jakarta.ws.rs.core.Response.Status;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class T9tMcpProcessor implements IT9tMcpProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(T9tMcpProcessor.class);
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private static final ObjectMapper MAPPER = JacksonTools.createObjectMapper();

    private final IRemoteConnection connection = Jdp.getRequired(IRemoteConnection.class);
    private final IGatewayStringSanitizerFactory gatewayStringSanitizerFactory = Jdp.getRequired(IGatewayStringSanitizerFactory.class);
    private final DataConverter<String, AlphanumericElementaryDataItem> stringSanitizer = gatewayStringSanitizerFactory.createStringSanitizerForGateway();

    @Override
    public AiGetToolsResponse getTools(final String authorizationHeader) {
        final AiGetToolsRequest toolsRequest = new AiGetToolsRequest();

        final ServiceResponse response = performAsyncBackendRequest(toolsRequest, T9tAiMcpConstants.METHOD_TOOLS_LIST, authorizationHeader);
        if (response.getReturnCode() > 0) {
            throw new T9tException(T9tAiException.TOOLS_NOT_AVAILABLE, response.getErrorMessage());
        }

        if (response instanceof AiGetToolsResponse r) {
            return r;
        } else {
            LOGGER.error("Unexpected response type: {}", response.getClass().getName());
            throw new T9tException(T9tAiException.TOOLS_NOT_AVAILABLE, response.getErrorMessage());
        }
    }

    @Override
    public CallToolResult runTool(final AiRunToolRequest request, final String authorizationHeader) {
        final ServiceResponse response = performAsyncBackendRequest(request, T9tAiMcpConstants.METHOD_TOOLS_CALL, authorizationHeader);
        if (response.getReturnCode() > 0) {
            return new CallToolResult.Builder().addTextContent(response.getErrorDetails()).isError(true).build();
        }

        if (response instanceof AiRunToolResponse runToolResponse) {
            return convert(runToolResponse);
        } else {
            LOGGER.error("Unexpected response type: {}", response.getClass().getName());
            return new CallToolResult.Builder().addTextContent(response.getErrorDetails()).isError(true).build();
        }
    }

    @Override
    @Nonnull
    public AiGetPromptsResponse getPrompts(@Nonnull final String authorizationHeader) {
        final AiGetPromptsRequest promptsRequest = new AiGetPromptsRequest();
        promptsRequest.setOffset(-1); // no pagination, get all prompts

        final ServiceResponse response = performAsyncBackendRequest(promptsRequest, T9tAiMcpConstants.METHOD_PROMPTS_LIST, authorizationHeader);
        if (response.getReturnCode() > 0) {
            throw new T9tException(T9tAiException.PROMPTS_NOT_AVAILABLE, response.getErrorMessage());
        }

        if (response instanceof AiGetPromptsResponse r) {
            return r;
        } else {
            LOGGER.error("Unexpected response type for listing prompts: {}", response.getClass().getName());
            throw new T9tException(T9tAiException.PROMPTS_NOT_AVAILABLE, response.getErrorMessage());
        }
    }

    @Override
    @Nonnull
    public GetPromptResult getPrompt(@Nonnull final AiGetPromptRequest request, @Nonnull final String authorizationHeader) {
        final ServiceResponse response = performAsyncBackendRequest(request, T9tAiMcpConstants.METHOD_PROMPTS_GET, authorizationHeader);
        if (response.getReturnCode() > 0) {
            return new GetPromptResult(null, Collections.emptyList());
        }

        if (response instanceof AiGetPromptResponse getPromptResponse) {
            final TextContent textContent = new TextContent(getPromptResponse.getPrompt());
            final PromptMessage promptMessage = new PromptMessage(Role.USER, textContent);
            return new GetPromptResult(getPromptResponse.getDescription(), Collections.singletonList(promptMessage));
        } else {
            LOGGER.error("Unexpected response type for getting prompt: {}", response.getClass().getName());
            return new GetPromptResult(null, Collections.emptyList());
        }
    }

    private CallToolResult convert(final AiRunToolResponse src) {
        CallToolResult.Builder b = new CallToolResult.Builder();

        if (src.getStructuredResponse() != null) {
            try {
                b.structuredContent(MAPPER.writeValueAsString(src.getStructuredResponse()));
            } catch (JsonProcessingException e) {
                LOGGER.error("Error serializing structured response: {}", e.getMessage(), e);
                b.addTextContent("Error serializing structured response: " + e.getMessage());
                b.isError(true);
                return b.build();
            }
        } else if (src.getStructuredResponseAsString() != null) {
            b.structuredContent(src.getStructuredResponseAsString());
        }

        if (src.getContents() != null && !src.getContents().isEmpty()) {
            for (MediaData mediaData : src.getContents()) {
                final MediaType mediaType = MediaType.factory(mediaData.getMediaType().getToken());
                final MediaTypeDescriptor mediaTypeDesc = MediaTypeInfo.getFormatByType(mediaData.getMediaType());
                final String mimeType = mediaTypeDesc != null ? mediaTypeDesc.getMimeType() : mediaData.getMediaType().getToken();
                switch (mediaType) {
                case MediaType.TEXT:
                    b.addTextContent(mediaData.getText());
                    break;
                case MediaType.JPG:
                case MediaType.SVG:
                case MediaType.GIF:
                case MediaType.PNG:
                case MediaType.TIFF:
                case MediaType.WEBP:
                    b.addContent(new ImageContent(null, mediaData.getRawData().asBase64(), mimeType));
                    break;
                case MediaType.MP3:
                case MediaType.MP4:
                case MediaType.WAV:
                    b.addContent(new AudioContent(null, mediaData.getRawData().asBase64(), mimeType));
                    break;
                default:
                    LOGGER.warn("Unknown content type: {}", mediaData.getMediaType());
                }
            }
        }

        b.isError(src.getReturnCode() > 0 || Boolean.TRUE.equals(src.getIsError()));

        return b.build();
    }

    private ServiceResponse performAsyncBackendRequest(final RequestParameters requestParameters, final String infoMsg, final String authorizationHeader) {
        LOGGER.debug("Processing request: {}", requestParameters);
        try {
            requestParameters.validate(); // validate the request before we launch a worker thread!
        } catch (final ApplicationException e) {
            LOGGER.error("Exception during request validation: {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
            return new ServiceResponse(Status.BAD_REQUEST.getStatusCode(), null, null, requestParameters.getMessageId(), "Exception during request validation",
                e.getMessage());
        }
        final int invocationNo = COUNTER.incrementAndGet();
        // assign a message ID unless there is one already provided
        if (requestParameters.getMessageId() == null) {
            requestParameters.setMessageId(RandomNumberGenerators.randomFastUUID());
        }
        if (stringSanitizer != null) {
            try {
                requestParameters.treeWalkString(stringSanitizer, true);
            } catch (final ApplicationException e) {
                LOGGER.error("Exception during stringSanitizer: {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                return new ServiceResponse(Status.BAD_REQUEST.getStatusCode(), null, null, requestParameters.getMessageId(),
                    "Exception during String sanitizing", e.getMessage());
            }
        }
        LOGGER.debug("Starting {}: {} with assigned messageId {}", invocationNo, infoMsg, requestParameters.getMessageId());
        requestParameters.setWhenSent(System.currentTimeMillis()); // assumes all server clocks are sufficiently synchronized
        requestParameters.setTransactionOriginType(TransactionOriginType.GATEWAY_EXTERNAL);

        final CompletableFuture<ServiceResponse> readResponse = connection.executeAsync(authorizationHeader, requestParameters);

        try {
            final ServiceResponse sr = readResponse.get();

            LOGGER.debug("Response obtained {}: {}", invocationNo, infoMsg);

            return sr;
        } catch (final InterruptedException | ExecutionException e) {
            LOGGER.error("Exception during backend call: {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
            return new ServiceResponse(Status.BAD_REQUEST.getStatusCode(), null, null, requestParameters.getMessageId(), "Exception during backend call", e.getMessage());
        }
    }
}
