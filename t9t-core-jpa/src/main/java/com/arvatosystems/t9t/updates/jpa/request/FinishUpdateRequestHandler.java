package com.arvatosystems.t9t.updates.jpa.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.T9tCoreException;
import com.arvatosystems.t9t.updates.UpdateApplyStatusType;
import com.arvatosystems.t9t.updates.UpdateStatusTicketKey;
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusEntity;
import com.arvatosystems.t9t.updates.jpa.persistence.IUpdateStatusEntityResolver;
import com.arvatosystems.t9t.updates.request.FinishUpdateRequest;
import com.arvatosystems.t9t.updates.service.IUpdateStatusService;
import de.jpaw.dp.Jdp;

public class FinishUpdateRequestHandler extends AbstractRequestHandler<FinishUpdateRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FinishUpdateRequestHandler.class);

    protected final IUpdateStatusEntityResolver resolver = Jdp.getRequired(IUpdateStatusEntityResolver.class);
    protected final IUpdateStatusService updateStatusService = Jdp.getRequired(IUpdateStatusService.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final FinishUpdateRequest request) {
        final String ticketId = request.getTicketId();
        final UpdateStatusEntity updateStatus = resolver.getEntityData(new UpdateStatusTicketKey(ticketId), true);
        if (UpdateApplyStatusType.IN_PROGRESS != updateStatus.getUpdateApplyStatus()) {
            LOGGER.error("UpdateApplyStatus of the ticket {} must be in progress.", ticketId);
            throw new T9tException(T9tCoreException.FINISH_UPDATE_MUST_BE_IN_PROGRESS,
                    "Ticket status is " + updateStatus.getUpdateApplyStatus() + " but not "
                            + UpdateApplyStatusType.IN_PROGRESS + ".");
        }
        updateStatus.setUpdateApplyStatus(UpdateApplyStatusType.COMPLETE);
        updateStatusService.logUpdateStatus(updateStatus);
        return ok();
    }
}
