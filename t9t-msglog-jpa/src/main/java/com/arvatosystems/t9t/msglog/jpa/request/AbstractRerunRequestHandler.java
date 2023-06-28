/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.msglog.jpa.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.msglog.jpa.entities.MessageEntity;
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageEntityResolver;
import com.arvatosystems.t9t.server.services.IAuthorize;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;

public abstract class AbstractRerunRequestHandler<T extends RequestParameters> extends AbstractRequestHandler<T> {
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);
    protected final IMessageEntityResolver resolver = Jdp.getRequired(IMessageEntityResolver.class);
    protected final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);

    protected void checkPermission(final RequestContext ctx, final RequestParameters rq) {
        // additional permission check for CUSTOM and ADMIN
        final Permissionset permissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.BACKEND, rq.ret$PQON());
        if (!permissions.contains(OperationType.CUSTOM)) {
            throw new T9tException(T9tException.NOT_AUTHORIZED, OperationType.CUSTOM.name() + " on " + rq.ret$PQON());
        }
        if (!permissions.contains(OperationType.ADMIN)) {
            throw new T9tException(T9tException.NOT_AUTHORIZED, OperationType.ADMIN.name() + " on " + rq.ret$PQON());
        }
    }

    protected MessageEntity getLoggedRequestByProcessRef(final RequestContext ctx, final Long processRef) {
        final MessageEntity loggedRequest = resolver.find(processRef);
        if (loggedRequest == null) {
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, processRef);
        }
        final RequestParameters recordedRequest = loggedRequest.getRequestParameters();
        if (recordedRequest == null) {
            throw new T9tException(T9tException.RERUN_NOT_POSSIBLE_NO_RECORDED_REQUEST, processRef);
        }
        // also check permissions for this request
        final Permissionset permissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.BACKEND,
          recordedRequest.ret$PQON());
        if (!permissions.contains(OperationType.EXECUTE)) {
            throw new T9tException(T9tException.NOT_AUTHORIZED, OperationType.EXECUTE.name() + " on " + recordedRequest.ret$PQON());
        }
        return loggedRequest;
    }
}
