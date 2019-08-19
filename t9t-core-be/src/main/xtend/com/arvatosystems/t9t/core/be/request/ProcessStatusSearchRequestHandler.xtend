/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.core.be.request

import com.arvatosystems.t9t.base.request.ProcessStatusDTO
import com.arvatosystems.t9t.base.request.ProcessStatusRequest
import com.arvatosystems.t9t.base.request.ProcessStatusResponse
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.IExporterTool
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.core.request.ProcessStatusSearchRequest
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.AndFilter
import de.jpaw.bonaparte.pojos.api.LongFilter
import de.jpaw.bonaparte.pojos.api.NoTracking
import de.jpaw.bonaparte.pojos.api.SearchFilter
import de.jpaw.bonaparte.pojos.api.UnicodeFilter
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW
import de.jpaw.dp.Inject
import java.util.ArrayList

@AddLogger
class ProcessStatusSearchRequestHandler extends AbstractSearchRequestHandler<ProcessStatusSearchRequest> {
    @Inject protected IExecutor executor
    @Inject protected IExporterTool<ProcessStatusDTO, NoTracking> exporter

    def protected void applyFilters(ProcessStatusRequest psCmd, SearchFilter it) {
        switch it {
            AndFilter: {
                psCmd.applyFilters(filter1)
                psCmd.applyFilters(filter2)
            }
            LongFilter:
                if (fieldName == "ageInMs" && lowerBound !== null)
                    psCmd.minAgeInMs = lowerBound
            UnicodeFilter:
                if (equalsValue !== null) {
                    if (fieldName == "tenantId")
                        psCmd.tenantId = equalsValue
                    else if (fieldName == "userId")
                        psCmd.userId = equalsValue

                }
        }
    }

    override ReadAllResponse<ProcessStatusDTO, NoTracking> execute(RequestContext ctx, ProcessStatusSearchRequest rq) {
        // map generic search filters to the specific parameters
        val psCmd = new ProcessStatusRequest
        psCmd.minAgeInMs = 0L   // default
        if (rq.searchFilter !== null)
            psCmd.applyFilters(rq.searchFilter)
        val processes = executor.executeSynchronousAndCheckResult(ctx, psCmd, ProcessStatusResponse).processes
        val dataList = new ArrayList<DataWithTrackingW<ProcessStatusDTO, NoTracking>>(processes.size)
        for (dto: processes) {
            val dwt = new DataWithTrackingW<ProcessStatusDTO, NoTracking>
            dwt.data = dto
            dwt.tenantRef = ctx.tenantRef  // avoid having null here
            dataList.add(dwt)
        }
        val finalSort = if (rq.sortColumns.nullOrEmpty) {
            dataList
        } else {
            // sort by one column is supported
            val sortColumn = rq.sortColumns.get(0)
            LOGGER.debug("Result list of {} entries should be sorted by {} {}",
                dataList.size, sortColumn.fieldName, if (sortColumn.descending) "descending" else "ascending"
            )
            val sortedList = switch (sortColumn.fieldName) {
            case "threadId":            dataList.sortBy[data.threadId           ].toList
            case "ageInMs":             dataList.sortBy[data.ageInMs            ].toList
            case "sessionRef":          dataList.sortBy[data.sessionRef         ].toList
            case "processRef":          dataList.sortBy[data.processRef         ].toList
            case "processStartedAt":    dataList.sortBy[data.processStartedAt   ].toList
            case "tenantId":            dataList.sortBy[data.tenantId           ].toList
            case "userId":              dataList.sortBy[data.userId             ].toList
            case "pqon":                dataList.sortBy[data.pqon               ].toList
            case "invokingProcessRef":  dataList.sortBy[data.invokingProcessRef ].toList
            case "progressCounter":     dataList.sortBy[data.progressCounter    ].toList
            case "statusText":          dataList.sortBy[data.statusText         ].toList
            default: dataList  // no sort...
            }
            if (sortColumn.descending) sortedList.reverse else sortedList  // expression assigned to finalSort
        }
        return exporter.returnOrExport(exporter.cut(finalSort, rq.offset, rq.limit), rq.getSearchOutputTarget());
    }
}
