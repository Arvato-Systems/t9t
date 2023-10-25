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
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.jpa.impl.AbstractMonitoringSearchRequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.jpa.mapping.IMessageDTOMapper;
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageEntityResolver;
import com.arvatosystems.t9t.msglog.request.MessageSearchRequest;
import com.arvatosystems.t9t.server.services.IAuthorize;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;

// do not use the searchWithTotals super class because the result is very likely HUGE. Instead, use the shadow DB if available
public class MessageSearchRequestHandler extends AbstractMonitoringSearchRequestHandler<MessageSearchRequest> {

    protected final IMessageEntityResolver resolver = Jdp.getRequired(IMessageEntityResolver.class);
    protected final IMessageDTOMapper mapper = Jdp.getRequired(IMessageDTOMapper.class);
    protected final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);

    @Override
    public ReadAllResponse<MessageDTO, NoTracking> execute(final RequestContext ctx, final MessageSearchRequest request) throws Exception {
        final Permissionset permissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.BACKEND, request.ret$PQON());
        if (!permissions.contains(OperationType.CUSTOM)) {
            throw new T9tException(T9tException.NOT_AUTHORIZED, OperationType.CUSTOM.name() + " on " + request.ret$PQON());
        }
        if (!permissions.contains(OperationType.ADMIN)) {
            final UnicodeFilter filterByUserId = SearchFilters.equalsFilter(MessageDTO.meta$$userId.getName(), ctx.userId);
            request.setSearchFilter(SearchFilters.and(request.getSearchFilter(), filterByUserId));
        }
        mapper.processSearchPrefixForDB(request);
        return mapper.createReadAllResponse(resolver.search(request), request.getSearchOutputTarget());
    }
}
