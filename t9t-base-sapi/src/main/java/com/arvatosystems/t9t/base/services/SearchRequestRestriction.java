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
package com.arvatosystems.t9t.base.services;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.search.SearchRequest;

/**
 * Define restriction that can be applied for a particular {@linkplain SearchRequest}.
 * @author LIEE001
 */
public interface SearchRequestRestriction {

    /**
     * Apply search restriction to the request.
     * @param searchRequest target search request
     * @throws T9tException if any error accessing persistence
     */
    void apply(RequestContext ctx, SearchCriteria searchRequest);
}
