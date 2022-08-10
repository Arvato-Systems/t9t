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
package com.arvatosystems.t9t.bpmn.services;

import java.time.Instant;
import java.util.List;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionRef;
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefRequest;

import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;

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

    /** Retrieves all processDefinitions of current tasks. */
    List<ProcessExecutionStatusDTO> getTasksDue(String onlyForProcessDefinitionId, Instant whenDue, boolean includeErrorStatus, boolean allClusterNodes);

    /** Retrieves all processDefinitions of current tasks. */
    List<Long> getTaskRefsDue(String onlyForProcessDefinitionId, Instant whenDue, boolean includeErrorStatus, boolean allClusterNodes);

    /** Update existing process execution status */
    Long createOrUpdateNewStatus(RequestContext ctx, ProcessExecutionStatusDTO dto, ExecuteProcessWithRefRequest rq);

    /** Find existing process execution status, or throw an exception, if none exists. */
    ProcessExecutionStatusDTO getProcessExecutionStatusDTO(String processDefinitionId, Long targetObjectRef);

    /** Find existing process execution status, or return null if non existing. */
    ProcessExecutionStatusDTO getProcessExecutionStatusOpt(String processDefinitionId, Long targetObjectRef);
}
