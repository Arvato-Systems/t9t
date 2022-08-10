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
import com.arvatosystems.t9t.genconf.ConfigKey;
import com.arvatosystems.t9t.genconf.jpa.entities.ConfigEntity;
import com.arvatosystems.t9t.genconf.jpa.mapping.IConfigDTOMapper;
import com.arvatosystems.t9t.genconf.jpa.persistence.IConfigEntityResolver;
import com.arvatosystems.t9t.genconf.request.ReadConfigWithDefaultsRequest;
import com.arvatosystems.t9t.genconf.request.ReadConfigWithDefaultsResponse;

import de.jpaw.dp.Jdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadConfigWithDefaultsRequestHandler extends AbstractRequestHandler<ReadConfigWithDefaultsRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadConfigWithDefaultsRequestHandler.class);
    private static final Long ZERO = Long.valueOf(0L);

    private final IConfigEntityResolver resolver = Jdp.getRequired(IConfigEntityResolver.class);
    private final IConfigDTOMapper mapper = Jdp.getRequired(IConfigDTOMapper.class);

    @Override
    public boolean isReadOnly(final ReadConfigWithDefaultsRequest params) {
        return true;
    }

    @Override
    public ReadConfigWithDefaultsResponse execute(final RequestContext ctx, final ReadConfigWithDefaultsRequest request) throws Exception {
        final ReadConfigWithDefaultsResponse response = new ReadConfigWithDefaultsResponse();

        final ConfigKey key = request.getKey();
        LOGGER.debug("read {} for tenant {}", key, ctx.tenantId);
        ConfigEntity e = resolver.findByKey(true, ctx.tenantId, key.getConfigGroup(), key.getConfigKey(), key.getGenericRef1(), key.getGenericRef2());
        if (e == null) {
            // need defaults. Create a new key object only once!
            response.setDefaultsUsed(true);
            if (request.getRetryWithDefaultRef2() && !ZERO.equals(key.getGenericRef2())) {
                // retry if the original key consisted of a generic ref2
                e = resolver.findByKey(true, ctx.tenantId, key.getConfigGroup(), key.getConfigKey(), key.getGenericRef1(), ZERO);
            }
            if (e == null && request.getRetryWithDefaultRef1() && !ZERO.equals(key.getGenericRef1())) {
                e = resolver.findByKey(true, ctx.tenantId, key.getConfigGroup(), key.getConfigKey(), ZERO, ZERO);
            }
            if (e == null && request.getRetryWithDefaultTenant() && !ctx.tenantId.equals(T9tConstants.GLOBAL_TENANT_ID)) {
                e = resolver.findByKey(true, T9tConstants.GLOBAL_TENANT_ID, key.getConfigGroup(), key.getConfigKey(), ZERO, ZERO);
            }
        }
        // compatibility to previous implementation:
        if (e == null) {
            response.setDefaultsUsed(false); // don't pretend a default entry was returned
        }

        response.setConfigurationResult(mapper.mapToDto(e));
        response.setReturnCode(0);
        return response;
    }
}
