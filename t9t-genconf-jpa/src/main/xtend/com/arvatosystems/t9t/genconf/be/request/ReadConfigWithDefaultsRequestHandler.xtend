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
package com.arvatosystems.t9t.genconf.be.request

import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.genconf.jpa.mapping.IConfigDTOMapper
import com.arvatosystems.t9t.genconf.jpa.persistence.IConfigEntityResolver
import com.arvatosystems.t9t.genconf.request.ReadConfigWithDefaultsRequest
import com.arvatosystems.t9t.genconf.request.ReadConfigWithDefaultsResponse
import de.jpaw.dp.Inject
import com.arvatosystems.t9t.base.services.RequestContext
import de.jpaw.annotations.AddLogger

@AddLogger
class ReadConfigWithDefaultsRequestHandler extends AbstractRequestHandler<ReadConfigWithDefaultsRequest> {

    private static final Long ZERO = Long.valueOf(0L);
    @Inject IConfigEntityResolver resolver
    @Inject IConfigDTOMapper mapper

    override boolean isReadOnly(ReadConfigWithDefaultsRequest params) {
        return true;
    }

    override ReadConfigWithDefaultsResponse execute(RequestContext ctx, ReadConfigWithDefaultsRequest request) throws Exception {
        val response = new ReadConfigWithDefaultsResponse();

        val key = request.key
        LOGGER.debug("read {} for tenant {} ({})", key, ctx.tenantId, ctx.tenantRef)
        var e = resolver.findByKey(true, ctx.tenantRef, key.getConfigGroup(), key.getConfigKey(), key.getGenericRef1(), key.getGenericRef2());
        if (e === null) {
            // need defaults. Create a new key object only once!
            response.setDefaultsUsed(true);
            if (request.getRetryWithDefaultRef2() && !ZERO.equals(key.getGenericRef2())) {
                // retry if the original key consisted of a generic ref2
                e = resolver.findByKey(true, ctx.tenantRef, key.getConfigGroup(), key.getConfigKey(), key.getGenericRef1(), ZERO);
            }
            if ((e === null) && request.getRetryWithDefaultRef1() && !ZERO.equals(key.getGenericRef1())) {
                e = resolver.findByKey(true, ctx.tenantRef, key.getConfigGroup(), key.getConfigKey(), ZERO, ZERO);
            }
            if ((e === null) && request.getRetryWithDefaultTenant() && !ctx.tenantRef.equals(T9tConstants.GLOBAL_TENANT_REF42)) {
                e = resolver.findByKey(true, T9tConstants.GLOBAL_TENANT_REF42, key.getConfigGroup(), key.getConfigKey(), ZERO, ZERO);
            }
        }
        // compatibility to previous implementation:
        if (e === null) {
            response.setDefaultsUsed(false); // don't pretend a default entry was returned
        }

        response.setConfigurationResult(mapper.mapToDto(e));
        response.setReturnCode(0);
        return response;
    }
}
