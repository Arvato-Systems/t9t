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
package com.arvatosystems.t9t.bpmn2.be.request;

import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.IdentifierConverter.bpmnTenantIdToT9tTenantRef;
import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.IdentifierConverter.t9tTenantRefToBPMNTenantId;
import static java.util.stream.Collectors.toList;

import java.time.ZoneId;
import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.IncidentDTO;
import com.arvatosystems.t9t.bpmn2.be.camunda.utils.SearchRequestMapper;
import com.arvatosystems.t9t.bpmn2.request.IncidentSearchRequest;

import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;

public class IncidentSearchRequestHandler extends AbstractBPMNRequestHandler<IncidentSearchRequest> {

    private final RuntimeService runtimeService = Jdp.getRequired(RuntimeService.class);

    private static final SearchRequestMapper<IncidentQuery, Incident> queryMapping = new SearchRequestMapper<>();

    static {
        queryMapping.addFilterMapping("IncidentId", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.incidentId(fieldFilter.getEqualsValue());
            } else {
                return null;
            }
        });

        queryMapping.addFilterMapping("IncidentType", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.incidentType(fieldFilter.getEqualsValue());
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

        queryMapping.addFilterMapping("ActivityId", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.activityId(fieldFilter.getEqualsValue());
            } else {
                return null;
            }
        });

        queryMapping.addFilterMapping("Message", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.incidentMessage(fieldFilter.getEqualsValue());
            } else {
                return null;
            }
        });

        queryMapping.addSortMapping("IncidentId", IncidentQuery::orderByIncidentId);
        queryMapping.addSortMapping("IncidentType", IncidentQuery::orderByIncidentType);
        queryMapping.addSortMapping("ProcessInstanceId", IncidentQuery::orderByProcessInstanceId);
        queryMapping.addSortMapping("ActivityId", IncidentQuery::orderByActivityId);
        queryMapping.addSortMapping("Created", IncidentQuery::orderByIncidentTimestamp);
    }

    @Override
    protected ServiceResponse executeInWorkflowContext(RequestContext requestContext, IncidentSearchRequest request) throws Exception {
        final List<Incident> incidents = queryMapping.search(runtimeService.createIncidentQuery()
                                                                           .tenantIdIn(t9tTenantRefToBPMNTenantId(requestContext.getTenantRef())),
                request);

        final ReadAllResponse<IncidentDTO, NoTracking> response = new ReadAllResponse<>();
        response.setDataList(incidents.stream()
                                      .map(incident -> map(incident))
                                      .collect(toList()));
        response.setNumResults(Long.valueOf(response.getDataList()
                                                    .size()));
        return response;
    }

    private DataWithTrackingW<IncidentDTO, NoTracking> map(Incident incident) {

        final IncidentDTO dto = new IncidentDTO();

        dto.setIncidentId(incident.getId());
        dto.setIncidentType(incident.getIncidentType());
        dto.setProcessInstanceId(incident.getProcessInstanceId());
        dto.setActivityId(incident.getActivityId());
        dto.setMessage(incident.getIncidentMessage());
        dto.setCreated(incident.getIncidentTimestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        final DataWithTrackingW<IncidentDTO, NoTracking> result = new DataWithTrackingW<>();
        result.setTenantRef(bpmnTenantIdToT9tTenantRef(incident.getTenantId()));
        result.setData(dto);
        result.setTracking(new NoTracking());

        return result;
    }
}
