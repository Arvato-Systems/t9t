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
package com.arvatosystems.t9t.bpmn.be.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.services.IBpmApplicationService;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefRequest;
import com.arvatosystems.t9t.bpmn.request.WorkflowActionEnum;
import com.arvatosystems.t9t.bpmn.services.IBpmnPersistenceAccess;
import com.arvatosystems.t9t.bpmn.services.IProcessDefinitionCache;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;

@Singleton
public class BpmApplicationService implements IBpmApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BpmApplicationService.class);

    protected final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);
    protected final IBpmnPersistenceAccess persistenceAccess = Jdp.getRequired(IBpmnPersistenceAccess.class);
    protected final IProcessDefinitionCache pdCache = Jdp.getRequired(IProcessDefinitionCache.class);

    /** Access to constant moved into a function in order to allow overriding. */
    @Override
    public int getPartitionCount() {
        return T9tConstants.DEFAULT_KAFKA_PARTITION_COUNT_SMALL;
    }

    /**
     * Returns the shard / node which is most suitable to run the workflow on.
     * This implementation ensures that all workflows for a given customer are run on the same node, such that JVM locking works best.
     **/
    @Override
    public Integer getNode(final String partitionKey) {
        if (partitionKey == null) {
            return null;
        }
        final int hash = partitionKey.hashCode() & Integer.MAX_VALUE;  // force it to be positive
        return Integer.valueOf(hash % getPartitionCount());
    }

    @Override
    public void startBusinessProcess(final RequestContext ctx, final String workflowId,
      final Long targetObjectRef, final Long refToLock, final String partitionKey) {
        LOGGER.debug("Starting NEW business process for {}:{}", workflowId, targetObjectRef);
        final ExecuteProcessWithRefRequest bpmRefRequest = new ExecuteProcessWithRefRequest();
        bpmRefRequest.setTargetObjectRef(targetObjectRef);
        bpmRefRequest.setProcessDefinitionId(workflowId);
        bpmRefRequest.setIfEntryExists(WorkflowActionEnum.ERROR);  // should use restart in that case...
        bpmRefRequest.setRunOnNode(getNode(partitionKey));
        bpmRefRequest.setLockRef(refToLock);
        bpmRefRequest.setLockId(partitionKey);

        // this just creates the entry in the process execution table, and then launches the process asynchronously
        // 1.) validate that the processId exists
        final ProcessDefinitionDTO pd = pdCache.getCachedProcessDefinitionDTO(ctx.tenantId, workflowId);

        // 2.) create or validate the entry in the status table
        final ProcessExecutionStatusDTO newStatus = new ProcessExecutionStatusDTO();
        newStatus.setProcessDefinitionId(workflowId);
        newStatus.setTargetObjectRef(targetObjectRef);
        newStatus.setCurrentParameters(pd.getInitialParams());
        newStatus.setYieldUntil(ctx.executionStart);
        newStatus.setRunOnNode(getNode(partitionKey));
        newStatus.setLockRef(refToLock);
        newStatus.setLockId(partitionKey);

        persistenceAccess.createOrUpdateNewStatus(ctx, newStatus, bpmRefRequest, pd.getAlwaysRestartAtStep1());
    }

    @Override
    public void startBusinessProcess(final String workflowId, final Long objectRef, final Long refToLock, final String partitionKey) {
        startBusinessProcess(ctxProvider.get(), workflowId, objectRef, refToLock, partitionKey);
    }

    @Override
    public void continueExistingBusinessProcess(final Long ref, final String workflowId) {
        LOGGER.debug("Continue business process for {}:{}", workflowId, ref);
        startExistingBusinessProcess(ref, workflowId, false, WorkflowActionEnum.NO_ACTIVITY, null);
    }

    @Override
    public void continueExistingBusinessProcess(final Long ref, final String workflowId, final boolean createNewWf, final String workflowStep) {
        LOGGER.debug("Continue business process for {}:{} at step {}", workflowId, ref, workflowStep);
        startExistingBusinessProcess(ref, workflowId, false, createNewWf ? null : WorkflowActionEnum.NO_ACTIVITY, workflowStep);
    }

    @Override
    public void restartExistingBusinessProcess(final Long ref, final String workflowId) {
        LOGGER.debug("Restarting business process for {}:{}", workflowId, ref);
        startExistingBusinessProcess(ref, workflowId, true, WorkflowActionEnum.NO_ACTIVITY, null);
    }

    @Override
    public void restartOrContinueExistingBusinessProcess(final Long ref, final String workflowId) {
        LOGGER.debug("Restarting OR continuing business process for {}:{}", workflowId, ref);
        startExistingBusinessProcess(ref, workflowId, null, WorkflowActionEnum.NO_ACTIVITY, null);
    }

    /**
     * Common execution unit to restart or continue an existing business process.
     * This must be performed asynchronously, because we could encounter deadlocks otherwise.
     *
     * @param ref
     * @param workflowId
     * @param restartAtBeginning
     * @param workflowActionEnum
     * @param workflowStep
     */
    private void startExistingBusinessProcess(final Long ref, final String workflowId, final Boolean restartAtBeginning,
            final WorkflowActionEnum workflowActionEnum, final String workflowStep) {
        final RequestContext ctx = ctxProvider.get();
        final boolean workflowExisted = persistenceAccess.removeWaitFlag(ctx, ref, workflowId);
        if (!workflowExisted && workflowActionEnum == WorkflowActionEnum.NO_ACTIVITY) {
            // no need to trigger the asynchronous process, because no entry was there
            return;
        }
        final ExecuteProcessWithRefRequest bpmRefRequest = new ExecuteProcessWithRefRequest();
        bpmRefRequest.setTargetObjectRef(ref);
        bpmRefRequest.setProcessDefinitionId(workflowId);
        bpmRefRequest.setIfNoEntryExists(workflowActionEnum);
        bpmRefRequest.setRestartAtBeginningIfExists(restartAtBeginning);
        bpmRefRequest.setIfEntryExists(WorkflowActionEnum.RUN);
        bpmRefRequest.setWorkflowStep(workflowStep);
        executor.executeAsynchronous(ctx, bpmRefRequest);
    }
}
