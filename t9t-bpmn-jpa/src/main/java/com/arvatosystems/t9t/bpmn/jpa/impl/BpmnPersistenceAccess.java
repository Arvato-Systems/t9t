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
package com.arvatosystems.t9t.bpmn.jpa.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.IClusterEnvironment;
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

import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class BpmnPersistenceAccess implements IBpmnPersistenceAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(BpmnPersistenceAccess.class);

    protected final IProcessDefinitionEntityResolver resolver = Jdp.getRequired(IProcessDefinitionEntityResolver.class);
    protected final IProcessDefinitionDTOMapper mapper = Jdp.getRequired(IProcessDefinitionDTOMapper.class);
    protected final IProcessExecStatusEntityResolver statusResolver = Jdp.getRequired(IProcessExecStatusEntityResolver.class);
    protected final IProcessExecutionStatusDTOMapper statusMapper = Jdp.getRequired(IProcessExecutionStatusDTOMapper.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);
    protected final IClusterEnvironment clusterEnvironment = Jdp.getRequired(IClusterEnvironment.class);

    @Override
    public ProcessDefinitionDTO getProcessDefinitionDTO(final String processDefinitionId) {
        final List<ProcessDefinitionEntity> r = resolver.findByProcessIdWithDefault(true, processDefinitionId);
        if (r == null || r.isEmpty()) {
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "bpmnId = " + processDefinitionId);
        }

        return mapper.mapToDto(r.get(0));
    }

    /** Reads a process definition using the usual fallback rule for some general reference. Throws an Exception if none exists. */
    @Override
    public ProcessDefinitionDTO getProcessDefinitionDTO(final ProcessDefinitionRef ref) {
        return mapper.mapToDto(resolver.getEntityData(ref));
    }

    /** Reads all process definitions. */
    @Override
    public List<DataWithTrackingS<ProcessDefinitionDTO, FullTrackingWithVersion>> getAllProcessDefinitionsForEngine(final String engine) {
        final List<ProcessDefinitionEntity> defs = resolver.readAll(true);
        final List<DataWithTrackingS<ProcessDefinitionDTO, FullTrackingWithVersion>> results = new ArrayList<>(defs.size());
        for (final ProcessDefinitionEntity e: defs) {
            if (Objects.equal(engine,  e.getEngine()))
                results.add(mapper.mapToDwt(e));
        }
        return results;
    }

    @Override
    public void save(final ProcessDefinitionDTO dto) {
        final ProcessDefinitionEntity entity = mapper.mapToEntity(dto);
        entity.setObjectRef(resolver.createNewPrimaryKey());

        resolver.save(entity);

        dto.setObjectRef(entity.getObjectRef());
    }

    @Override
    public Long persistNewStatus(final ProcessExecutionStatusDTO dto) {
        final ProcessExecStatusEntity entity = statusMapper.mapToEntity(dto);
        statusResolver.save(entity);
        return entity.getObjectRef();
    }

    @Override
    public ProcessExecutionStatusDTO getProcessExecutionStatusDTO(final String processDefinitionId, final Long targetObjectRef) {
        final ProcessExecStatusEntity existingEntity = statusResolver.findByProcessDefinitionIdAndTargetObjectRef(true, processDefinitionId, targetObjectRef);
        if (existingEntity == null) {
            throw new T9tException(T9tBPMException.BPM_NO_CURRENT_PROCESS, processDefinitionId + ":" + targetObjectRef);
        }

        return statusMapper.mapToDto(existingEntity);
    }

    @Override
    public ProcessExecutionStatusDTO getProcessExecutionStatusOpt(final String processDefinitionId, final Long targetObjectRef) {
        return statusMapper.mapToDto(statusResolver.findByProcessDefinitionIdAndTargetObjectRef(true, processDefinitionId, targetObjectRef));
    }

    protected <E> List<E> getQueryForDueTasks(final Class<E> type, final String field, final String onlyForProcessDefinitionId, final Instant whenDue,
      final boolean includeErrorStatus, final boolean allClusterNodes, final String onlyForNextStep, final Collection<Integer> returnCodes,
      final Integer maxTasks) {
        String nodeCondition = "";
        int numPartitions = 1;
        Collection<Integer> shards = Collections.emptyList();
        final String tenantId = statusResolver.getSharedTenantId();
        if (!allClusterNodes) {
            numPartitions = clusterEnvironment.getNumberOfNodes();
            shards = clusterEnvironment.getListOfShards(tenantId);
            if (shards.isEmpty()) {
                LOGGER.debug("getTasksDue(): No process partitions assigned to this node");
                return Collections.emptyList();
            }
            if (shards.size() < numPartitions) {
                // we want a subset of the data only
                nodeCondition = " AND MOD(s.runOnNode, :partitions) IN :listOfPartitions";
                if (LOGGER.isDebugEnabled()) {
                    final StringBuilder sb = new StringBuilder(200);
                    for (final Integer shard : shards) {
                        sb.append(' ').append(shard);
                    }
                    LOGGER.debug("getTasksDue(): only {} of {} partitions assigned to this node, namely {}", numPartitions, shards.size(), sb.toString());
                }
            } else {
                LOGGER.debug("getTasksDue(): all partitions are assigned to this node");
            }
        }
        final String errorCondition;
        if (returnCodes == null) {
            errorCondition = includeErrorStatus ? "" : " AND s.returnCode IS NULL";
        } else {
            errorCondition = includeErrorStatus
                ? " AND s.returnCode IS NOT NULL AND a.returnCode NOT IN :returnCodes"
                : " AND s.returnCode IN :returnCodes";
        }
        final String pdCondition = onlyForProcessDefinitionId == null ? "" : " AND s.processDefinitionId = :pdId";
        final String stepCondition = onlyForNextStep == null ? "" : " AND s.nextStep = :step";
        final String queryString = "SELECT s" + field + " FROM " + statusResolver.getEntityClass().getSimpleName()
                + " s WHERE s.tenantId = :tenantId AND s.yieldUntil <= :timeLimit"
                + pdCondition + errorCondition + nodeCondition + stepCondition + " ORDER BY s.yieldUntil";
        LOGGER.trace("Now performing query {}", queryString);

        final TypedQuery<E> query = statusResolver.getEntityManager().createQuery(queryString, type);
        query.setParameter("tenantId", tenantId);
        query.setParameter("timeLimit", whenDue);
        if (onlyForProcessDefinitionId != null) {
            query.setParameter("pdId", onlyForProcessDefinitionId);
        }
        if (onlyForNextStep != null) {
            query.setParameter("step", onlyForNextStep);
        }
        if (returnCodes != null) {
            query.setParameter("returnCodes", returnCodes);
        }
        if (nodeCondition.length() > 0) {
            query.setParameter("partitions", numPartitions);
            query.setParameter("listOfPartitions", shards);
        }
        if (maxTasks != null) {
            query.setMaxResults(maxTasks);
        }
        return query.getResultList();
    }

    @Override
    public List<ProcessExecutionStatusDTO> getTasksDue(final String onlyForProcessDefinitionId, final Instant whenDue,
      final boolean includeErrorStatus, final boolean allClusterNodes, final String onlyForNextStep, final Collection<Integer> returnCodes) {
        return statusMapper.mapListToDto(getQueryForDueTasks(ProcessExecStatusEntity.class, "",
          onlyForProcessDefinitionId, whenDue, includeErrorStatus, allClusterNodes,
          onlyForNextStep, returnCodes, null));
    }

    @Override
    public List<Long> getTaskRefsDue(final String onlyForProcessDefinitionId, final Instant whenDue, final boolean includeErrorStatus,
      final boolean allClusterNodes, final String onlyForNextStep, final Collection<Integer> returnCodes, final Integer maxTasks) {
        return getQueryForDueTasks(Long.class, ".objectRef",
          onlyForProcessDefinitionId, whenDue, includeErrorStatus, allClusterNodes,
          onlyForNextStep, returnCodes, maxTasks);
    }

    @Override
    public Long createOrUpdateNewStatus(final RequestContext ctx, final ProcessExecutionStatusDTO dto, final ExecuteProcessWithRefRequest rq,
      final boolean restart) {
        final Long objectRef;
        final ProcessExecStatusEntity existingEntity = statusResolver.findByProcessDefinitionIdAndTargetObjectRef(true, dto.getProcessDefinitionId(), dto.getTargetObjectRef());
        if (existingEntity != null && refresh(existingEntity) != null) { // refresh because it might be removed at another thread
            ctx.lockRef(existingEntity.getObjectRef()); // lock the status ref
            refresh(existingEntity); // refresh again after acquiring the lock to avoid outdated version
            // Existing status: check what to do
            if (rq.getIfEntryExists() == WorkflowActionEnum.ERROR) {
                LOGGER.error("Attempted to recreate an existing business process entry: {}:{}", rq.getProcessDefinitionId(), rq.getTargetObjectRef());
                throw new T9tException(T9tBPMException.BPM_CURRENT_PROCESS_EXISTS, rq.getProcessDefinitionId() + ":" + rq.getTargetObjectRef());
            }
            if (rq.getIfEntryExists() == WorkflowActionEnum.NO_ACTIVITY) {
                return existingEntity.getObjectRef();
            }

            if (rq.getWorkflowStep() != null) {
                // PRIO 1: set next step explicitly if defined
                existingEntity.setNextStep(rq.getWorkflowStep());
            } else if (restart) {
                // PRIO 2: else if RestartAtBeginning set next step to null
                existingEntity.setNextStep(null);  // reset to beginning
            }
            // ELSE resume at current position (RUN: default action)
            if (rq.getInitialParameters() != null) {
                final boolean merge = Boolean.TRUE.equals(rq.getMergeParameters());
                existingEntity.setCurrentParameters(merge
                    ? JsonUtil.mergeZ(existingEntity.getCurrentParameters(), rq.getInitialParameters())
                    : rq.getInitialParameters());
            }
            existingEntity.setYieldUntil(dto.getYieldUntil());
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
            ctx.lockRef(objectRef);
        }
        if (rq.getInitialDelay() == null) {
            // launch the process immediately (TODO: need a special node!)
            final TriggerSingleProcessNowRequest trigger = new TriggerSingleProcessNowRequest(objectRef);
            executor.executeAsynchronous(ctx, trigger);
        }
        return objectRef;
    }

    private ProcessExecStatusEntity refresh(final ProcessExecStatusEntity statusEntity) {
        try {
            statusResolver.getEntityManager().refresh(statusEntity);
        } catch (final EntityNotFoundException enfe) {
            LOGGER.debug("Status probably been removed at another thread.");
            return null;
        }
        return statusEntity;
    }
}
