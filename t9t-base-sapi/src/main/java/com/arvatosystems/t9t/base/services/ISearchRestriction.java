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

import java.util.List;

import com.arvatosystems.t9t.base.search.SearchCriteria;

/** Provides cached access to search restrictions, for Ref based JPA entities.
 * Implementations are singletons, qualified by some name.
 * Currently implementations exist for location and orgUnit. */
public interface ISearchRestriction {

    /** retrieves the allowed refs, or an empty list of no restrictions exist.
     * The entries are cached for about 15 minutes.
     */
    List<Long> retrieveAllowedRefsCached(RequestContext ctx);

    /** retrieves the allowed refs, or an empty list of no restrictions exist.
     * The entries are obtained from the JWT or the DB.
     */
    List<Long> retrieveAllowedRefsUncached(RequestContext ctx);

    /** Apply search restrictions to some existing search request, for a given number of fields. */
    void addRestrictionsForFields(RequestContext ctx, SearchCriteria srq, List<String> pathnames);

    /** Apply search restrictions to some existing search request, for a given number of fields.
     * The required fields must match, the optional fields are also accepted if null. */
    void addRestrictionsForFields(RequestContext ctx, SearchCriteria srq, List<String> requiredFields, List<String> optionalFields);
}
