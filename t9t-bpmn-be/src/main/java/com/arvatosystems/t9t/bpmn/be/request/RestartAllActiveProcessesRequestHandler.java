/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn.be.request;

import java.util.List;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.request.RestartAllActiveProcessesRequest;
import com.arvatosystems.t9t.bpmn.request.TriggerSingleProcessNowRequest;
import com.arvatosystems.t9t.bpmn.services.IBpmnPersistenceAccess;
import com.arvatosystems.t9t.statistics.services.IAutonomousRunner;

import de.jpaw.dp.Jdp;

public class RestartAllActiveProcessesRequestHandler extends AbstractRequestHandler<RestartAllActiveProcessesRequest> {
    protected static final Logger LOGGER  = LoggerFactory.getLogger(RestartAllActiveProcessesRequestHandler.class);
    protected final IAutonomousRunner runner = Jdp.getRequired(IAutonomousRunner.class);
    protected final IBpmnPersistenceAccess persistenceAccess = Jdp.getRequired(IBpmnPersistenceAccess.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, RestartAllActiveProcessesRequest rq) {
        final String displayId = rq.getOnlyThisProcessId() == null ? "*" : rq.getOnlyThisProcessId();
        ctx.statusText = "Querying refs to process for " + displayId;
        Instant dueWhen = rq.getMinAgeInSeconds() == null ? ctx.executionStart : ctx.executionStart.minus(1000L * rq.getMinAgeInSeconds());
        List<Long> taskRefs = persistenceAccess.getTaskRefsDue(rq.getOnlyThisProcessId(), dueWhen, Boolean.TRUE.equals(rq.getIncludeErrorStatus()));

        final int numRecords = taskRefs.size();
        LOGGER.debug("Found {} active tasks for workflow {} for restart", numRecords, displayId);
        ctx.statusText = "Processing " + numRecords + " tasks for " + displayId;

        runner.runSingleAutonomousTx(ctx, numRecords, taskRefs,
                ref -> new TriggerSingleProcessNowRequest(ref),
                stat -> stat.setInfo1(rq.getOnlyThisProcessId()),
                "t9t-bpm");
        return ok();
    }
}
