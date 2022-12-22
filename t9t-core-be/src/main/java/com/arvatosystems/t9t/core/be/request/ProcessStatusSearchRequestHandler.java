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
package com.arvatosystems.t9t.core.be.request;

import com.arvatosystems.t9t.base.request.ProcessStatusDTO;
import com.arvatosystems.t9t.base.request.ProcessStatusRequest;
import com.arvatosystems.t9t.base.request.ProcessStatusResponse;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IExporterTool;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.request.ProcessStatusSearchRequest;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.dp.Jdp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessStatusSearchRequestHandler extends AbstractSearchRequestHandler<ProcessStatusSearchRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStatusSearchRequestHandler.class);
    private static final String FIELD_NAME_THREAD_ID = "threadId";
    private static final String FIELD_NAME_AGE_IN_MS = "ageInMs";
    private static final String FIELD_NAME_SESSION_REF = "sessionRef";
    private static final String FIELD_NAME_PROCESS_REF = "processRef";
    private static final String FIELD_NAME_PROCESS_STARTED_AT = "processStartedAt";
    private static final String FIELD_NAME_TENANT_ID = "tenantId";
    private static final String FIELD_NAME_USER_ID = "userId";
    private static final String FIELD_NAME_PQON = "pqon";
    private static final String FIELD_NAME_INVOKING_PROCESS_REF = "invokingProcessRef";
    private static final String FIELD_NAME_PROGRESS_COUNTER = "progressCounter";
    private static final String FIELD_NAME_STATUS_TEXT = "statusText";

    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @SuppressWarnings("unchecked")
    protected final IExporterTool<ProcessStatusDTO, NoTracking> exporter = Jdp.getRequired(IExporterTool.class);

    @Override
    public ReadAllResponse<ProcessStatusDTO, NoTracking> execute(final RequestContext ctx, final ProcessStatusSearchRequest rq) throws Exception {
        // map generic search filters to the specific parameters
        final ProcessStatusRequest psCmd = new ProcessStatusRequest();
        psCmd.setMinAgeInMs(0L); // default
        if (rq.getSearchFilter() != null) {
            applyFilters(psCmd, rq.getSearchFilter());
        }
        final List<ProcessStatusDTO> processes = executor.executeSynchronousAndCheckResult(ctx, psCmd, ProcessStatusResponse.class).getProcesses();
        final List<DataWithTrackingS<ProcessStatusDTO, NoTracking>> dataList = new ArrayList<>(processes.size());
        for (ProcessStatusDTO processStatus: processes) {
            final DataWithTrackingS<ProcessStatusDTO, NoTracking> dwt = new DataWithTrackingS<>();
            dwt.setData(processStatus);
            dwt.setTenantId(ctx.tenantId); // avoid having null here
            dataList.add(dwt);
        }

        final List<DataWithTrackingS<ProcessStatusDTO, NoTracking>> finalSort = rq.getSortColumns() == null || rq.getSortColumns().isEmpty() ? dataList
                : sortProcessStatusDwt(rq.getSortColumns(), dataList);
        return exporter.returnOrExport(exporter.cut(finalSort, rq.getOffset(), rq.getLimit()), rq.getSearchOutputTarget());
    }

    private List<DataWithTrackingS<ProcessStatusDTO, NoTracking>> sortProcessStatusDwt(final List<SortColumn> sortColumns,
            final List<DataWithTrackingS<ProcessStatusDTO, NoTracking>> dataList) {
        // sort by one column is supported
        final SortColumn sortColumn = sortColumns.get(0);
        LOGGER.debug("Result list of {} entries should be sorted by {} {}", Integer.valueOf(dataList.size()), sortColumn.getFieldName(),
                sortColumn.getDescending() ? "descending" : "ascending");
        if (sortColumn.getFieldName() != null) {
            switch (sortColumn.getFieldName()) {
            case FIELD_NAME_THREAD_ID:
                dataList.sort(compareByLongField(ProcessStatusDTO::getThreadId));
                break;
            case FIELD_NAME_AGE_IN_MS:
                dataList.sort(compareByLongField(ProcessStatusDTO::getAgeInMs));
                break;
            case FIELD_NAME_SESSION_REF:
                dataList.sort(compareByLongField(ProcessStatusDTO::getSessionRef));
                break;
            case FIELD_NAME_PROCESS_REF:
                dataList.sort(compareByLongField(ProcessStatusDTO::getProcessRef));
                break;
            case FIELD_NAME_PROCESS_STARTED_AT:
                dataList.sort(compareByInstantField(ProcessStatusDTO::getProcessStartedAt));
                break;
            case FIELD_NAME_TENANT_ID:
                dataList.sort(compareByStringField(ProcessStatusDTO::getTenantId));
                break;
            case FIELD_NAME_USER_ID:
                dataList.sort(compareByStringField(ProcessStatusDTO::getUserId));
                break;
            case FIELD_NAME_PQON:
                dataList.sort(compareByStringField(ProcessStatusDTO::getPqon));
                break;
            case FIELD_NAME_INVOKING_PROCESS_REF:
                dataList.sort(compareByLongField(ProcessStatusDTO::getInvokingProcessRef));
                break;
            case FIELD_NAME_PROGRESS_COUNTER:
                dataList.sort(compareByIntField(ProcessStatusDTO::getProgressCounter));
                break;
            case FIELD_NAME_STATUS_TEXT:
                dataList.sort(compareByStringField(ProcessStatusDTO::getStatusText));
                break;
            }

            final List<DataWithTrackingS<ProcessStatusDTO, NoTracking>> sortedList = new ArrayList<>(dataList);
            if (sortColumn.getDescending()) {
                Collections.reverse(sortedList);
            }
            return sortedList; // expression assigned to finalSort
        } else {
            return dataList;
        }
    }

    protected void applyFilters(final ProcessStatusRequest psCmd, final SearchFilter searchFilter) {
        if (searchFilter instanceof AndFilter andFilter) {
            applyFilters(psCmd, andFilter.getFilter1());
            applyFilters(psCmd, andFilter.getFilter2());
        } else if (searchFilter instanceof LongFilter longFilter) {
            if (FIELD_NAME_AGE_IN_MS.equals(longFilter.getFieldName()) && longFilter.getLowerBound() != null) {
                psCmd.setMinAgeInMs(longFilter.getLowerBound());
            }
        } else if (searchFilter instanceof UnicodeFilter unicodeFilter) {
            if (unicodeFilter.getEqualsValue() != null) {
                String equalsValue = unicodeFilter.getEqualsValue();
                if (FIELD_NAME_TENANT_ID.equals(unicodeFilter.getFieldName())) {
                    psCmd.setTenantId(equalsValue);
                } else if (FIELD_NAME_USER_ID.equals(unicodeFilter.getFieldName())) {
                    psCmd.setUserId(equalsValue);
                }
            }
        }
    }

    private static Comparator<DataWithTrackingS<ProcessStatusDTO, NoTracking>> compareByStringField (final Function<ProcessStatusDTO, String> func) {
        return (item1, item2) -> func.apply(item1.getData()).compareTo(func.apply(item2.getData()));
    }

    private static Comparator<DataWithTrackingS<ProcessStatusDTO, NoTracking>> compareByLongField (final Function<ProcessStatusDTO, Long> func) {
        return (item1, item2) -> func.apply(item1.getData()).compareTo(func.apply(item2.getData()));
    }

    private static Comparator<DataWithTrackingS<ProcessStatusDTO, NoTracking>> compareByIntField (final Function<ProcessStatusDTO, Integer> func) {
        return (item1, item2) -> func.apply(item1.getData()).compareTo(func.apply(item2.getData()));
    }

    private static Comparator<DataWithTrackingS<ProcessStatusDTO, NoTracking>> compareByInstantField (final Function<ProcessStatusDTO, Instant> func) {
        return (item1, item2) -> func.apply(item1.getData()).compareTo(func.apply(item2.getData()));
    }
}
