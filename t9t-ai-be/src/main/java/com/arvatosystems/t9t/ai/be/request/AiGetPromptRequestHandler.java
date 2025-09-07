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
package com.arvatosystems.t9t.ai.be.request;

import com.arvatosystems.t9t.ai.AiPromptDTO;
import com.arvatosystems.t9t.ai.AiPromptParameter;
import com.arvatosystems.t9t.ai.T9tAiException;
import com.arvatosystems.t9t.ai.request.AiGetPromptRequest;
import com.arvatosystems.t9t.ai.request.AiGetPromptResponse;
import com.arvatosystems.t9t.ai.request.AiPromptSearchRequest;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.server.services.IAuthorize;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AiGetPromptRequestHandler extends AbstractReadOnlyRequestHandler<AiGetPromptRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiGetPromptRequestHandler.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);
    private final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);

    @Nonnull
    @Override
    public AiGetPromptResponse execute(@Nonnull final RequestContext ctx, @Nonnull final AiGetPromptRequest request) throws Exception {

        final Permissionset permissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.PROMPT, request.getName());
        if (!permissions.contains(OperationType.EXECUTE)) {
            throw new T9tException(T9tAiException.AI_PROMPT_NO_PERMISSION, OperationType.EXECUTE.name() + " on " + request.getName());
        }

        final AiPromptSearchRequest searchRequest = new AiPromptSearchRequest();
        final AsciiFilter filter = new AsciiFilter();
        filter.setFieldName(AiPromptDTO.meta$$promptId.getName());
        filter.setEqualsValue(request.getName());
        searchRequest.setSearchFilter(filter);
        final ReadAllResponse<AiPromptDTO, FullTrackingWithVersion> searchResponse = executor.executeSynchronousAndCheckResult(ctx, searchRequest, ReadAllResponse.class);
        if (searchResponse.getDataList().isEmpty()) {
            LOGGER.error("Prompt with name {} not found", request.getName());
            throw new T9tException(T9tAiException.INVALID_PROMPT_NAME, "Prompt with name " + request.getName() + " not found.");
        }
        final AiPromptDTO prompt = searchResponse.getDataList().getFirst().getData();
        LOGGER.debug("Processing prompt {} with {} parameters ({} supplied by request)", prompt.getPromptId(), prompt.getParameters().getParameters().size(), request.getArguments() != null ? request.getArguments().size() : 0);
        String promptText = prompt.getPrompt();
        for (final Map.Entry<String, AiPromptParameter> entry: prompt.getParameters().getParameters().entrySet()) {
            final String paramName = entry.getKey();
            final AiPromptParameter param = entry.getValue();
            final Object paramValue = request.getArguments().get(paramName);
            if (paramValue != null) {
                promptText = promptText.replace("${" + paramName + "}", paramValue.toString());
            } else if (!param.getIsRequired()) {
                // just replace with empty string
                promptText = promptText.replace("${" + paramName + "}", "");
            } else {
                LOGGER.error("Required argument {} is missing for prompt {}", paramName, request.getName());
                throw new T9tException(T9tAiException.MISSING_REQUIRED_ARGUMENT, "Required argument " + paramName + " is missing for prompt " + request.getName());
            }
        }
        final AiGetPromptResponse response = new AiGetPromptResponse();
        response.setPrompt(promptText);
        response.setDescription(prompt.getDescription());
        return response;
    }
}
