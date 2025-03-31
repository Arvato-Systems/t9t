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
package com.arvatosystems.t9t.base.services;

import com.arvatosystems.t9t.base.api.RequestParameters;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.OperationType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface IDataChangeRequestFlow {

    /**
     * Checks if the crud operation type requires approval.
     *
     * @param pqon              PQON of the DTO
     * @param operationType     {@link OperationType}
     * @return true if the operation requires approval, false otherwise
     */
    boolean requireApproval(@Nonnull String pqon, @Nonnull OperationType operationType);

    /**
     * Creates a data change request.
     *
     * @param ctx           request context
     * @param pqon          PQON of the DTO
     * @param changeId      the change ID
     * @param key           key of the DTO
     * @param crudRequest   actual crud request
     * @param changeComment change comment
     * @param submitChange  true if the change should be submitted for approval, false otherwise
     * @return the change request objectRef
     */
    Long createDataChangeRequest(@Nonnull RequestContext ctx, @Nonnull String pqon, @Nonnull String changeId, @Nullable BonaPortable key,
        @Nonnull RequestParameters crudRequest, @Nullable String changeComment, @Nullable Boolean submitChange);

    /**
     * Verify if the change request for the given objectRef matches with the key and data. And also if the status of the change request is ACTIVATED
     * @param objectRef     objectRef of the change request
     * @param key           key of the DTO
     * @param data          data of the DTO
     * @return true if the change request is valid to activate, false otherwise
     */
    boolean isChangeRequestValidToActivate(@Nonnull Long objectRef, @Nullable BonaPortable key, @Nonnull BonaPortable data);
}
