package com.arvatosystems.t9t.changeRequest.services;

import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import jakarta.annotation.Nonnull;

public interface IChangeWorkFlowConfigCache {

    /**
     * Retrieves {@link ChangeWorkFlowConfigDTO} config from the cache by PQON, or null if no data is available.
     *
     * @param pqon  PQON as cache key
     * @return {@link ChangeWorkFlowConfigDTO} or null if no data is available
     */
    ChangeWorkFlowConfigDTO getOrNull(@Nonnull String pqon);
}
