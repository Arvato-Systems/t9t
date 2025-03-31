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
package com.arvatosystems.t9t.bpmn2.be.request;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.ProcessInstanceDTO;
import com.arvatosystems.t9t.bpmn2.be.camunda.utils.SearchRequestMapper;
import com.arvatosystems.t9t.bpmn2.request.ProcessInstanceSearchRequest;

import de.jpaw.bonaparte.pojos.api.BooleanFilter;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.dp.Jdp;

public class ProcessInstanceSearchRequestHandler extends AbstractBPMNRequestHandler<ProcessInstanceSearchRequest> {

    private final RuntimeService runtimeService = Jdp.getRequired(RuntimeService.class);
    private final RepositoryService repositoryService = Jdp.getRequired(RepositoryService.class);

    private static final SearchRequestMapper<ProcessInstanceQuery, ProcessInstance> queryMapping = new SearchRequestMapper<>();

    static {
        queryMapping.addFilterMapping("ProcessDefinitionId", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.processDefinitionKey(fieldFilter.getEqualsValue());
            } else {
                return null;
            }
        });

        queryMapping.addFilterMapping("ProcessInstanceId", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.processInstanceId(fieldFilter.getEqualsValue());
            } else {
                return null;
            }
        });

        queryMapping.addFilterMapping("BusinessKey", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.processInstanceBusinessKey(fieldFilter.getEqualsValue());
            } else if (fieldFilter.getLikeValue() != null) {
                return query.processInstanceBusinessKeyLike(fieldFilter.getLikeValue());
            } else {
                return null;
            }
        });

        queryMapping.addFilterMapping("IsActive", BooleanFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getBooleanValue()) {
                return query.active();
            } else {
                return query.suspended();
            }
        });

        queryMapping.addSortMapping("ProcessDefinitionId", ProcessInstanceQuery::orderByProcessDefinitionKey);
        queryMapping.addSortMapping("ProcessInstanceId", ProcessInstanceQuery::orderByProcessInstanceId);
        queryMapping.addSortMapping("BusinessKey", ProcessInstanceQuery::orderByBusinessKey);
    }

    @Override
    protected ServiceResponse executeInWorkflowContext(RequestContext requestContext, ProcessInstanceSearchRequest request) throws Exception {
        final List<ProcessInstance> processInstances = queryMapping.search(runtimeService.createProcessInstanceQuery()
                                                                                         .tenantIdIn(requestContext.tenantId),
                request);

        final Set<String> processDefinitionIds = processInstances.stream()
                                                                 .map(ProcessInstance::getProcessDefinitionId)
                                                                 .collect(toSet());
        final Map<String, ProcessDefinition> processDefinitionsById = repositoryService.createProcessDefinitionQuery()
                                                                                       .processDefinitionIdIn(processDefinitionIds.toArray(
                                                                                               new String[processDefinitionIds.size()]))
                                                                                       .list()
                                                                                       .stream()
                                                                                       .collect(toMap(
                                                                                               ProcessDefinition::getId,
                                                                                               identity()));

        final ReadAllResponse<ProcessInstanceDTO, NoTracking> response = new ReadAllResponse<>();
        response.setDataList(processInstances.stream()
                                             .map(processInstance -> map(processInstance, processDefinitionsById))
                                             .collect(toList()));
        response.setNumResults(Long.valueOf(response.getDataList()
                                                    .size()));

        return response;
    }

    private DataWithTrackingS<ProcessInstanceDTO, NoTracking> map(ProcessInstance processInstance,
                                                                  Map<String, ProcessDefinition> processDefinitionsById) {

        final ProcessInstanceDTO resultExecutionStatus = new ProcessInstanceDTO();
        resultExecutionStatus.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        resultExecutionStatus.setProcessDefinitionKey(processDefinitionsById.get(processInstance.getProcessDefinitionId())
                                                                            .getKey());
        resultExecutionStatus.setProcessInstanceId(processInstance.getId());
        resultExecutionStatus.setBusinessKey(processInstance.getBusinessKey());
        resultExecutionStatus.setIsActive(!processInstance.isSuspended());
        resultExecutionStatus.setIsEnded(processInstance.isEnded());

        final DataWithTrackingS<ProcessInstanceDTO, NoTracking> result = new DataWithTrackingS<>();
        result.setTenantId(processInstance.getTenantId());
        result.setData(resultExecutionStatus);
        result.setTracking(new NoTracking());

        return result;
    }
}
