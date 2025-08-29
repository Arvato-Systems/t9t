package com.arvatosystems.t9t.mcp.vertx.impl;

import com.arvatosystems.t9t.ai.T9tAiException;
import com.arvatosystems.t9t.ai.mcp.IMcpService;
import com.arvatosystems.t9t.ai.mcp.McpPromptResult;
import com.arvatosystems.t9t.ai.mcp.McpPromptsResult;
import com.arvatosystems.t9t.ai.mcp.McpResult;
import com.arvatosystems.t9t.ai.mcp.McpResultPayload;
import com.arvatosystems.t9t.ai.mcp.McpUtils;
import com.arvatosystems.t9t.ai.request.AiGetPromptRequest;
import com.arvatosystems.t9t.ai.request.AiGetPromptResponse;
import com.arvatosystems.t9t.ai.request.AiGetPromptsRequest;
import com.arvatosystems.t9t.ai.request.AiGetPromptsResponse;
import com.arvatosystems.t9t.ai.request.AiGetToolsRequest;
import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
import com.arvatosystems.t9t.ai.request.AiRunToolRequest;
import com.arvatosystems.t9t.ai.request.AiRunToolResponse;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationInfo;
import com.arvatosystems.t9t.base.services.T9tInternalConstants;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.arvatosystems.t9t.server.services.IRequestProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestBody;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public class McpEndpointHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(McpEndpointHandler.class);

    private static final String JSONRPC_VERSION = "2.0";

    // This interface is used to define the type of handlers in the dispatcher map
    @FunctionalInterface
    private interface McpHandler {
        BonaPortable handle(@Nonnull JsonObject obj, @Nonnull AuthenticationInfo auth, @Nullable String protocolVersion);
    }
    private final Map<String, McpHandler> dispatcher = new ConcurrentHashMap<>();

    private final IRequestProcessor requestProcessor = Jdp.getRequired(IRequestProcessor.class);
    private final ObjectMapper objectMapper = JacksonTools.createObjectMapper();
    private final IMcpService mcpService = Jdp.getRequired(IMcpService.class);
    private final Vertx vertx;

    public McpEndpointHandler(final Vertx vertx) {
        this.vertx = vertx;
        dispatcher.put("initialize", this::initialize);
        dispatcher.put("tools/list", this::toolsList);
        dispatcher.put("tools/call", this::toolsCall);
        dispatcher.put("prompts/list", this::promptsList);
        dispatcher.put("prompts/get", this::promptsGet);
    }


    /** Error output uses vertx I/O to avoid nested exceptions.
     *
     * @param id the request ID, or null if not available
     * @param code the error code
     * @param message the error message
     * @return a JSON string representing the error response
     */
    private String error(final Object id, final int code, final String message) {
        final JsonObject error = new JsonObject()
            .put("code", code)
            .put("message", message);

        return new JsonObject()
            .put("jsonrpc", JSONRPC_VERSION)
            .put("id", id)
            .put("error", error)
            .encode();
    }

    private String out(final JsonObject body, final BonaPortable result) {
        // construct the full response object, including the header
        final Object id = body.getValue("id");
        final McpResult mcpResult = new McpResult();
        mcpResult.setJsonrpc(JSONRPC_VERSION);
        mcpResult.setId(id);
        mcpResult.setResult(result);
        try {
            return objectMapper.writeValueAsString(mcpResult);
        } catch (final JsonProcessingException e) {
            LOGGER.error("Error serializing result: {}", e.getMessage(), e);
            return error(id, T9tAiException.MCP_SERIALIZATION_ERROR, e.getMessage());
        }
    }

    private BonaPortable initialize(final JsonObject request, final AuthenticationInfo authInfo, final String protocolVersionOfHeader) {
        // get the parameters of the client
        final String clientInfo = request.getString("clientInfo");
        final String protocolVersionOfClient = request.getString("protocolVersion");
        LOGGER.debug("Initialize request from client: {}, protocol version: {}", clientInfo, protocolVersionOfClient);
        // construct the response
        return mcpService.getInitializeResult(protocolVersionOfClient);
    }

    /**
     * Dispatches the request for the supported methods.
     *
     * @param body the request body
     * @param authInfo the authentication information
     * @return the response, serialized as a string (logically a JSON object)
     */
    public String handleRequest(final RequestBody body, final AuthenticationInfo authInfo, final String protocolVersion) {
        final JsonObject request = body.asJsonObject();
        final Object id = request.getValue(McpUtils.KEY_ID);
        final String method = request.getString(McpUtils.KEY_METHOD);
        // check for notification (no response expected)
        if (id == null) {
            LOGGER.debug("received notification: {} (ignored)", method);
            return null;
        }
        // check if there is a valid handler
        final McpHandler handler = dispatcher.get(method);
        if (handler == null) {
            return error(id, McpUtils.MCP_METHOD_NOT_FOUND, "Method not found: " + method);
        }
        final JsonObject params = request.getJsonObject(McpUtils.KEY_PARAMS);
        final BonaPortable response = handler.handle(params, authInfo, protocolVersion);
        return response == null ? null : out(request, response);
    }

    public McpResultPayload toolsList(final JsonObject request, final AuthenticationInfo authInfo, final String protocolVersion) {
        // obtain current tools list for the given user
        final AiGetToolsResponse aiGetToolsResponse = processRequest(new AiGetToolsRequest(), authInfo, AiGetToolsResponse.class);
        // transfer list of tools
        LOGGER.debug("retrieved {} tools for user {}", aiGetToolsResponse.getTools().size(), authInfo.getJwtInfo().getUserId());
        return mcpService.mapGetToolsResponse(aiGetToolsResponse);
    }

    public McpResultPayload toolsCall(final JsonObject request, final AuthenticationInfo authInfo, final String protocolVersion) {
        // execute specified tool for the given user
        final String toolName = request.getString(McpUtils.KEY_NAME);
        final JsonObject arguments = request.getJsonObject(McpUtils.KEY_ARGUMENTS);
        final AiRunToolRequest runRq = new AiRunToolRequest();
        runRq.setName(toolName);
        if (arguments != null) {
            // convert the arguments back to a String (TODO: would be nicer to convert directly to BonaPortable...)
            runRq.setArguments(arguments.encode());
        }
        final AiRunToolResponse aiRunToolResponse = processRequest(runRq, authInfo, AiRunToolResponse.class);
        // transfer result object
        LOGGER.debug("tool call {} returned result for user {}", toolName, authInfo.getJwtInfo().getUserId());
        return mcpService.mapRunToolResponse(aiRunToolResponse);
    }

    protected <T extends ServiceResponse> T processRequest(final RequestParameters rq, final AuthenticationInfo authInfo, final Class<T> responseClass) {
        MDC.put(T9tInternalConstants.MDC_REQUEST_PQON, rq.ret$PQON());
        final ServiceResponse resp = requestProcessor.execute(null, rq, authInfo.getJwtInfo(), authInfo.getEncodedJwt(), false, null);
        if (responseClass.isInstance(resp)) {
            // return the response as the expected type
            return responseClass.cast(resp);
        } else {
            // return an error response
            LOGGER.error("Unexpected response type: expected {}, got {}", responseClass.getName(), resp.getClass().getName());
            throw new T9tException(resp.getReturnCode(), resp.getErrorMessage());
        }
    }

    public McpPromptsResult promptsList(final JsonObject request, final AuthenticationInfo authInfo, final String protocolVersion) {
        // obtain current prompts list for the given user
        final AiGetPromptsResponse aiGetPromptsResponse = processRequest(new AiGetPromptsRequest(), authInfo, AiGetPromptsResponse.class);
        // transfer list of prompts
        LOGGER.debug("retrieved {} prompts for user {}", aiGetPromptsResponse.getPrompts().size(), authInfo.getJwtInfo().getUserId());
        return mcpService.mapGetPromptsResponse(aiGetPromptsResponse);
    }

    public McpPromptResult promptsGet(final JsonObject request, final AuthenticationInfo authInfo, final String protocolVersion) {
        // obtain specific prompt
        final AiGetPromptRequest rq = new AiGetPromptRequest();

        final JsonObject params = request.getJsonObject(McpUtils.KEY_PARAMS);
        // we need that data
        if (params == null) {
            LOGGER.error("Received prompts get request without parameters");
            throw new T9tException(McpUtils.MCP_INVALID_PARAMS, "Missing parameters for prompts get request");
        }
        final String promptName = params.getString(McpUtils.KEY_NAME);
        final JsonObject arguments = params.getJsonObject(McpUtils.KEY_PARAMS);
        rq.setName(promptName);
        if (arguments == null) {
            LOGGER.debug("Received prompts get request for {} without arguments", promptName);
            rq.setArguments(Map.of());
        } else {
            LOGGER.debug("Received prompts get request for {} with arguments {}", promptName, arguments.encode());
            final Map untyped = arguments.getMap();
            rq.setArguments(untyped);
        }
        final AiGetPromptResponse aiGetPromptResponse = processRequest(rq, authInfo, AiGetPromptResponse.class);
        // transfer assembled prompt
        return mcpService.mapGetPromptResponse(aiGetPromptResponse);
    }
}
