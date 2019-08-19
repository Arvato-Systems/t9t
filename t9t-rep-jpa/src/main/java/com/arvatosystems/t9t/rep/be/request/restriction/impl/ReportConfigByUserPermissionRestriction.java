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
package com.arvatosystems.t9t.rep.be.request.restriction.impl;

import java.util.List;

import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.rep.be.request.restriction.IReportConfigByUserPermissionRestriction;
import com.arvatosystems.t9t.rep.be.request.restriction.IReportConfigResolverRestriction;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * Implementation of {@linkplain IReportConfigByUserPermissionRestriction}
 * @author RREN001
 *
 * No report assigned means no restriction (forbid the screens if no permissions at all).
 */
@Singleton
public class ReportConfigByUserPermissionRestriction implements IReportConfigByUserPermissionRestriction {
    protected final IReportConfigResolverRestriction reportConfigRestriction = Jdp.getRequired(IReportConfigResolverRestriction.class);

    @Override
    public void apply(RequestContext ctx, SearchCriteria searchRequest) {
        List<String> permittedReportIds = reportConfigRestriction.getPermissionIdList(ctx);

        if (permittedReportIds == null || permittedReportIds.isEmpty())
            return;

        AsciiFilter idFilter = new AsciiFilter("reportConfigId");
        if (permittedReportIds.size() == 1)
            idFilter.setEqualsValue(permittedReportIds.get(0));  // single entry => use equals
        else
            idFilter.setValueList(permittedReportIds);           // multiple entry => use IN
        searchRequest.setSearchFilter(SearchFilters.and(searchRequest.getSearchFilter(), idFilter));
    }
}
