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
package com.arvatosystems.t9t.base.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.be.impl.SimpleCallOutExecutor;
import com.arvatosystems.t9t.base.request.ExecuteRemoteRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.server.services.IAuthorize;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.dp.Jdp;

public class ExecuteRemoteRequestHandler extends AbstractRequestHandler<ExecuteRemoteRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteRemoteRequestHandler.class);

    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);
    protected final IAuthorize authorizator = Jdp.getRequired(IAuthorize.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, ExecuteRemoteRequest request) throws Exception {
        RequestParameters remoteRequest = request.getRemoteRequest();
        Permissionset permissions = authorizator.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.EXTERNAL, remoteRequest.ret$PQON());
        LOGGER.debug("External execution execution permissions checked for request {}, got {}", remoteRequest.ret$PQON(), permissions);
        boolean allowed = permissions.contains(OperationType.EXECUTE);
        if (!allowed) {
            throw new T9tException(T9tException.ACCESS_DENIED, "No EXECUTE permission on {}.{}", PermissionType.EXTERNAL, remoteRequest.ret$PQON());
        }
        ctx.statusText = remoteRequest.ret$PQON();
        // obtain an remoter for single use
        SimpleCallOutExecutor remoteClient = new SimpleCallOutExecutor(request.getUrl(), MediaType.COMPACT_BONAPARTE);
        return remoteClient.execute(ctx, request.getRemoteRequest());
    }
}
