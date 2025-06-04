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
package com.arvatosystems.t9t.zkui.services.impl;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.request.ChangeWorkFlowConfigSearchRequest;
import com.arvatosystems.t9t.zkui.services.IChangeWorkFlowConfigDAO;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.util.FreezeTools;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Singleton
public class ChangeWorkFlowConfigDAO implements IChangeWorkFlowConfigDAO {

    protected final IT9tRemoteUtils t9tRemoteUtils = Jdp.getRequired(IT9tRemoteUtils.class);
    protected final Cache<String, Map<String, ChangeWorkFlowConfigDTO>> configCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    @Override
    public ChangeWorkFlowConfigDTO getChangeWorkFlowConfigByPqon(@Nonnull final String pqon) {
        final Map<String, ChangeWorkFlowConfigDTO> configMap = getChangeWorkFlowConfigMap();
        return configMap.get(pqon);
    }

    @Override
    public void invalidateCache() {
        configCache.invalidate(ApplicationSession.get().getTenantId());
    }

    @SuppressWarnings("unchecked")
    protected Map<String, ChangeWorkFlowConfigDTO> getChangeWorkFlowConfigMap() {
        return configCache.get(ApplicationSession.get().getTenantId(), key -> {
            final ChangeWorkFlowConfigSearchRequest request = new ChangeWorkFlowConfigSearchRequest();
            final ReadAllResponse<ChangeWorkFlowConfigDTO, FullTrackingWithVersion> response = t9tRemoteUtils.executeExpectOk(request, ReadAllResponse.class);
            final Map<String, ChangeWorkFlowConfigDTO> configMap = new HashMap<>(FreezeTools.getInitialHashMapCapacity(response.getDataList().size()));
            for (final DataWithTracking<ChangeWorkFlowConfigDTO, FullTrackingWithVersion> dwt : response.getDataList()) {
                configMap.put(dwt.getData().getPqon(), dwt.getData());
            }
            return configMap;
        });
    }
}
