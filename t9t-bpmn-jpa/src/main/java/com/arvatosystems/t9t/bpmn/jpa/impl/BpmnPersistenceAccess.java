/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn.jpa.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionRef;
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.bpmn.T9tBPMException;
import com.arvatosystems.t9t.bpmn.jpa.entities.ProcessDefinitionEntity;
import com.arvatosystems.t9t.bpmn.jpa.entities.ProcessExecStatusEntity;
import com.arvatosystems.t9t.bpmn.jpa.mapping.IProcessDefinitionDTOMapper;
import com.arvatosystems.t9t.bpmn.jpa.mapping.IProcessExecutionStatusDTOMapper;
import com.arvatosystems.t9t.bpmn.jpa.persistence.IProcessDefinitionEntityResolver;
import com.arvatosystems.t9t.bpmn.jpa.persistence.IProcessExecStatusEntityResolver;
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefRequest;
import com.arvatosystems.t9t.bpmn.request.TriggerSingleProcessNowRequest;
import com.arvatosystems.t9t.bpmn.request.WorkflowActionEnum;
import com.arvatosystems.t9t.bpmn.services.IBpmnPersistenceAccess;
import com.google.common.base.Objects;

import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class BpmnPersistenceAccess implements IBpmnPersistenceAccess {
    private final static Logger LOGGER = LoggerFactory.getLogger(BpmnPersistenceAccess.class);
    protected final IProcessDefinitionEntityResolver resolver = Jdp.getRequired(IProcessDefinitionEntityResolver.class);
    protected final IProcessDefinitionDTOMapper mapper = Jdp.getRequired(IProcessDefinitionDTOMapper.class);
    protected final IProcessExecStatusEntityResolver statusResolver = Jdp.getRequired(IProcessExecStatusEntityResolver.class);
    protected final IProcessExecutionStatusDTOMapper statusMapper = Jdp.getRequired(IProcessExecutionStatusDTOMapper.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public ProcessDefinitionDTO getProcessDefinitionDTO(String processDefinitionId) {
        List<ProcessDefinitionEntity> r = resolver.findByProcessIdWithDefault(true, processDefinitionId);
        if (r == null || r.isEmpty()) {
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "bpmnId = " + processDefinitionId);
        }

        return mapper.mapToDto(r.get(0));
    }

    /** Reads a process definition using the usual fallback rule for some general reference. Throws an Exception if none exists. */
    @Override
    public ProcessDefinitionDTO getProcessDefinitionDTO(ProcessDefinitionRef ref) {
        return mapper.mapToDto(resolver.getEntityData(ref, true));
    }

    /** Reads all process definitions. */
    @Override
    public List<DataWithTrackingW<ProcessDefinitionDTO, FullTrackingWithVersion>> getAllProcessDefinitionsForEngine(String engine) {
        final List<ProcessDefinitionEntity> defs = resolver.readAll(true);
        final List<DataWithTrackingW<ProcessDefinitionDTO, FullTrackingWithVersion>> results = new ArrayList<>(defs.size());
        for (ProcessDefinitionEntity e: defs) {
            if (Objects.equal(engine,  e.getEngine()))
                results.add(mapper.mapToDwt(e));
        }
        return results;
    }

    @Override
    public void save(ProcessDefinitionDTO dto) {
        ProcessDefinitionEntity entity = mapper.mapToEntity(dto, true);
        entity.setObjectRef(resolver.createNewPrimaryKey());

        resolver.save(entity);

        dto.setObjectRef(entity.getObjectRef());
    }

    @Override
    public Long persistNewStatus(ProcessExecutionStatusDTO dto) {
        ProcessExecStatusEntity entity = statusMapper.mapToEntity(dto, false);
        statusResolver.save(entity);
        return entity.getObjectRef();
    }

    @Override
    public ProcessExecutionStatusDTO getProcessExecutionStatusDTO(String processDefinitionId, Long targetObjectRef) {
        ProcessExecStatusEntity existingEntity = statusResolver.findByProcessDefinitionIdAndTargetObjectRef(true, processDefinitionId, targetObjectRef);
        if (existingEntity == null) {
            throw new T9tException(T9tBPMException.BPM_NO_CURRENT_PROCESS, processDefinitionId + ":" + targetObjectRef);
        }

        return statusMapper.mapToDto(existingEntity);
    }

    @Override
    public ProcessExecutionStatusDTO getProcessExecutionStatusOpt(String processDefinitionId, Long targetObjectRef) {
        return statusMapper.mapToDto(statusResolver.findByProcessDefinitionIdAndTargetObjectRef(true, processDefinitionId, targetObjectRef));
    }

    @Override
    public List<ProcessExecutionStatusDTO> getTasksDue(String onlyForProcessDefinitionId, Instant whenDue, boolean includeErrorStatus)
            {
        String pdCondition = onlyForProcessDefinitionId == null ? "" : " AND s.processDefinitionId = :pdId";
        String errorCondition = includeErrorStatus ? "" : " AND s.returnCode IS NULL";
        TypedQuery<ProcessExecStatusEntity> query = statusResolver.getEntityManager().createQuery(
            "SELECT s FROM " + resolver.getEntityClass().getSimpleName()
            + " s WHERE s.tenantRef = :tenantRef AND s.yieldUntil <= :timeLimit"
            + pdCondition + errorCondition + " ORDER BY s.yieldUntil",
            ProcessExecStatusEntity.class
        );
        query.setParameter("tenantRef", statusResolver.getSharedTenantRef());
        query.setParameter("timeLimit", whenDue);
        if (onlyForProcessDefinitionId != null)
            query.setParameter("pdId", onlyForProcessDefinitionId);
        return statusMapper.mapListToDto(query.getResultList());
    }

    @Override
    public List<Long> getTaskRefsDue(String onlyForProcessDefinitionId, Instant whenDue, boolean includeErrorStatus)
            {
        String pdCondition = onlyForProcessDefinitionId == null ? "" : " AND s.processDefinitionId = :pdId";
        String errorCondition = includeErrorStatus ? "" : " AND s.returnCode IS NULL";
        TypedQuery<Long> query = statusResolver.getEntityManager().createQuery(
            "SELECT s.objectRef FROM " + statusResolver.getEntityClass().getSimpleName()
            + " s WHERE s.tenantRef = :tenantRef AND s.yieldUntil <= :timeLimit "
            + pdCondition + errorCondition + " ORDER BY s.yieldUntil",
            Long.class
        );
        query.setParameter("tenantRef", statusResolver.getSharedTenantRef());
        query.setParameter("timeLimit", whenDue);
        if (onlyForProcessDefinitionId != null)
            query.setParameter("pdId", onlyForProcessDefinitionId);
        return query.getResultList();
    }

    @Override
    public Long createOrUpdateNewStatus(RequestContext ctx, ProcessExecutionStatusDTO dto, ExecuteProcessWithRefRequest rq) {
        Long objectRef = null;
        ProcessExecStatusEntity existingEntity = statusResolver.findByProcessDefinitionIdAndTargetObjectRef(true, dto.getProcessDefinitionId(), dto.getTargetObjectRef());
        if (existingEntity != null) { // use object ref to prevent searching for existing entity every time
            // Existing status: check what to do
            if (rq.getIfEntryExists() == WorkflowActionEnum.ERROR) {
                LOGGER.error("Attempted to recreate an existing business process entry: {}:{}", rq.getProcessDefinitionId(), rq.getTargetObjectRef());
                throw new T9tException(T9tBPMException.BPM_CURRENT_PROCESS_EXISTS, rq.getProcessDefinitionId() + ":" + rq.getTargetObjectRef());
            }
            if (rq.getIfEntryExists() == WorkflowActionEnum.NO_ACTIVITY) {
                return existingEntity.getObjectRef();
            }
            // PRIO 1: set next step explicitly if defined
            if(rq.getWorkflowStep()!=null) {
                existingEntity.setNextStep(rq.getWorkflowStep());
            } // PRIO 2: else if RestartAtBeginning set next step to null
            else if (Boolean.TRUE.equals(rq.getRestartAtBeginningIfExists())) {
                existingEntity.setNextStep(null);  // reset to beginning
            }
            // ELSE resume at current position (RUN: default action)

            existingEntity.setCurrentParameters(dto.getCurrentParameters());
            existingEntity.setYieldUntil(dto.getYieldUntil());
            statusResolver.update(existingEntity);
            objectRef = existingEntity.getObjectRef();
        } else {
            if (rq.getIfNoEntryExists() == WorkflowActionEnum.ERROR) {
                LOGGER.error("Missing an existing business process entry: {}:{}", rq.getProcessDefinitionId(), rq.getTargetObjectRef());
                throw new T9tException(T9tBPMException.BPM_NO_CURRENT_PROCESS, rq.getProcessDefinitionId() + ":" + rq.getTargetObjectRef());
            }
            if (rq.getIfNoEntryExists() == WorkflowActionEnum.NO_ACTIVITY) {
                return null;
            }
            objectRef = persistNewStatus(dto);
        }
        if (rq.getInitialDelay() == null) {
            // launch the process immediately
            TriggerSingleProcessNowRequest trigger = new TriggerSingleProcessNowRequest(objectRef);
            executor.executeAsynchronous(ctx, trigger);
        }
        return objectRef;
    }
}
