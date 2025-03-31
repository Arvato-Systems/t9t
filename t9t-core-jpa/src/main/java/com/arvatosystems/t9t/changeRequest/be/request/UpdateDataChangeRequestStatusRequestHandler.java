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
package com.arvatosystems.t9t.changeRequest.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowStatus;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestDTO;
import com.arvatosystems.t9t.changeRequest.jpa.entities.DataChangeRequestEntity;
import com.arvatosystems.t9t.changeRequest.jpa.persistence.IDataChangeRequestEntityResolver;
import com.arvatosystems.t9t.changeRequest.request.UpdateDataChangeRequestStatusRequest;
import com.arvatosystems.t9t.changeRequest.services.IChangeWorkFlowConfigCache;
import com.arvatosystems.t9t.server.services.IAuthorize;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;

public class UpdateDataChangeRequestStatusRequestHandler extends AbstractRequestHandler<UpdateDataChangeRequestStatusRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataChangeRequestStatusRequestHandler.class);
    private static final EnumSet<ChangeWorkFlowStatus> CHANGE_WORK_FLOW_IN_PROGRESS = EnumSet.of(ChangeWorkFlowStatus.WORK_IN_PROGRESS,
        ChangeWorkFlowStatus.TO_REVIEW, ChangeWorkFlowStatus.CONFLICT);
    private static final String SQL_UPDATE_CHANGE_REQUEST = "UPDATE " + DataChangeRequestEntity.class.getSimpleName()
        + " SET status = :status WHERE objectRef != :objectRef AND status NOT IN :excludeStatus AND pqon = :pqon AND key = :key";

    protected final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);
    protected final IDataChangeRequestEntityResolver resolver = Jdp.getRequired(IDataChangeRequestEntityResolver.class);
    protected final IChangeWorkFlowConfigCache configCache = Jdp.getRequired(IChangeWorkFlowConfigCache.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Nonnull
    @Override
    public ServiceResponse execute(@Nonnull final RequestContext ctx, @Nonnull final UpdateDataChangeRequestStatusRequest updateRequest) throws Exception {
        LOGGER.debug("Updating data change request status to {} for ref: {}", updateRequest.getNewStatus(), updateRequest.getDataChangeRequestRef());
        final DataChangeRequestEntity changeRequest = resolver.getEntityData(updateRequest.getDataChangeRequestRef());
        final ChangeWorkFlowStatus currentStatus = changeRequest.getStatus();
        final ChangeWorkFlowStatus newStatus = updateRequest.getNewStatus();
        final ChangeWorkFlowConfigDTO config = configCache.getOrNull(changeRequest.getPqon());
        final CrudAnyKeyRequest<?, ?> crudRequest = getCrudRequest(changeRequest);
        final boolean separateActivation = config != null && config.getSeparateActivation();

        permissionCheck(ctx, crudRequest, newStatus, separateActivation);
        validateStatus(ctx, config, changeRequest, newStatus);
        updateStatus(ctx, changeRequest, newStatus, updateRequest.getComment(), separateActivation);
        LOGGER.debug("Change request status updated from {} to {} for ref: {}", currentStatus, newStatus, updateRequest.getDataChangeRequestRef());

        if (ChangeWorkFlowStatus.ACTIVATED == changeRequest.getStatus()) {
            LOGGER.info("Activating data change for PQON: {}, changeId: {}, key: [{}]", changeRequest.getPqon(), changeRequest.getChangeId(),
            changeRequest.getKey());
            // Set the change request ref for the validation in CRUD request handler
            crudRequest.setChangeRequestRef(changeRequest.getObjectRef());
            final ServiceResponse crudResponse = executor.executeSynchronous(ctx, crudRequest);
            if (crudResponse.getReturnCode() != 0) {
                LOGGER.error("Error while activating data change for PQON: {}, changeId: {}, key: [{}], ref:{}, errorMessage:{}, errorDetails:{}",
                    changeRequest.getPqon(), changeRequest.getChangeId(), changeRequest.getKey(), changeRequest.getObjectRef(), crudResponse.getErrorMessage(),
                    crudResponse.getErrorDetails());
                throw new T9tException(T9tException.CHANGE_REQUEST_ACTIVATION_ERROR, crudResponse.getErrorMessage());
            }
            // Attempt to check the clear cache by DTO class name retrieved from PQON.
            String pqon = changeRequest.getPqon();
            executor.clearCache(pqon.substring(pqon.lastIndexOf('.') + 1), null);

            markConflict(changeRequest);
        }

        return ok();
    }

    protected void updateStatus(@Nonnull final RequestContext ctx, @Nonnull final DataChangeRequestEntity changeRequest,
        @Nonnull final ChangeWorkFlowStatus newStatus, @Nullable final String comment, final boolean separateActivation) {
        if (ChangeWorkFlowStatus.WORK_IN_PROGRESS == newStatus) {
            // Remove all workflow tracking information and comments because change request is reset to initial status.
            changeRequest.setUserIdSubmitted(null);
            changeRequest.setWhenSubmitted(null);
            changeRequest.setTextSubmitted(null);
            changeRequest.setUserIdApprove(null);
            changeRequest.setWhenDecided(null);
            changeRequest.setTextDecision(null);
        } else if (ChangeWorkFlowStatus.TO_REVIEW == newStatus) {
            // Populate the workflow tracking information for in-review status
            changeRequest.setUserIdSubmitted(ctx.userId);
            changeRequest.setWhenSubmitted(ctx.executionStart);
            changeRequest.setTextSubmitted(comment);
        } else if (ChangeWorkFlowStatus.APPROVED == newStatus || ChangeWorkFlowStatus.REJECTED == newStatus) {
            // Populate the workflow tracking information for approved or rejected status
            changeRequest.setUserIdApprove(ctx.userId);
            changeRequest.setWhenDecided(ctx.executionStart);
            changeRequest.setTextDecision(comment);
        }

        if (ChangeWorkFlowStatus.ACTIVATED == newStatus || (ChangeWorkFlowStatus.APPROVED == newStatus && !separateActivation)) {
            // Populate the workflow tracking information for activated status
            changeRequest.setUserIdActivated(ctx.userId);
            changeRequest.setWhenActivated(ctx.executionStart);
            changeRequest.setStatus(ChangeWorkFlowStatus.ACTIVATED);
        } else {
            changeRequest.setStatus(newStatus);
        }

    }

    /** Check if user have permission to set the given new status
     * Following permissions are required to set the status to the given new status:
     * WORK_IN_PROGRESS:
     *      CREATE -> If the crud request is for CREATE
     *      DELETE -> If the crud request is for DELETE
     *      MERGE  -> If the crud request is for MERGE
     *      UPDATE or ACTIVATE or INACTIVATE -> If the crud request is for UPDATE (ACTIVATE/INACTIVATE are converted to UPDATE)
     * TO_REVIEW: same as WORK_IN_PROGRESS
     * CONFLICT: same as WORK_IN_PROGRESS
     * APPROVED: APPROVE
     * REJECTED: REJECT
     * ACTIVATED: ACTIVATE
     */
    protected void permissionCheck(@Nonnull final RequestContext ctx, @Nonnull final CrudAnyKeyRequest<?, ?> crudRequest,
        @Nonnull final ChangeWorkFlowStatus newStatus, final boolean separateActivation) {

        // Check permission based on CRUD request PQON (and not the PQON of DTO)
        final Permissionset permissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.BACKEND, crudRequest.ret$PQON());

        // If the new status related to in progress, then check the permissions need to work on the change request data
        if (CHANGE_WORK_FLOW_IN_PROGRESS.contains(newStatus) && !permissions.contains(crudRequest.getCrud())) {
            // This will check the permission for CREATE, MERGE and DELETE operation
            throw new T9tException(T9tException.CHANGE_REQUEST_PERMISSION_ERROR, "No permission to perform " + crudRequest.getCrud() + " operation for "
                + crudRequest.ret$PQON());
        }

        // OperationType.APPROVE permission is needed to set the status to APPROVED
        if (newStatus == ChangeWorkFlowStatus.APPROVED && !permissions.contains(OperationType.APPROVE)) {
            throw new T9tException(T9tException.CHANGE_REQUEST_PERMISSION_ERROR, "No permission to approve data change for " + crudRequest.ret$PQON());
        }

        // OperationType.REJECT permission is needed to set the status to REJECTED
        if (newStatus == ChangeWorkFlowStatus.REJECTED && !permissions.contains(OperationType.REJECT)) {
            throw new T9tException(T9tException.CHANGE_REQUEST_PERMISSION_ERROR, "No permission to reject data change for " + crudRequest.ret$PQON());
        }

        // OperationType.ACTIVATE permission is needed to set the status to ACTIVATED
        // also check ACTIVATE permission if separateActivation is false and new status is APPROVED
        boolean activate  = newStatus == ChangeWorkFlowStatus.ACTIVATED || (!separateActivation && newStatus == ChangeWorkFlowStatus.APPROVED);
        if (activate && !permissions.contains(OperationType.ACTIVATE)) {
            throw new T9tException(T9tException.CHANGE_REQUEST_PERMISSION_ERROR, "No permission to activate data change for " + crudRequest.ret$PQON());
        }
    }

    protected void validateStatus(@Nonnull final RequestContext ctx, @Nullable final ChangeWorkFlowConfigDTO config,
        @Nonnull final DataChangeRequestEntity changeRequest, @Nonnull final ChangeWorkFlowStatus newStatus) {
        final ChangeWorkFlowStatus currentStatus = changeRequest.getStatus();

        // validate status transition
        if (!isStatusChangeValid(currentStatus, newStatus)) {
            throw new T9tException(T9tException.INVALID_CHANGE_REQUEST_STATUS, "Cannot change status from " + currentStatus + " to " + newStatus);
        }

        // If enforceFourEyes is true, then approval should be done from other user with permissions
        final boolean enforceFourEyes = config != null && config.getEnforceFourEyes();
        if (ChangeWorkFlowStatus.APPROVED == newStatus && enforceFourEyes && ctx.userId.equals(changeRequest.getUserIdSubmitted())) {
            throw new T9tException(T9tException.INVALID_CHANGE_REQUEST_STATUS, "Approval should be done from other user with permissions");
        }

        // If privateChangeIds is true, then only creator can submit for review (no collaboration mode)
        final boolean privateChangeIds = config != null && config.getPrivateChangeIds();
        if (ChangeWorkFlowStatus.TO_REVIEW == newStatus && privateChangeIds && !ctx.userId.equals(changeRequest.getUserIdCreated())) {
            throw new T9tException(T9tException.INVALID_CHANGE_REQUEST_STATUS, "Change Id is private and only creator can submit for review");
        }
    }

    // Check status based on the status transition defined in the ChangeWorkFlowStatus enum
    protected boolean isStatusChangeValid(@Nonnull final ChangeWorkFlowStatus currentStatus, @Nonnull final ChangeWorkFlowStatus newStatus) {
        switch (currentStatus) {
            case WORK_IN_PROGRESS -> {
                return ChangeWorkFlowStatus.TO_REVIEW == newStatus || ChangeWorkFlowStatus.CONFLICT == newStatus;
            }
            case TO_REVIEW -> {
                return ChangeWorkFlowStatus.APPROVED == newStatus || ChangeWorkFlowStatus.REJECTED == newStatus || ChangeWorkFlowStatus.CONFLICT == newStatus;
            }
            case APPROVED -> {
                return ChangeWorkFlowStatus.ACTIVATED == newStatus || ChangeWorkFlowStatus.REJECTED == newStatus || ChangeWorkFlowStatus.CONFLICT == newStatus;
            }
            case REJECTED, CONFLICT -> {
                return ChangeWorkFlowStatus.WORK_IN_PROGRESS == newStatus;
            }
            default -> {
                return false;
            }
        }
    }

    /** Mark the change request as conflict by setting status to CONFLICT based on the status transition flow defined in ChangeWorkFlowStatus enum */
    protected void markConflict(@Nonnull final DataChangeRequestEntity changeRequest) {
        final Query query = resolver.getEntityManager().createQuery(SQL_UPDATE_CHANGE_REQUEST);
        query.setParameter("status", ChangeWorkFlowStatus.CONFLICT.getToken());
        query.setParameter("objectRef", changeRequest.getObjectRef());
        query.setParameter("excludeStatus", List.of(ChangeWorkFlowStatus.CONFLICT.getToken(), ChangeWorkFlowStatus.ACTIVATED.getToken(),
            ChangeWorkFlowStatus.REJECTED.getToken()));
        query.setParameter("pqon", changeRequest.getPqon());
        query.setParameter("key", CompactByteArrayComposer.marshal(DataChangeRequestDTO.meta$$key, changeRequest.getKey(), false));
        query.executeUpdate();
    }

    protected CrudAnyKeyRequest<?, ?> getCrudRequest(@Nonnull final DataChangeRequestEntity changeRequest) {
        if (changeRequest.getCrudRequest() instanceof CrudAnyKeyRequest<?, ?> crudAnyKeyRequest) {
            return crudAnyKeyRequest;
        }
        LOGGER.error("Invalid crud request {}, for PQON:{} and ref:{}", changeRequest.getCrudRequest(), changeRequest.getPqon(), changeRequest.getObjectRef());
        throw new T9tException(T9tException.INVALID_CHANGE_REQUEST, "Invalid crud request!");
    }
}
