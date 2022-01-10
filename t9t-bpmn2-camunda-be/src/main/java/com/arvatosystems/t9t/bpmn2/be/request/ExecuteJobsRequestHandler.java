/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn2.be.request;

import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.IdentifierConverter.t9tTenantRefToBPMNTenantId;

import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.request.ExecuteJobsRequest;
import com.arvatosystems.t9t.bpmn2.request.ExecuteJobsResponse;

import de.jpaw.dp.Jdp;

public class ExecuteJobsRequestHandler extends AbstractBPMNRequestHandler<ExecuteJobsRequest> {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExecuteJobsRequestHandler.class);

    private final ManagementService managementService = Jdp.getRequired(ManagementService.class);
    private final RuntimeService runtimeService = Jdp.getRequired(RuntimeService.class);

    @Override
    protected ServiceResponse executeInWorkflowContext(RequestContext requestContext, ExecuteJobsRequest request) throws Exception {
        requestContext.statusText = "Selecting jobs";

        JobQuery query = managementService.createJobQuery()
                                          .tenantIdIn(t9tTenantRefToBPMNTenantId(requestContext.getTenantRef()));

        if (request.getProcessDefinitionKey() != null) {
            query = query.processDefinitionKey(request.getProcessDefinitionKey());
        }

        if (request.getProcessInstanceId() != null) {
            query = query.processInstanceId(request.getProcessInstanceId());
        }

        if (request.getRunFailedJobs()) {
            query = query.active()
                         .noRetriesLeft();
        } else {
            query = query.executable();
        }

        final List<Job> jobs = query.orderByProcessInstanceId()
                                    .asc()
                                    .orderByJobPriority()
                                    .asc()
                                    .orderByJobDuedate()
                                    .asc()
                                    .list();

        LOGGER.debug("Found {} jobs for execution", jobs.size());
        requestContext.statusText = "Execution of " + jobs.size() + " jobs";

        final ExecuteJobsResponse result = new ExecuteJobsResponse();
        result.setJobsFailedNoRetriesLeft(0);
        result.setJobsFailedRetriesLeft(0);
        result.setJobsSuccessfull(0);

        for (Job job : jobs) {
            try {

                LOGGER.debug("execute job {} of process instance id {} (process definition id {})", job.getId(), job.getProcessInstanceId(),
                        job.getProcessDefinitionKey());
                managementService.executeJob(job.getId());
                result.setJobsSuccessfull(result.getJobsSuccessfull() + 1);

            } catch (RuntimeException e) {
                LOGGER.error("Error executing job " + job.getId(), e);

                if (job.getRetries() - 1 > 0) {
                    managementService.setJobRetries(job.getId(), job.getRetries() - 1);
                    result.setJobsFailedRetriesLeft(result.getJobsFailedRetriesLeft() + 1);
                } else {
                    managementService.setJobRetries(job.getId(), 0);
                    result.setJobsFailedNoRetriesLeft(result.getJobsFailedNoRetriesLeft() + 1);

                    runtimeService.createIncident(Incident.FAILED_JOB_HANDLER_TYPE, job.getExecutionId(), job.getId(), e.getMessage());
                }
            }

            requestContext.incrementProgress();
        }

        if (result.getJobsFailedNoRetriesLeft() > 0 || result.getJobsFailedRetriesLeft() > 0) {
            result.setReturnCode(1);
        } else {
            result.setReturnCode(0);
        }

        return result;
    }

}
