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
package com.arvatosystems.t9t.base.be.search;

import com.arvatosystems.t9t.base.search.GenericTextSearchRequest;
import com.arvatosystems.t9t.base.search.GenericTextSearchResponse;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.ITextSearch;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class GenericTextSearchRequestHandler extends AbstractSearchRequestHandler<GenericTextSearchRequest> {
    private final ITextSearch engine = Jdp.getRequired(ITextSearch.class);

    @Override
    public GenericTextSearchResponse execute(final RequestContext ctx, final GenericTextSearchRequest rq) {
        final GenericTextSearchResponse resp = new GenericTextSearchResponse();
        resp.setResults(engine.search(ctx, rq, rq.getDocumentName(), rq.getResultFieldName()));
        return resp;
    }
}
