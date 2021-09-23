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
package com.arvatosystems.t9t.base.services;

import java.util.List;
import java.util.function.BiFunction;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.ResolveAnyRefResponse;

/**
 * Interface to be used to search an entity by ref, when the type of the data is not known.
 */
public interface IAnyKeySearchRegistry {

    /** Register a new LeanSearchRequestHandler, to resolve a ref of specific RTTI. */
    void registerLeanSearchRequest(BiFunction<RequestContext, Long, List<Description>> resolver, int rtti, String classname);

    /** Apply a search. */
    ResolveAnyRefResponse performLookup(RequestContext ctx, Long ref);
}
