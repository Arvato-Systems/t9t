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
            final Map<String, ChangeWorkFlowConfigDTO> configMap = new HashMap<>(response.getDataList().size());
            for (final DataWithTracking<ChangeWorkFlowConfigDTO, FullTrackingWithVersion> dwt : response.getDataList()) {
                configMap.put(dwt.getData().getPqon(), dwt.getData());
            }
            return configMap;
        });
    }
}
