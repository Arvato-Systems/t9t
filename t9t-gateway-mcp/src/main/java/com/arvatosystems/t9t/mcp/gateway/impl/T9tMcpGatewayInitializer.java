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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.AiPromptDTO;
import com.arvatosystems.t9t.ai.AiPromptParameter;
import com.arvatosystems.t9t.ai.mcp.AiToolSpecification;
import com.arvatosystems.t9t.ai.request.AiGetPromptRequest;
import com.arvatosystems.t9t.ai.request.AiGetPromptsResponse;
import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
import com.arvatosystems.t9t.ai.request.AiRunToolRequest;
import com.arvatosystems.t9t.base.IRemoteDefaultUrlRetriever;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.client.init.SystemConfigurationProvider;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.arvatosystems.t9t.jdp.Init;
import com.arvatosystems.t9t.mcp.gateway.IT9tMcpProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.jpaw.api.ConfigurationReader;
import de.jpaw.bonaparte.util.FreezeTools;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ConfigurationReaderFactory;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class T9tMcpGatewayInitializer implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tMcpGatewayInitializer.class);
    private static final ObjectMapper MAPPER = JacksonTools.createObjectMapper();
    private static final McpJsonMapper MCP_MAPPER = new JacksonMcpJsonMapper(MAPPER);

    private static final ConfigurationReader CONFIG_READER = ConfigurationReaderFactory.getConfigReaderForName("t9t.mcp", null);

    private final IT9tMcpProcessor mcpProcessor;

    public T9tMcpGatewayInitializer() {
        MessagingUtil.initializeBonaparteParsers();
        Init.initializeT9t();

        mcpProcessor = Jdp.getRequired(IT9tMcpProcessor.class);

        LOGGER.info("Initializing system configuration");
        Jdp.bindInstanceTo(new SystemConfigurationProvider(), IRemoteDefaultUrlRetriever.class);
    }

    public McpSyncServer initMcpServer(final McpStreamableServerTransportProvider transportProvider) {
        final String apiKey = CONFIG_READER.getProperty("t9t.mcp.apiKey");
        if (T9tUtil.isBlank(apiKey)) {
            LOGGER.error("Missing system property t9t.mcp.apiKey or corresponding environment variable T9T_MCP_APIKEY");
            throw new T9tException(T9tException.MISSING_CONFIGURATION, "t9t.mcp.apiKey");
        }
        final String authHeader = "API-Key " + apiKey;

        final McpSyncServer syncServer = McpServer.sync(transportProvider).serverInfo("t9t MCP gateway", "1.0.0")
            .capabilities(ServerCapabilities.builder()
                .resources(false, true) // Enable resource support
                .tools(true) // Enable tool support
                .prompts(true) // Enable prompt support
                .logging() // Enable logging support
                .completions() // Enable completions support
                .build())
            .build();

        registerTools(syncServer, authHeader);
        registerPrompts(syncServer, authHeader);

        return syncServer;
    }

    private void registerTools(@Nonnull final McpSyncServer syncServer, @Nonnull final String authHeader) {
        final AiGetToolsResponse aiGetToolsResponse = mcpProcessor.getTools(authHeader);

        LOGGER.debug("Retrieved {} tools: {}", aiGetToolsResponse.getTools().size(),
                aiGetToolsResponse.getTools().stream().map(AiToolSpecification::getName).toList());

        for (final AiToolSpecification tool : aiGetToolsResponse.getTools()) {
            try {
                final Tool mcpTool = new Tool.Builder().name(tool.getName()).description(tool.getDescription())
                    .inputSchema(MCP_MAPPER, MAPPER.writeValueAsString(tool.getInputSchema()))
                    .outputSchema(MCP_MAPPER, MAPPER.writeValueAsString(tool.getOutputSchema()))
                    .build();

                McpServerFeatures.SyncToolSpecification toolSpec = new McpServerFeatures.SyncToolSpecification(mcpTool, null,
                        (mcpAsyncServerExchange, callToolRequest) -> {

                            final String toolName = callToolRequest.name();
                            final Map<String, Object> params = callToolRequest.arguments();
                            LOGGER.debug("Received tools call request with toolName={}, arguments={}", toolName, params);

                            if (T9tUtil.isBlank(toolName)) {
                                LOGGER.error("Tool name is missing in the request body");
                                return new CallToolResult("Tool executed failed, missing name", true);
                            }

                            removeEmptyEntries(params);

                            final AiRunToolRequest runRq = new AiRunToolRequest();
                            runRq.setName(toolName);
                            runRq.setParameters(params);
                            runRq.setStructuredResultAsString(Boolean.TRUE);

                            return mcpProcessor.runTool(runRq, authHeader);
                        });

                syncServer.addTool(toolSpec);
            } catch (final JsonProcessingException e) {
                LOGGER.error("Error converting tool specification to MCP Tool, skipping: {}", tool.getName());
                LOGGER.error("Error details: ", e);
            }
        }
    }

    private void registerPrompts(@Nonnull final McpSyncServer syncServer, @Nonnull final String authHeader) {
        final AiGetPromptsResponse aiGetPromptsResponse = mcpProcessor.getPrompts(authHeader);
        LOGGER.debug("Retrieved {} prompts", aiGetPromptsResponse.getPrompts().size());

        for (final AiPromptDTO prompt : aiGetPromptsResponse.getPrompts()) {
            try {
                final List<PromptArgument> promptArguments = new ArrayList<>(prompt.getParameters() != null
                    && prompt.getParameters().getParameters() != null ? prompt.getParameters().getParameters().size() : 0);
                if (prompt.getParameters() != null && prompt.getParameters().getParameters() != null) {
                    for (Map.Entry<String, AiPromptParameter> entry : prompt.getParameters().getParameters().entrySet()) {
                        final PromptArgument promptArgument = new PromptArgument(entry.getKey(), entry.getValue().getDescription(),
                            entry.getValue().getIsRequired());
                        promptArguments.add(promptArgument);
                    }
                }
                final Prompt mcpPrompt = new Prompt(prompt.getPromptId(), prompt.getTitle(), prompt.getDescription(), promptArguments);
                McpServerFeatures.SyncPromptSpecification promptSpec = new McpServerFeatures.SyncPromptSpecification(mcpPrompt,
                    (mcpAsyncServerExchange, getPromptRequest) -> {
                        final String promptName = getPromptRequest.name();
                        LOGGER.debug("Received get prompt request with promptName={}, arguments={}", promptName, getPromptRequest.arguments());

                        if (T9tUtil.isBlank(promptName)) {
                            LOGGER.error("Prompt name is missing in the request body");
                            return new GetPromptResult(null, Collections.emptyList());
                        }

                        final Map<String, Object> arguments = new HashMap<>(FreezeTools.getInitialHashMapCapacity(getPromptRequest.arguments() != null
                            ? getPromptRequest.arguments().size() : 0));
                        if (getPromptRequest.arguments() != null) {
                            for (Map.Entry<String, Object> entry : getPromptRequest.arguments().entrySet()) {
                                if (entry.getValue() != null && entry.getValue() instanceof String strValue && !strValue.isBlank()) {
                                    arguments.put(entry.getKey(), strValue);
                                }
                            }
                        }
                        final AiGetPromptRequest promptRequest = new AiGetPromptRequest();
                        promptRequest.setName(promptName);
                        promptRequest.setArguments(arguments);

                        return mcpProcessor.getPrompt(promptRequest, authHeader);
                    });

                syncServer.addPrompt(promptSpec);
            } catch (Exception e) {
                LOGGER.error("Error registering MCP Prompt, skipping: {}", prompt.getPromptId());
                LOGGER.error("Error details: ", e);
            }
        }
    }

    private static void removeEmptyEntries(final Map<String, Object> params) {
        params.entrySet().removeIf(entry -> {
            Object value = entry.getValue();
            if (value == null) {
                return true;
            }
            if (value instanceof String && ((String) value).isBlank()) {
                return true;
            }
            if (value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
                return true;
            }
            if (value instanceof Iterable && !((Iterable<?>) value).iterator().hasNext()) {
                return true;
            }
            return false;
        });
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        // do nothing
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        // do nothing
    }
}
