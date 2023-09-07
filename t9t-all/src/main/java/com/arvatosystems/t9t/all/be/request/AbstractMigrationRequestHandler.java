package com.arvatosystems.t9t.all.be.request;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.updates.request.FinishUpdateRequest;
import com.arvatosystems.t9t.updates.request.StartUpdateRequest;

import de.jpaw.dp.Jdp;

public abstract class AbstractMigrationRequestHandler<R extends RequestParameters> extends AbstractRequestHandler<R> {
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    protected void startUpdate(final RequestContext ctx, final String ticketId, final String sequenceId, final boolean allowRestart, final String description) {
        final StartUpdateRequest startUpdateRequest = new StartUpdateRequest();
        startUpdateRequest.setTicketId(ticketId);
        startUpdateRequest.setDescription(description);
        startUpdateRequest.setApplySequenceId(sequenceId);
        startUpdateRequest.setAllowRestartOfPending(allowRestart);
        executor.executeSynchronousAndCheckResult(ctx, startUpdateRequest, ServiceResponse.class);
    }

    protected void finishUpdate(final RequestContext ctx, final String ticketId) {
        final FinishUpdateRequest finishUpdateRequest = new FinishUpdateRequest(ticketId);
        executor.executeSynchronousAndCheckResult(ctx, finishUpdateRequest, ServiceResponse.class);
    }
}
