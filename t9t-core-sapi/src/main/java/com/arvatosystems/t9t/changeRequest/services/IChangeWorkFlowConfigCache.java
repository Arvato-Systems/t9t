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
