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

import com.arvatosystems.t9t.base.request.ComponentInfoDTO
import com.arvatosystems.t9t.base.request.RetrieveComponentInfoRequest
import com.arvatosystems.t9t.base.request.RetrieveComponentInfoResponse
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.IExporterTool
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.core.request.ComponentInfoSearchRequest
import de.jpaw.bonaparte.pojos.api.AndFilter
import de.jpaw.bonaparte.pojos.api.NoTracking
import de.jpaw.bonaparte.pojos.api.SearchFilter
import de.jpaw.bonaparte.pojos.api.SortColumn
import de.jpaw.bonaparte.pojos.api.UnicodeFilter
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW
import de.jpaw.dp.Inject
import java.util.ArrayList
import java.util.Collections
import java.util.List

class ComponentInfoSearchRequestHandler extends AbstractSearchRequestHandler<ComponentInfoSearchRequest> {
    @Inject protected IExecutor executor
    @Inject protected IExporterTool<ComponentInfoDTO, NoTracking> exporter

    def protected List<ComponentInfoDTO> applyFilters(SearchFilter it, List<ComponentInfoDTO> input) {
        switch it {
            AndFilter: return filter1.applyFilters(filter2.applyFilters(input))
            UnicodeFilter:
                if (equalsValue !== null) {
                    if (fieldName == "groupId")
                        return input.filter[dto | dto.groupId == equalsValue].toList
                    else if (fieldName == "artifactId")
                        return input.filter[dto | dto.artifactId == equalsValue].toList
                } else if (likeValue !== null) {
                    val like = if (likeValue.endsWith("%")) likeValue.substring(0, likeValue.length - 1) else likeValue
                    if (fieldName == "groupId")
                        return input.filter[dto | dto.groupId.startsWith(like)].toList
                    else if (fieldName == "artifactId")
                        return input.filter[dto | dto.artifactId.startsWith(like)].toList
                }
        }
    }

    def protected List<ComponentInfoDTO> reverse(List<ComponentInfoDTO> input, boolean reverse) {
        if (reverse) {
            val tmp = new ArrayList<ComponentInfoDTO>(input);
            Collections.reverse(tmp)
            return tmp
        } else {
            return input
        }
    }

    def protected List<ComponentInfoDTO> applySort(SortColumn it, List<ComponentInfoDTO> input) {
        switch (fieldName) {
            case "groupId":       return input.sortBy[groupId].reverse(descending)
            case "artifactId":    return input.sortBy[artifactId].reverse(descending)
            case "versionString": return input.sortBy[versionString].reverse(descending)
            case "commitId":      return input.sortBy[commitId ?: "x"].reverse(descending)
        }
    }

    override ReadAllResponse<ComponentInfoDTO, NoTracking> execute(RequestContext ctx, ComponentInfoSearchRequest rq) {
        // map generic search filters to the specific parameters
        val cmd = new RetrieveComponentInfoRequest
        val components   = executor.executeSynchronousAndCheckResult(ctx, cmd, RetrieveComponentInfoResponse).components
        val filteredList = if (rq.searchFilter === null)   components   else rq.searchFilter.applyFilters(components)
        val sortedList   = if (rq.sortColumns.nullOrEmpty) filteredList else rq.sortColumns.get(0).applySort(filteredList)
        val limitedList  = exporter.cut(sortedList, rq.offset, rq.limit)
        val dataList     = new ArrayList<DataWithTrackingW<ComponentInfoDTO, NoTracking>>(limitedList.size)
        for (dto: limitedList) {
            val dwt = new DataWithTrackingW<ComponentInfoDTO, NoTracking>
            dwt.data = dto
            dwt.tenantRef = ctx.tenantRef  // avoid having null here
            dataList.add(dwt)
        }
        return exporter.returnOrExport(dataList, rq.getSearchOutputTarget());
    }
}
