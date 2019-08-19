/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.core.be.request

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.auth.PermissionType
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.core.CannedRequestKey
import com.arvatosystems.t9t.core.request.ExecutePreparedRequest
import com.arvatosystems.t9t.core.services.ICannedRequestResolver
import com.arvatosystems.t9t.server.services.IAuthorize
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.dp.Inject

@AddLogger
class ExecutePreparedRequestHandler extends AbstractRequestHandler<ExecutePreparedRequest> {
    @Inject protected ICannedRequestResolver          resolver
    @Inject protected IExecutor                       executor
    @Inject protected IAuthorize                      authorizator

    override ServiceResponse execute(RequestContext ctx, ExecutePreparedRequest rq) {
        val permissions = authorizator.getPermissions(ctx.internalHeaderParameters.jwtInfo, PermissionType.PREPARED, rq.requestId)
        LOGGER.debug("Prepared process execution permissions checked for request {}, got {}", rq.ret$PQON, permissions)
        if (!permissions.contains(OperationType.EXECUTE)) {
            return new ServiceResponse => [
                returnCode          = T9tException.NOT_AUTHORIZED
                errorDetails        = OperationType.EXECUTE.name
            ]
        }
        // access granted!
        // resolve the DTO from DB
        val dto = resolver.getDTO(new CannedRequestKey(rq.requestId))
        LOGGER.info("Executing resolved canned request of ID {} for request {}", dto.requestId, dto.request.ret$PQON)
        return executor.executeSynchronous(dto.request)
    }
}
