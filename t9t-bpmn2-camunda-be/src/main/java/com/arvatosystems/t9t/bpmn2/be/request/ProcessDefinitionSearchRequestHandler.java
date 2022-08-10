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

import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.SearchRequestUtils.initTrackingFields;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn2.be.camunda.utils.SearchRequestMapper;
import com.arvatosystems.t9t.bpmn2.request.ProcessDefinitionSearchRequest;

import de.jpaw.bonaparte.pojos.api.BooleanFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.dp.Jdp;

public class ProcessDefinitionSearchRequestHandler extends AbstractBPMNRequestHandler<ProcessDefinitionSearchRequest> {

    private final RepositoryService repositoryService = Jdp.getRequired(RepositoryService.class);

    private static final SearchRequestMapper<ProcessDefinitionQuery, ProcessDefinition> queryMapping = new SearchRequestMapper<>();

    static {
        queryMapping.addFilterMapping("ProcessDefinitionId", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.processDefinitionId(fieldFilter.getEqualsValue());
            } else {
                return null;
            }
        });

        queryMapping.addFilterMapping("ProcessDefinitionKey", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.processDefinitionKey(fieldFilter.getEqualsValue());
            } else if (fieldFilter.getLikeValue() != null) {
                return query.processDefinitionKeyLike(fieldFilter.getLikeValue());
            } else {
                return null;
            }
        });

        queryMapping.addFilterMapping("Name", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.processDefinitionName(fieldFilter.getEqualsValue());
            } else if (fieldFilter.getLikeValue() != null) {
                return query.processDefinitionNameLike(fieldFilter.getLikeValue());
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

        queryMapping.addSortMapping("ProcessDefinitionId", ProcessDefinitionQuery::orderByProcessDefinitionId);
        queryMapping.addSortMapping("ProcessDefinitionKey", ProcessDefinitionQuery::orderByProcessDefinitionKey);
        queryMapping.addSortMapping("Name", ProcessDefinitionQuery::orderByProcessDefinitionName);
    }

    @Override
    protected ServiceResponse executeInWorkflowContext(RequestContext requestContext, ProcessDefinitionSearchRequest request) throws Exception {

        final List<ProcessDefinition> processDefinitions = queryMapping.search(repositoryService.createProcessDefinitionQuery()
                                                                                                .tenantIdIn(requestContext.tenantId),
                                                                               request);

        final ReadAllResponse<ProcessDefinitionDTO, FullTrackingWithVersion> response = new ReadAllResponse<>();
        response.setDataList(processDefinitions.stream()
                                               .map(processDefinition -> map(processDefinition))
                                               .collect(toList()));
        response.setNumResults(Long.valueOf(response.getDataList()
                                                    .size()));

        return response;
    }

    private DataWithTrackingS<ProcessDefinitionDTO, FullTrackingWithVersion> map(ProcessDefinition processDefinition) {

        final ProcessDefinitionDTO resultDefinition = new ProcessDefinitionDTO();
        resultDefinition.setProcessDefinitionId(processDefinition.getId());
        resultDefinition.setProcessDefinitionKey(processDefinition.getKey());
        resultDefinition.setName(processDefinition.getName());
        resultDefinition.setIsActive(!processDefinition.isSuspended());

        final FullTrackingWithVersion tracking = new FullTrackingWithVersion();
        initTrackingFields(tracking);
        tracking.setVersion(processDefinition.getVersion());

        final DataWithTrackingS<ProcessDefinitionDTO, FullTrackingWithVersion> result = new DataWithTrackingS<>();
        result.setTenantId(processDefinition.getTenantId());
        result.setData(resultDefinition);
        result.setTracking(tracking);

        return result;
    }

}
