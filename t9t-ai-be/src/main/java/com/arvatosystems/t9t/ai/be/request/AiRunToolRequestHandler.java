package com.arvatosystems.t9t.ai.be.request;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.AiChatLogDTO;
import com.arvatosystems.t9t.ai.AiConversationRef;
import com.arvatosystems.t9t.ai.AiRoleType;
import com.arvatosystems.t9t.ai.T9tAiException;
import com.arvatosystems.t9t.ai.request.AiRunToolRequest;
import com.arvatosystems.t9t.ai.request.AiRunToolResponse;
import com.arvatosystems.t9t.ai.service.AiToolDescriptor;
import com.arvatosystems.t9t.ai.service.AiToolRegistry;
import com.arvatosystems.t9t.ai.service.IAiChatLogService;
import com.arvatosystems.t9t.ai.service.IAiTool;
import com.arvatosystems.t9t.ai.tools.AbstractAiTool;
import com.arvatosystems.t9t.ai.tools.AbstractAiToolResult;
import com.arvatosystems.t9t.ai.tools.AiToolMediaDataResult;
import com.arvatosystems.t9t.ai.tools.AiToolStringResult;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.server.services.IAuthorize;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.dp.Jdp;
import de.jpaw.json.JsonParser;
import de.jpaw.util.ApplicationException;

public class AiRunToolRequestHandler extends AbstractRequestHandler<AiRunToolRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiRunToolRequestHandler.class);
    private final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);
    private final IAiChatLogService aiChatLogService = Jdp.getRequired(IAiChatLogService.class);

    @Override
    public AiRunToolResponse execute(final RequestContext ctx, final AiRunToolRequest request) {
        final AiRunToolResponse toolResponse = new AiRunToolResponse();
        final AiToolDescriptor<?, ?> tool = AiToolRegistry.get(request.getName());

        if (tool == null) {
            LOGGER.error("Tool {} not found in registry - LLM fantasizing?", request.getName());
            throw new T9tException(T9tAiException.UNKNOWN_AI_TOOL, request.getName());
        }

        final Permissionset permissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.TOOL_CALL, request.getName());
        LOGGER.trace("Backend execution permissions checked for request {}, got {}", request.getName(), permissions);
        if (!permissions.contains(OperationType.EXECUTE)) {
            throw new T9tException(T9tAiException.AI_TOOL_NO_PERMISSION, OperationType.EXECUTE.name() + " on " + request.getName());
        }

        final String json = request.getArguments();
        final Map<String, Object> params = request.getParameters();
        LOGGER.debug("Calling {} with parameters {}", request.getName(), T9tUtil.nvl(json, params));
        // determine parameters as bonaparte object from JSON representation
        try {
            final AbstractAiTool requestObject = tool.requestClass().newInstance();
            if (params != null) {
                MapParser.populateFrom(requestObject, params);
            } else if (json != null) {
                final Map<String, Object> parameters = new JsonParser(json, false).parseObject();
                MapParser.populateFrom(requestObject, parameters);
            }
            // call the tool (hack to get it around type checks)
            final IAiTool toolInstance = tool.toolInstance();
//                if (conversationRef != null) {
//                    // log the call (to be completed)
//                    logToolCall(ctx, conversationRef, tool.name(), requestObject);
//                }
            final AbstractAiToolResult result = toolInstance.performToolCall(ctx, requestObject);
            // convert result to JSON
            if (result == null) {
                LOGGER.debug("Output of tool call to {} returned null (OK)", request.getName());
                setText(toolResponse, "Success! (No data returned by tool.)");
            } else if (result instanceof AiToolStringResult textResult) {
                LOGGER.debug("Output of tool call to {} resulted in string {}", request.getName(), textResult.getText());
                setText(toolResponse, textResult.getText());
            } else if (result instanceof AiToolMediaDataResult mediaDataResult) {
                LOGGER.debug("Output of tool call to {} resulted in MediaData of type {}", request.getName(), mediaDataResult.getMediaData().getMediaType());
                toolResponse.setResponse(mediaDataResult.getMediaData());
            } else {
                LOGGER.debug("Output of tool call to {} returned object of type {}", request.getName(), result.ret$PQON());
                toolResponse.setResponse(result);
            }
        } catch (final Exception e) {
            LOGGER.error("Exception in tool call", e);
            // intentionally hide details from LLM
            throw new T9tException(e instanceof ApplicationException ae ? ae.getErrorCode() : T9tException.GENERAL_EXCEPTION, e.getMessage());
        }
        return toolResponse;

    }

    private void setText(final AiRunToolResponse toolResultMessage, final String text) {
        final MediaData mediaData = new MediaData();
        mediaData.setMediaType(MediaType.TEXT);
        mediaData.setText(text);
        toolResultMessage.setResponse(mediaData);
    }

    private void logToolCall(final RequestContext ctx, final Long conversationRef, final String function, final BonaPortable parameters) {
        // log the input
        final AiChatLogDTO chatLog = new AiChatLogDTO();
        chatLog.setConversationRef(new AiConversationRef(conversationRef));
        chatLog.setRoleType(AiRoleType.SYSTEM);
        chatLog.setFunctionPqon(function);
        chatLog.setFunctionParameter(parameters);
        aiChatLogService.saveAiChatLog(chatLog);
    }
}
