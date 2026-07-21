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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.ai.T9tAiTools;
import com.arvatosystems.t9t.ai.mcp.AiToolSpecification;
import com.arvatosystems.t9t.ai.request.AiGetToolsRequest;
import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
import com.arvatosystems.t9t.ai.service.AiToolRegistry;
import com.arvatosystems.t9t.ai.tools.AiToolMediaDataResult;
import com.arvatosystems.t9t.ai.tools.AiToolNoResult;
import com.arvatosystems.t9t.ai.tools.AiToolStringResult;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.server.services.IAuthorize;

public class AiGetToolsRequestHandler extends AbstractReadOnlyRequestHandler<AiGetToolsRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiGetToolsRequestHandler.class);

    private final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);

    @Override
    public AiGetToolsResponse execute(final RequestContext ctx, final AiGetToolsRequest request) {
        final List<AiToolSpecification> toolSpecifications = new ArrayList<>(AiToolRegistry.size());
        AiToolRegistry.forEach(tool -> {
            final ClassDefinition metaData = tool.requestClass().getMetaData();
            final Permissionset permissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.TOOL_CALL, tool.name());
            if (!permissions.contains(OperationType.EXECUTE)) {
                return; // skip tool if no EXECUTE permission
            }
            final AiToolSpecification spec = new AiToolSpecification();
            spec.setName(tool.name());
            spec.setTitle(null);  // TODO: not yet available
            spec.setDescription(T9tAiTools.getToolDescription(metaData));
            spec.setInputSchema(T9tAiTools.buildJsonSchemaObject(metaData, null));
            // optionally specify result structure
            final ClassDefinition resultMetaData = tool.resultClass().getMetaData();
            if (resultMetaData != null) {
                final String resultType = resultMetaData.getClassRef().getCanonicalName();
                if (resultType.equals(AiToolNoResult.class.getCanonicalName())) {
                    spec.setOutputSchema(null); // no result
                } else if (resultType.equals(AiToolStringResult.class.getCanonicalName())) {
                    spec.setOutputSchema(null); // uses unstructured result
                } else if (resultType.equals(AiToolMediaDataResult.class.getCanonicalName())) {
                    spec.setOutputSchema(null); // uses unstructured result
                } else {
                    spec.setOutputSchema(T9tAiTools.buildJsonSchemaObject(resultMetaData, null));
                }
            }

            toolSpecifications.add(spec);
        });
        LOGGER.info("User {} has permissions to {} of total {} AI tools", ctx.internalHeaderParameters.getJwtInfo().getUserId(), toolSpecifications.size(), AiToolRegistry.size());
        final AiGetToolsResponse response = new AiGetToolsResponse();
        response.setTools(toolSpecifications);
        return response;
    }
}
