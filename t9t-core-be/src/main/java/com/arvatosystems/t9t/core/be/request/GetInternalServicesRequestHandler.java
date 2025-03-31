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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.UplinkConfiguration;
import com.arvatosystems.t9t.core.request.GetInternalServicesRequest;
import com.arvatosystems.t9t.core.request.GetInternalServicesResponse;

public class GetInternalServicesRequestHandler extends AbstractReadOnlyRequestHandler<GetInternalServicesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetInternalServicesRequestHandler.class);
    private static final String MY_OWN_SERVER_ID = ConfigProvider.getConfiguration().getServerIdSelf();

    @Override
    public GetInternalServicesResponse execute(final RequestContext ctx, final GetInternalServicesRequest rq) throws Exception {

        final GetInternalServicesResponse resp = new GetInternalServicesResponse();
        final List<UplinkConfiguration> uplinkConfigs = ConfigProvider.getConfiguration().getUplinkConfiguration();
        if (uplinkConfigs == null) {
            LOGGER.trace("No uplink configuration found!");
            resp.setInternalServiceKeys(Collections.emptyList());
        } else {
            LOGGER.trace("Total {} uplink configurations found.", uplinkConfigs.size());
            final List<String> internalServiceKeys = new ArrayList<>(uplinkConfigs.size());
            for (final UplinkConfiguration uplinkConfig : uplinkConfigs) {
                if (T9tUtil.isTrue(uplinkConfig.getInternalService()) && !uplinkConfig.getKey().equals(MY_OWN_SERVER_ID)) {
                    internalServiceKeys.add(uplinkConfig.getKey());
                }
            }
            LOGGER.trace("{} internal services found in uplink configuration", internalServiceKeys.size());
            resp.setInternalServiceKeys(internalServiceKeys);
        }
        return resp;
    }
}
