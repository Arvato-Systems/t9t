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
package com.arvatosystems.t9t.base.services;

import com.arvatosystems.t9t.base.search.SearchCriteria;

import de.jpaw.bonaparte.pojos.api.OperationType;

public abstract class AbstractSearchRequestHandler<REQUEST extends SearchCriteria> extends AbstractRequestHandler<REQUEST> {

    @Override
    public boolean isReadOnly(final REQUEST params) {
        return params.getSearchOutputTarget() == null;  // a search request is "read only" if the result is not redirected to a data sink
    }

    @Override
    public OperationType getAdditionalRequiredPermission(final REQUEST request) {
        // must have permission EXPORT for output to data sink
        return request.getSearchOutputTarget() != null ? OperationType.EXPORT : OperationType.SEARCH;
    }
}
