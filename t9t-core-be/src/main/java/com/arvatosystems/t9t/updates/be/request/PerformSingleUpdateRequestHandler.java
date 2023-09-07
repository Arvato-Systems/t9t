/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.updates.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.T9tCoreException;
import com.arvatosystems.t9t.updates.UpdateApplyStatusType;
import com.arvatosystems.t9t.updates.request.FinishUpdateRequest;
import com.arvatosystems.t9t.updates.request.PerformSingleUpdateRequest;
import com.arvatosystems.t9t.updates.request.StartUpdateRequest;
import com.arvatosystems.t9t.updates.services.IFeatureUpdate;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public class PerformSingleUpdateRequestHandler extends AbstractRequestHandler<PerformSingleUpdateRequest> {

    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);
    protected final IAutonomousExecutor autonomousExecutor = Jdp.getRequired(IAutonomousExecutor.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final PerformSingleUpdateRequest request) throws Exception {
        final IFeatureUpdate updateHandler = Jdp.getOptional(IFeatureUpdate.class, request.getTicketId());
        if (updateHandler == null) {
            throw new T9tException(T9tCoreException.UPDATE_MISSING_IMPLEMENTATION, request.getTicketId());
        }
        startUpdate(ctx, updateHandler);

        int returnCode = 0;
        Exception ex = null;
        try {
            returnCode = updateHandler.performUpdate(ctx, executor);
        } catch (final ApplicationException ae) {
            ex = ae;
            returnCode = ae.getErrorCode();
        } catch (final Exception e) {
            ex = e;
            returnCode = T9tException.GENERAL_EXCEPTION;
        }
        if (ApplicationException.isOk(returnCode)) {
            finishUpdate(ctx, updateHandler);
            return ok();
        } else {
            failUpdate(ctx, updateHandler, returnCode);
            if (ex != null) {
                // reuse existing exception
                throw ex;
            } else {
                // create a new exception
                throw new T9tException(returnCode);
            }
        }
    }

    private void startUpdate(final RequestContext ctx, final IFeatureUpdate updateHandler) {
        final StartUpdateRequest startRq = new StartUpdateRequest();
        startRq.setTicketId(updateHandler.getTicketId());
        startRq.setApplySequenceId(updateHandler.getApplySequenceId());
        startRq.setDescription(updateHandler.getDescription());
        startRq.setAllowRestartOfPending(updateHandler.getAllowRestartOfPending());
        startRq.setInitialStatus(UpdateApplyStatusType.IN_PROGRESS);
        autonomousExecutor.executeAndCheckResult(ctx, startRq, ServiceResponse.class);
    }

    private void finishUpdate(final RequestContext ctx, final IFeatureUpdate updateHandler) {
        final FinishUpdateRequest finishRq = new FinishUpdateRequest();
        finishRq.setTicketId(updateHandler.getTicketId());
        autonomousExecutor.executeAndCheckResult(ctx, finishRq, ServiceResponse.class);
    }

    private void failUpdate(final RequestContext ctx, final IFeatureUpdate updateHandler, final int returnCode) {
        final FinishUpdateRequest finishRq = new FinishUpdateRequest();
        finishRq.setTicketId(updateHandler.getTicketId());
        finishRq.setNewStatus(UpdateApplyStatusType.ERROR);
        finishRq.setErrorCode(returnCode);
        autonomousExecutor.executeAndCheckResult(ctx, finishRq, ServiceResponse.class);
    }
}
