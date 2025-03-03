package com.arvatosystems.t9t.changeRequest.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowStatus;
import com.arvatosystems.t9t.changeRequest.jpa.entities.DataChangeRequestEntity;
import com.arvatosystems.t9t.changeRequest.jpa.persistence.IDataChangeRequestEntityResolver;
import com.arvatosystems.t9t.changeRequest.request.ModifyDataChangeRequestDataRequest;
import com.arvatosystems.t9t.changeRequest.services.IChangeWorkFlowConfigCache;
import com.arvatosystems.t9t.server.services.IAuthorize;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModifyDataChangeRequestDataRequestHandler extends AbstractRequestHandler<ModifyDataChangeRequestDataRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyDataChangeRequestDataRequestHandler.class);

    protected final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);
    protected final IDataChangeRequestEntityResolver resolver = Jdp.getRequired(IDataChangeRequestEntityResolver.class);
    protected final IChangeWorkFlowConfigCache configCache = Jdp.getRequired(IChangeWorkFlowConfigCache.class);

    @Nonnull
    @Override
    public ServiceResponse execute(@Nonnull final RequestContext ctx, @Nonnull final ModifyDataChangeRequestDataRequest modifyRequest) throws Exception {
        LOGGER.debug("Modifying data change request for ref: {}", modifyRequest.getDataChangeRequestRef());
        final DataChangeRequestEntity changeRequest = resolver.getEntityData(modifyRequest.getDataChangeRequestRef());
        final ChangeWorkFlowConfigDTO config = configCache.getOrNull(changeRequest.getPqon());
        permissionCheck(ctx, changeRequest);

        // No changes can be done after the change request is activated
        if (changeRequest.getStatus() == ChangeWorkFlowStatus.ACTIVATED) {
            throw new T9tException(T9tException.CHANGE_REQUEST_FINALIZED, "Change request is already activated!");
        }

        // If privateChangeIds is set to true, only the creator can modify the change request
        final boolean privateChangeIds = config != null && config.getPrivateChangeIds();
        if (privateChangeIds && !changeRequest.getUserIdCreated().equals(ctx.userId)) {
            throw new T9tException(T9tException.CHANGE_REQUEST_PERMISSION_ERROR, "Change request is private and can only be modified by the creator!");
        }

        modifyData(changeRequest, modifyRequest.getData());
        // Reset the status to WORK_IN_PROGRESS
        changeRequest.setStatus(ChangeWorkFlowStatus.WORK_IN_PROGRESS);
        changeRequest.setUserIdModified(ctx.userId);
        changeRequest.setWhenLastModified(ctx.executionStart);
        // Reset all the fields that are set during the approval process
        changeRequest.setUserIdSubmitted(null);
        changeRequest.setWhenSubmitted(null);
        changeRequest.setTextSubmitted(null);
        changeRequest.setUserIdApprove(null);
        changeRequest.setWhenDecided(null);
        changeRequest.setTextDecision(null);

        return ok();
    }

    @SuppressWarnings("all")
    protected void permissionCheck(@Nonnull final RequestContext ctx, final DataChangeRequestEntity changeRequest) {
        // Check permission based on CRUD request PQON (and not the PQON of DTO)
        final Permissionset permissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.BACKEND,
            changeRequest.getCrudRequest().ret$PQON());
        if (changeRequest.getCrudRequest() instanceof CrudAnyKeyRequest crudAnyKeyRequest) {
            if (!permissions.contains(crudAnyKeyRequest.getCrud())) {
                throw new T9tException(T9tException.CHANGE_REQUEST_PERMISSION_ERROR, "No permission to modify data for " + changeRequest.getPqon());
            }
        } else {
            LOGGER.error("Invalid crud request {}, for PQON:{} and ref:{}", changeRequest.getCrudRequest(), changeRequest.getPqon(),
                    changeRequest.getObjectRef());
            throw new T9tException(T9tException.INVALID_CHANGE_REQUEST, "Invalid crud request!");
        }
    }

    @SuppressWarnings("all")
    protected void modifyData(@Nonnull final DataChangeRequestEntity changeRequest, @Nonnull final BonaPortable newData) {
        // Replace the data in the CRUD request with data received in the modify request, only if the PQON matches
        if (changeRequest.getCrudRequest() instanceof CrudAnyKeyRequest crudAnyKeyRequest) {
            crudAnyKeyRequest.setData(newData);
            changeRequest.setCrudRequest(crudAnyKeyRequest);
        } else {
            LOGGER.error("Invalid crud request {}, for PQON:{} and ref:{}", changeRequest.getCrudRequest(), changeRequest.getPqon(),
                changeRequest.getObjectRef());
            throw new T9tException(T9tException.INVALID_CHANGE_REQUEST, "Invalid crud request!");
        }
    }
}
