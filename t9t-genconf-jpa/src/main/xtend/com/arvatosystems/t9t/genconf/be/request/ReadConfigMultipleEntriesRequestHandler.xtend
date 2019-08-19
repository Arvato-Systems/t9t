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
package com.arvatosystems.t9t.genconf.be.request;

import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.genconf.jpa.mapping.IConfigDTOMapper
import com.arvatosystems.t9t.genconf.jpa.persistence.IConfigEntityResolver
import com.arvatosystems.t9t.genconf.request.ReadConfigMultipleEntriesRequest
import com.arvatosystems.t9t.genconf.request.ReadConfigMultipleEntriesResponse
import de.jpaw.dp.Inject

public class ReadConfigMultipleEntriesRequestHandler extends AbstractRequestHandler<ReadConfigMultipleEntriesRequest> {
    @Inject IConfigEntityResolver resolver
    @Inject IConfigDTOMapper mapper

    override boolean isReadOnly(ReadConfigMultipleEntriesRequest params) {
        return true;
    }

    override ReadConfigMultipleEntriesResponse execute(RequestContext ctx, ReadConfigMultipleEntriesRequest request) throws Exception {

        return new ReadConfigMultipleEntriesResponse => [
            entries = mapper.mapListToDto((resolver.findByGroup(true, (if (request.readGlobalTenant) T9tConstants.GLOBAL_TENANT_REF42 else ctx.tenantRef), request.getConfigGroup())))
        ]
    }
}
