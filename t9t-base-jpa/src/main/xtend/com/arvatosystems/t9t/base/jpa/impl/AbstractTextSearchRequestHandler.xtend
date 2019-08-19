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
package com.arvatosystems.t9t.base.jpa.impl

import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey42
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.base.search.SearchRequest
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.ISearchTools
import com.arvatosystems.t9t.base.services.ITextSearch
import com.arvatosystems.t9t.base.services.RequestContext
import de.jpaw.bonaparte.core.BonaPortableClass
import de.jpaw.bonaparte.jpa.BonaPersistableKey
import de.jpaw.bonaparte.jpa.BonaPersistableTracking
import de.jpaw.bonaparte.pojos.api.LongFilter
import de.jpaw.bonaparte.pojos.api.TrackingBase
import de.jpaw.bonaparte.pojos.apiw.Ref
import de.jpaw.dp.Jdp
import java.util.List
import java.util.Map
import org.eclipse.xtend.lib.annotations.Data
import com.arvatosystems.t9t.base.jpa.IEntityMapper42

@Data
class AbstractTextSearchRequestHandler<
    REF extends Ref,
    DTO extends REF,
    TRACKING extends TrackingBase,
    REQ extends SearchRequest<DTO, TRACKING>,
    ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>>
  extends AbstractSearchRequestHandler<REQ> {

    protected final IExecutor                 executor    = Jdp.getRequired(IExecutor)
    protected final ISearchTools              searchTools = Jdp.getRequired(ISearchTools)
    protected final ITextSearch               textSearch  = Jdp.getRequired(ITextSearch)
    // @Inject protected Provider<IOutputSession>      outputSessionProvider

    protected IResolverSurrogateKey42<REF, TRACKING, ENTITY>    resolver
    protected IEntityMapper42    <Long, DTO, TRACKING, ENTITY>    mapper
    protected List<String>                                      textSearchOnlyPathElements
    protected Map<String, String>                               textSearchFieldMappings
    protected String                                            documentName
    protected String                                            keyFieldName
    protected BonaPortableClass<SearchRequest<DTO, TRACKING>>   bclass

    override ReadAllResponse<DTO, TRACKING> execute(RequestContext ctx, REQ rq) {

        if (!rq.expression.nullOrEmpty || searchTools.containsFieldPathElements(rq, textSearchOnlyPathElements)) {
            // preprocess args for SOLR
            searchTools.mapNames(rq, textSearchFieldMappings)
            val refs = textSearch.search(ctx, rq, documentName, keyFieldName)

            // end here if there are no results - DB query would return an error
            if (refs.isEmpty)
                return new ReadAllResponse<DTO, TRACKING> => [
                    dataList = #[]
                ]
            // obtain Customer DTOs for the refs
            val newSearchRq         = bclass.newInstance => [
                searchFilter        = new LongFilter("objectRef", null, null, null, refs)
                sortColumns         = rq.sortColumns
                searchOutputTarget  = rq.searchOutputTarget
            ]
            mapper.processSearchPrefixForDB(newSearchRq);       // convert the field with searchPrefix
            return mapper.createReadAllResponse(resolver.search(newSearchRq, null), newSearchRq.getSearchOutputTarget());

        } else {
            // database search

            // preprocess prefixes for DB
            mapper.processSearchPrefixForDB(rq);       // convert the field with searchPrefix
            // delegate to database search
            return mapper.createReadAllResponse(resolver.search(rq, null), rq.getSearchOutputTarget());
        }
    }
}
