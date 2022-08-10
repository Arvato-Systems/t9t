/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.genconf.jpa.request;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.genconf.jpa.entities.ConfigEntity;
import com.arvatosystems.t9t.genconf.jpa.mapping.IConfigDTOMapper;
import com.arvatosystems.t9t.genconf.jpa.persistence.IConfigEntityResolver;
import com.arvatosystems.t9t.genconf.request.ReadConfigMultipleEntriesRequest;
import com.arvatosystems.t9t.genconf.request.ReadConfigMultipleEntriesResponse;

import de.jpaw.dp.Jdp;

import java.util.List;

public class ReadConfigMultipleEntriesRequestHandler extends AbstractRequestHandler<ReadConfigMultipleEntriesRequest> {
    private final IConfigEntityResolver resolver = Jdp.getRequired(IConfigEntityResolver.class);
    private final IConfigDTOMapper mapper = Jdp.getRequired(IConfigDTOMapper.class);

    @Override
    public boolean isReadOnly(final ReadConfigMultipleEntriesRequest params) {
        return true;
    }

    @Override
    public ReadConfigMultipleEntriesResponse execute(final RequestContext ctx, final ReadConfigMultipleEntriesRequest request) throws Exception {
        final ReadConfigMultipleEntriesResponse resp = new ReadConfigMultipleEntriesResponse();
        final String tenantId = request.getReadGlobalTenant() ? T9tConstants.GLOBAL_TENANT_ID : ctx.tenantId;
        final List<ConfigEntity> configEntities = resolver.findByGroup(true, tenantId, request.getConfigGroup());
        resp.setEntries(mapper.mapListToDto(configEntities));
        return resp;
    }
}
