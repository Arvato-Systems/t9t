/**
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
package com.arvatosystems.t9t.base.jpa.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.arvatosystems.t9t.base.jpa.IEntityMapper42;
import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey42;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SearchRequest;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.ISearchTools;
import com.arvatosystems.t9t.base.services.ITextSearch;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;

public abstract class AbstractTextSearchRequestHandler<REF extends Ref, DTO extends REF, TRACKING extends TrackingBase, REQ extends SearchRequest<DTO, TRACKING>, ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>>
        extends AbstractSearchRequestHandler<REQ> {
    protected final IExecutor executor = Jdp.<IExecutor>getRequired(IExecutor.class);
    protected final ISearchTools searchTools = Jdp.<ISearchTools>getRequired(ISearchTools.class);
    protected final ITextSearch textSearch = Jdp.<ITextSearch>getRequired(ITextSearch.class);
    protected final IResolverSurrogateKey42<REF, TRACKING, ENTITY> resolver;
    protected final IEntityMapper42<Long, DTO, TRACKING, ENTITY> mapper;
    protected final List<String> textSearchOnlyPathElements;
    protected final Map<String, String> textSearchFieldMappings;
    protected final String documentName;
    protected final String keyFieldName;
    protected final BonaPortableClass<SearchRequest<DTO, TRACKING>> bclass;

    public AbstractTextSearchRequestHandler(final IResolverSurrogateKey42<REF, TRACKING, ENTITY> resolver,
            final IEntityMapper42<Long, DTO, TRACKING, ENTITY> mapper, final List<String> textSearchOnlyPathElements,
            final Map<String, String> textSearchFieldMappings, final String documentName, final String keyFieldName,
            final BonaPortableClass<SearchRequest<DTO, TRACKING>> bclass) {
        super();
        this.resolver = resolver;
        this.mapper = mapper;
        this.textSearchOnlyPathElements = textSearchOnlyPathElements;
        this.textSearchFieldMappings = textSearchFieldMappings;
        this.documentName = documentName;
        this.keyFieldName = keyFieldName;
        this.bclass = bclass;
    }

    @Override
    public ReadAllResponse<DTO, TRACKING> execute(final RequestContext ctx, final REQ rq) throws Exception {
        if (!(rq.getExpression() == null || rq.getExpression().isEmpty()) || searchTools.containsFieldPathElements(rq, textSearchOnlyPathElements)) {
            // preprocess args for SOLR
            this.searchTools.mapNames(rq, this.textSearchFieldMappings);
            final List<Long> refs = this.textSearch.search(ctx, rq, this.documentName, this.keyFieldName);

            // end here if there are no results - DB query would return an error
            if (refs.isEmpty()) {
                final ReadAllResponse<DTO, TRACKING> resp = new ReadAllResponse<DTO, TRACKING>();
                resp.setDataList(Collections.<DataWithTrackingW<DTO, TRACKING>>emptyList());
                return resp;
            }
            // obtain DTOs for the refs
            final LongFilter longFilter = new LongFilter("objectRef");
            longFilter.setValueList(refs);
            final SearchRequest<DTO, TRACKING> newSearchRq = this.bclass.newInstance();
            newSearchRq.setSearchFilter(longFilter);
            newSearchRq.setSortColumns(rq.getSortColumns());
            newSearchRq.setSearchOutputTarget(rq.getSearchOutputTarget());

            this.mapper.processSearchPrefixForDB(newSearchRq);       // convert the field with searchPrefix
            return this.mapper.createReadAllResponse(this.resolver.search(newSearchRq, null), newSearchRq.getSearchOutputTarget());
        } else {
            // database search

            // preprocess prefixes for DB
            this.mapper.processSearchPrefixForDB(rq);
            // delegate to database search
            return this.mapper.createReadAllResponse(this.resolver.search(rq, null), rq.getSearchOutputTarget());
        }
    }
}
