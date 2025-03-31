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
package com.arvatosystems.t9t.bpmn.be.request;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsDTO;
import com.arvatosystems.t9t.bpmn.request.RestartSpecificActiveProcessesRequest;
import com.arvatosystems.t9t.bpmn.request.TriggerSingleProcessNowRequest;
import com.arvatosystems.t9t.bpmn.services.IBpmnPersistenceAccess;
import com.arvatosystems.t9t.statistics.services.IAutonomousRunner;
import com.arvatosystems.t9t.statistics.services.IStatisticsService;

import de.jpaw.dp.Jdp;

public class RestartSpecificActiveProcessesRequestHandler extends AbstractRequestHandler<RestartSpecificActiveProcessesRequest> {
    private static final Logger LOGGER  = LoggerFactory.getLogger(RestartSpecificActiveProcessesRequestHandler.class);

    private final IBpmnPersistenceAccess persistenceAccess = Jdp.getRequired(IBpmnPersistenceAccess.class);
    private final IAutonomousRunner      runner            = Jdp.getRequired(IAutonomousRunner.class);
    private final IStatisticsService     statisticsService = Jdp.getRequired(IStatisticsService.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final RestartSpecificActiveProcessesRequest rq) {
        final String processId = rq.getProcessId();
        final Instant dueWhen = ctx.executionStart.minusSeconds(T9tUtil.nvl(rq.getMinAgeInSeconds(), 30));
        final List<Long> taskRefs = persistenceAccess.getTaskRefsDue(processId, dueWhen,
            Boolean.TRUE.equals(rq.getIncludeErrorStatus()), Boolean.TRUE.equals(rq.getRunProcessesOfAnyNode()), rq.getNextStep(), rq.getReturnCodes(),
            rq.getMaxTasks());

        final int numRecords = taskRefs.size();
        LOGGER.debug("Found {} active tasks for workflow {} at step {} for restart", numRecords, processId, rq.getNextStep());

        if (numRecords > 0) {
            ctx.statusText = "Processing " + numRecords + " tasks for " + processId;

            runner.runSingleAutonomousTx(ctx, numRecords, taskRefs,
                    ref -> createTriggerRequest(ref),
                    stat -> stat.setInfo1(processId),
                    "t9t-bpm-s");
            writeStatistics(ctx, numRecords, processId, rq.getNextStep());
        }
        return ok();
    }

    private TriggerSingleProcessNowRequest createTriggerRequest(final Long ref) {
        final TriggerSingleProcessNowRequest rq = new TriggerSingleProcessNowRequest(ref);
        rq.setEssentialKey(Long.toString(ref));
        return rq;
    }

    private void writeStatistics(final RequestContext ctx, final int numRecords, final String workflowId, final String stepToRestart) {
        final StatisticsDTO stat = new StatisticsDTO();
        stat.setInfo1(workflowId);
        stat.setInfo2(stepToRestart);
        stat.setRecordsProcessed(numRecords);
        stat.setStartTime(ctx.executionStart);
        stat.setEndTime(Instant.now());
        stat.setJobRef(ctx.internalHeaderParameters.getProcessRef());
        stat.setProcessId("restartSomeBpm");
        statisticsService.saveStatisticsData(stat);
    }
}
