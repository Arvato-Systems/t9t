package com.arvatosystems.t9t.changeRequest.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowStatus;
import com.arvatosystems.t9t.changeRequest.jpa.entities.DataChangeRequestEntity;
import com.arvatosystems.t9t.changeRequest.jpa.persistence.IDataChangeRequestEntityResolver;
import com.arvatosystems.t9t.changeRequest.request.DeleteDataChangeRequestRecordRequest;
import com.arvatosystems.t9t.changeRequest.services.IChangeWorkFlowConfigCache;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteDataChangeRequestRecordRequestHandler extends AbstractRequestHandler<DeleteDataChangeRequestRecordRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDataChangeRequestRecordRequestHandler.class);

    protected final IDataChangeRequestEntityResolver resolver = Jdp.getRequired(IDataChangeRequestEntityResolver.class);
    protected final IChangeWorkFlowConfigCache configCache = Jdp.getRequired(IChangeWorkFlowConfigCache.class);

    @Nonnull
    @Override
    public ServiceResponse execute(@Nonnull final RequestContext ctx, @Nonnull final DeleteDataChangeRequestRecordRequest deleteRequest) throws Exception {
        LOGGER.debug("Deleting data change request for ref: {}", deleteRequest.getDataChangeRequestRef());
        final DataChangeRequestEntity changeRequest = resolver.getEntityData(deleteRequest.getDataChangeRequestRef());
        final ChangeWorkFlowConfigDTO config = configCache.getOrNull(changeRequest.getPqon());

        // Change request cannot be deleted if it is activated
        if (ChangeWorkFlowStatus.ACTIVATED == changeRequest.getStatus()) {
            throw new T9tException(T9tException.CHANGE_REQUEST_FINALIZED, "Change request is activated and cannot be deleted!");
        }

        // If privateChangeIds is set to true, only the creator can delete the change request
        final boolean privateChangeIds = config != null && config.getPrivateChangeIds();
        if (privateChangeIds && !changeRequest.getUserIdCreated().equals(ctx.userId)) {
            throw new T9tException(T9tException.CHANGE_REQUEST_PERMISSION_ERROR, "Change request is private and can only be deleted by the creator!");
        }

        resolver.remove(changeRequest);
        return ok();
    }
}
