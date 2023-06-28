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
package com.arvatosystems.t9t.bpmn2.be.camunda.request;

import static java.util.Arrays.asList;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.IBPMNInitWorkflowCallback;
import com.arvatosystems.t9t.bpmn2.be.camunda.utils.BPMNExecutionReadOnlyContext;
import com.arvatosystems.t9t.bpmn2.be.request.AbstractBPMNRequestHandler;
import com.arvatosystems.t9t.bpmn2.camunda.request.JobExecutorWorkerRequest;

import de.jpaw.dp.Jdp;

public class JobExecutorWorkerRequestHandler extends AbstractBPMNRequestHandler<JobExecutorWorkerRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobExecutorWorkerRequestHandler.class);

    private final ProcessEngineImpl processEngine = (ProcessEngineImpl) Jdp.getRequired(ProcessEngine.class);

    @Override
    protected ServiceResponse executeInWorkflowContext(RequestContext requestContext, JobExecutorWorkerRequest request) throws Exception {

        lockRefs(request.getWorkflowType(), request.getExecutionId());

        new ExecuteJobsRunnable(asList(request.getJobId()), processEngine).run();

        return ok();
    }

    /**
     * Perform lock for job execution.
     */
    private void lockRefs(String workflowType, String executionId) {
        final RuntimeService runtimeService = processEngine.getRuntimeService();

        if (workflowType != null && executionId != null) {
            LOGGER.debug("Get lock refs for workflow with type '{}'", workflowType);
            final IBPMNInitWorkflowCallback initCallback = Jdp.getRequired(IBPMNInitWorkflowCallback.class, workflowType);

            initCallback.lockRefs(new BPMNExecutionReadOnlyContext(runtimeService, executionId));
        }
    }

}
