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
package com.arvatosystems.t9t.base.be.search;

import com.arvatosystems.t9t.base.search.ResolveAnyRefRequest;
import com.arvatosystems.t9t.base.search.ResolveAnyRefResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.IAnyKeySearchRegistry;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class ResolveAnyRefRequestHandler extends AbstractReadOnlyRequestHandler<ResolveAnyRefRequest> {
    private final IAnyKeySearchRegistry searchRegistry = Jdp.getRequired(IAnyKeySearchRegistry.class);

    @Override
    public ResolveAnyRefResponse execute(final RequestContext ctx, final ResolveAnyRefRequest request) throws Exception {
        return searchRegistry.performLookup(ctx, request.getRef());
    }
}
