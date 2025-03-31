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
package com.arvatosystems.t9t.bpmn2.be.camunda.jobExecutor;

import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.BPMNExtensionHelper.getAllProperties;
import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Runnable to execute given job id, called by BPMN engine. It will group the given job ids by their tenant and pass
 * them to the {@link JobExecutionRequestWrapper} to perform execution within a T9T request context.
 *
 * @author TWEL006
 */
public class T9tExecuteJobsRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(T9tExecuteJobsRunnable.class);

    private final List<String> jobIdsToExecute;
    private final ProcessEngine processEngine;
    private final JobExecutionRequestWrapper jobExecutionRequestWrapper;

    /** Property: APIKey on process */
    public static final String PROPERTY_API_KEY = "APIKey";

    /** Property: APIKey on process */
    public static final String PROPERTY_WORKFLOW_TYPE = "WorkflowType";

    public T9tExecuteJobsRunnable(List<String> jobIdsToExecute, ProcessEngine processEngine, JobExecutionRequestWrapper jobExecutionRequestWrapper) {
        this.jobIdsToExecute = jobIdsToExecute;
        this.processEngine = processEngine;
        this.jobExecutionRequestWrapper = jobExecutionRequestWrapper;
    }

    @Override
    public void run() {
        MDC.clear();
        LOGGER.debug("Try to execute {} jobs", jobIdsToExecute.size());

        if (jobIdsToExecute.isEmpty()) {
            return;
        }

        final ManagementService managementService = processEngine.getManagementService();

        for (String jobId : jobIdsToExecute) {
            final Job job = managementService.createJobQuery()
                                             .jobId(jobId)
                                             .singleResult();

            final String tenantId = job.getTenantId();
            final Map<String, String> processDefinitionProperties = getProcessDefinitionPropertiesOfJob(job);
            final UUID apiKey = getApiKey(processDefinitionProperties);
            final String workflowTypeString = processDefinitionProperties.get(PROPERTY_WORKFLOW_TYPE);


            jobExecutionRequestWrapper.executeJob(jobId, job.getExecutionId(), workflowTypeString, tenantId, apiKey);
        }
    }

    /**
     * Get all properties attached to process within process definition of given job.
     *
     * @param job
     *            Job
     *
     * @return Possible empty map of properties attached to process
     */
    private Map<String, String> getProcessDefinitionPropertiesOfJob(Job job) {
        final RepositoryService repositoryService = processEngine.getRepositoryService();

        // We have to navigate the BPMN model, which can be NULL at any point, thus using Optional
        return Optional.of(job.getProcessDefinitionId())
                       // First get BPMN Model
                       .map(processDefinitionId -> repositoryService.getBpmnModelInstance(processDefinitionId))
                       // Get process element of model
                       .map(model -> model.getModelElementById(job.getProcessDefinitionKey()))
                       .filter(element -> element instanceof Process)
                       // Now search all CamundaProperties
                       .map(process -> getAllProperties((Process) process))
                       .orElse(emptyMap());

    }

    /**
     * Get API key, which is defined on process definition level.
     *
     * @param properties
     *            Properties to retrieve API key from
     *
     * @return API key or NULL if none is configured
     */
    private UUID getApiKey(Map<String, String> properties) {
        final String apiKeyString = properties.get(PROPERTY_API_KEY);

        if (apiKeyString == null) {
            return null;
        } else {
            return UUID.fromString(apiKeyString);
        }
    }

}
