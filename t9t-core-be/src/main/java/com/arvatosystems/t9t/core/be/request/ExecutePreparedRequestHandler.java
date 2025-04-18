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
package com.arvatosystems.t9t.core.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.CannedRequestDTO;
import com.arvatosystems.t9t.core.CannedRequestKey;
import com.arvatosystems.t9t.core.request.ExecutePreparedRequest;
import com.arvatosystems.t9t.core.services.ICannedRequestResolver;
import com.arvatosystems.t9t.server.services.IAuthorize;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutePreparedRequestHandler extends AbstractRequestHandler<ExecutePreparedRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutePreparedRequestHandler.class);
    private final ICannedRequestResolver resolver = Jdp.getRequired(ICannedRequestResolver.class);
    private final IExecutor executor = Jdp.getRequired(IExecutor.class);
    private final IAuthorize authorizator = Jdp.getRequired(IAuthorize.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final ExecutePreparedRequest rq) throws Exception {
        final Permissionset permissions = authorizator.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.PREPARED, rq.getRequestId());
        LOGGER.debug("Prepared process execution permissions checked for request {}:{}, got {}", rq.ret$PQON(), rq.getRequestId(), permissions);
        if (!permissions.contains(OperationType.EXECUTE)) {
            final ServiceResponse response = new ServiceResponse();
            response.setReturnCode(T9tException.NOT_AUTHORIZED);
            response.setErrorDetails(OperationType.EXECUTE.name());
            return response;
        }
        // access granted!
        // resolve the DTO from DB
        final CannedRequestDTO dto = resolver.getDTO(new CannedRequestKey(rq.getRequestId()));
        LOGGER.info("Executing resolved canned request of ID {} for request {}", dto.getRequestId(), dto.getRequest().ret$PQON());
        return executor.executeSynchronous(dto.getRequest());
    }
}
