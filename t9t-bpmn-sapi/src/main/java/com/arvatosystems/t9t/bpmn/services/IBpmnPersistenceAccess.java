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
package com.arvatosystems.t9t.bpmn.services;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionRef;
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefRequest;

import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface IBpmnPersistenceAccess {

    void save(ProcessDefinitionDTO dto);

    /** Reads a process definition using the usual fallback rule to default tenant. Throws an Exception if none exists. */
    ProcessDefinitionDTO getProcessDefinitionDTO(String processDefinitionId);

    /** Reads a process definition using the usual fallback rule for some general reference. Throws an Exception if none exists. */
    ProcessDefinitionDTO getProcessDefinitionDTO(ProcessDefinitionRef ref);

    /** Reads all process definitions. */
    List<DataWithTrackingS<ProcessDefinitionDTO, FullTrackingWithVersion>> getAllProcessDefinitionsForEngine(String engine);

    /** Stores an initial status when a new task is submitted. */
    Long persistNewStatus(ProcessExecutionStatusDTO dto);

    /** Retrieves all process status records of current tasks (unused). */
    List<ProcessExecutionStatusDTO> getTasksDue(@Nullable String onlyForProcessDefinitionId, @Nonnull Instant whenDue,
        boolean includeErrorStatus, boolean allClusterNodes,
        @Nullable String onlyForNextStep, @Nullable Collection<Integer> returnCodes);

    /** Retrieves all references to process status records of current tasks. */
    List<Long> getTaskRefsDue(@Nullable String onlyForProcessDefinitionId, @Nonnull Instant whenDue,
        boolean includeErrorStatus, boolean allClusterNodes,
        @Nullable String onlyForNextStep, @Nullable Collection<Integer> returnCodes, @Nullable Integer maxTasks, boolean ignoreDueDate);

    /** Update existing process execution status */
    Long createOrUpdateNewStatus(RequestContext ctx, ProcessExecutionStatusDTO dto, ExecuteProcessWithRefRequest rq, boolean restart);

    /** Find existing process execution status, or throw an exception, if none exists. */
    ProcessExecutionStatusDTO getProcessExecutionStatusDTO(String processDefinitionId, Long targetObjectRef);

    /** Find existing process execution status, or return null if non existing. */
    ProcessExecutionStatusDTO getProcessExecutionStatusOpt(String processDefinitionId, Long targetObjectRef);

    /**
     * Checks whether an existing workflow status record for the given workflow and data object exists.
     * Returns true if found, false if not.
     *
     * In addition, dependent on server configuration settings, sets the "yieldUntil" setting to null if
     * ServerConfiguration.updateYieldUntilIfFarFuture is set to true and the existing value is FAR_FUTURE,
     * or if ServerConfiguration.updateYieldUntil is set and the existing value is anywhere in the future.
     *
     * The synchronous update costs performance, but is recommended because in case of an asynchronous restart of the workflow,
     * the retrigger event could be lost in case of a server restart.
     *
     * @param ctx the current context
     * @param ref the object ref of the workflow's data object
     * @param workflowId the ID of the workflow
     * @return true if the entry existed, false if not
     */
    boolean removeWaitFlag(@Nonnull RequestContext ctx, @Nonnull Long ref, @Nonnull String workflowId);
}
