package com.arvatosystems.t9t.ai.be.request;

import com.arvatosystems.t9t.ai.AiPromptDTO;
import com.arvatosystems.t9t.ai.mcp.McpUtils;
import com.arvatosystems.t9t.ai.request.AiGetPromptsRequest;
import com.arvatosystems.t9t.ai.request.AiGetPromptsResponse;
import com.arvatosystems.t9t.ai.request.AiPromptSearchRequest;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.server.services.IAuthorize;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AiGetPromptsRequestHandler extends AbstractReadOnlyRequestHandler<AiGetPromptsRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiGetPromptsRequestHandler.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);
    private final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);

    @Nonnull
    @Override
    public AiGetPromptsResponse execute(@Nonnull final RequestContext ctx, @Nonnull final AiGetPromptsRequest request) throws Exception {
        final AiPromptSearchRequest searchRequest = new AiPromptSearchRequest();
        if (request.getOffset() != -1) {
            searchRequest.setOffset(request.getOffset());
            searchRequest.setLimit(McpUtils.PROMPT_LIST_PAGE_SIZE + 1); // +1 to detect if there are more results than requested
        }
        final List<String> allowedPromptIds = getAllowedPromptIds(ctx);
        if (!allowedPromptIds.isEmpty()) {
            final AsciiFilter promptFilter = new AsciiFilter();
            promptFilter.setFieldName(AiPromptDTO.meta$$promptId.getName());
            promptFilter.setValueList(allowedPromptIds);
            searchRequest.setSearchFilter(promptFilter);
        }
        final ReadAllResponse<AiPromptDTO, FullTrackingWithVersion> searchResponse = executor.executeSynchronousAndCheckResult(ctx, searchRequest, ReadAllResponse.class);
        final List<AiPromptDTO> prompts = new ArrayList<>(searchResponse.getDataList().size());
        for (int i = 0; i < searchResponse.getDataList().size() && i < McpUtils.PROMPT_LIST_PAGE_SIZE; i++) {
            final DataWithTrackingS<AiPromptDTO, FullTrackingWithVersion> dwt = searchResponse.getDataList().get(i);
            prompts.add(dwt.getData());
        }
        final AiGetPromptsResponse response = new AiGetPromptsResponse();
        response.setPrompts(prompts);
        if (searchResponse.getDataList().size() > McpUtils.PROMPT_LIST_PAGE_SIZE) {
            response.setNextOffset(request.getOffset() + McpUtils.PROMPT_LIST_PAGE_SIZE);
        }
        return response;
    }

    @Nonnull
    private List<String> getAllowedPromptIds(@Nonnull final RequestContext ctx) {
        final List<String> allowedPromptIds = new ArrayList<>();
        final List<PermissionEntry> permissionEntries = authorizer.getAllPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.PROMPT);
        for (final PermissionEntry entry : permissionEntries) {
            if (entry.getResourceId().equalsIgnoreCase(PermissionType.PROMPT + ".") && entry.getPermissions().contains(OperationType.EXECUTE)) {
                // if all allowed then return empty list
                allowedPromptIds.clear();
                break;
            } else if (entry.getResourceId().length() > 2 && entry.getPermissions().contains(OperationType.EXECUTE)) {
                allowedPromptIds.add(entry.getResourceId().substring(2));
            }
        }
        return allowedPromptIds;
    }
}
