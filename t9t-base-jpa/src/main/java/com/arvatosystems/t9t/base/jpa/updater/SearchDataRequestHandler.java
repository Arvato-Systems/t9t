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
package com.arvatosystems.t9t.base.jpa.updater;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.updater.SearchDataRequest;

public class SearchDataRequestHandler extends AbstractDataRequestHandler<SearchDataRequest> {

    @Override
    public ReadAllResponse execute(final RequestContext ctx, final SearchDataRequest request) {
        final SearchCriteria searchRq = (SearchCriteria)(getCrudViewModel(request.getDtoClassCanonicalName()).searchClass.newInstance());
        if (searchRq == null) {
            throw new T9tException(T9tException.UPDATER_NO_SEARCH_REQUEST, request.getDtoClassCanonicalName());
        }
        searchRq.setSearchFilter(request.getFilter());
        return executor.executeSynchronousAndCheckResult(ctx, searchRq, ReadAllResponse.class);
    }
}
