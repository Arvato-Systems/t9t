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

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.jpa.IEntityMapper
import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey
import com.arvatosystems.t9t.base.search.ReadAll28Response
import com.arvatosystems.t9t.base.search.Search28Request
import com.arvatosystems.t9t.base.search.SearchFilterMatchTypeEnum
import com.arvatosystems.t9t.base.search.SearchFilterTypeEnum
import com.arvatosystems.t9t.base.search.SearchFilterTypes
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.ISearchTools
import com.arvatosystems.t9t.base.services.ITextSearch
import com.arvatosystems.t9t.base.services.RequestContext
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.SearchFilters
import de.jpaw.bonaparte.core.BonaPortableClass
import de.jpaw.bonaparte.jpa.BonaPersistableKey
import de.jpaw.bonaparte.jpa.BonaPersistableTracking
import de.jpaw.bonaparte.pojos.api.AndFilter
import de.jpaw.bonaparte.pojos.api.FieldFilter
import de.jpaw.bonaparte.pojos.api.LongFilter
import de.jpaw.bonaparte.pojos.api.SearchFilter
import de.jpaw.bonaparte.pojos.api.TrackingBase
import de.jpaw.bonaparte.pojos.apiw.Ref
import de.jpaw.dp.Jdp
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Map
import org.eclipse.xtend.lib.annotations.Data
import de.jpaw.bonaparte.pojos.api.NotFilter

/**
 * The combined search evaluates if we have filter and/or sort criteria for DB and SOLR,
 * and selects an appropriate approach to find the best strategy (at least it attempts to do so, based on the input data).
 *
 * The strategy is as follows:
 * If filter and sort all allow a DB search, then DB search is done.
 * If filter and sort all allow a SOLR search, then SOLR search is done.
 * Otherwise, if the sort is DB only, query by DB and filter via SOLR.
 * Otherwise, sort via SOLR and intersect with DB results.
 *
 * It is assumed that the DB can do all types of sorts, which means we only have
 * - no sort
 * - DB_ONLY sort
 * - BOTH can sort
 * In case of no sort but SOLR filter criteria, the ordering of the SOLR result is preserved because it is assumed that it reflects the phonetic match quality (relevance).
 *
 */
@AddLogger
@Data
class AbstractCombinedTextDatabaseSearch28RequestHandler<REF extends Ref, DTO extends REF, TRACKING extends TrackingBase, REQ extends Search28Request<DTO, TRACKING>, ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>> extends AbstractSearchRequestHandler<REQ> {
    final static int MAX_ITERATIONS = 50;  // limit the number of loop iterations. Will produce results too small, but that is better than choking the system.

    protected final IExecutor     executor    = Jdp.getRequired(IExecutor)
    protected final ISearchTools  searchTools = Jdp.getRequired(ISearchTools)
    protected final ITextSearch   textSearch  = Jdp.getRequired(ITextSearch)
    protected IResolverSurrogateKey<REF, TRACKING, ENTITY> resolver          // set by constructor
    protected IEntityMapper<Long, DTO, TRACKING, ENTITY>   mapper            // set by constructor
    protected final List<SearchTypeMappingEntry> textSearchPathElements      // set by constructor
    protected final Map<String, String>          textSearchFieldMappings     // set by constructor
    protected final String documentName // set by constructor
    protected final String keyFieldName // set by constructor
    protected final BonaPortableClass<Search28Request<DTO, TRACKING>> bclass // set by constructor

    /**
     * List entry for a prefix which defines if a field is SOLR only, DB only, or both search engines can evaluate the field.
     */
    @Data
    static protected class SearchTypeMappingEntry {
        protected final String                      name;       // the path prefix (or substring)
        protected final SearchFilterTypeEnum        searchType; // defines which search type can filter by this expression
        protected final SearchFilterMatchTypeEnum   matchType;  // if true, then the pattern may occur anywhere, normally the path must start with it
    }

    override ReadAll28Response<DTO, TRACKING> execute(RequestContext ctx, REQ rq) {
        // if the search is done by SOLR expression, it can do SOLR only - no other analysis required
        if (!rq.expression.nullOrEmpty) {
            return executeSOLRSearch(ctx, rq)
        }
        val filterTypes = new SearchFilterTypes
        rq.searchFilter?.decideFilterAssociation(filterTypes)
        // determine type of sort as well
        val sortType = if (!rq.sortColumns.nullOrEmpty) filterTypeForField(rq.sortColumns.get(0).fieldName)
        LOGGER.debug("Filters indicate search types {} and sort type {} for expression {}", filterTypes, sortType, rq.searchFilter)

        // If no filter is SOLR only, we run a pure DB search. It is assumed that the DB can process all sort criteria.
        if (!filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY) && sortType != SearchFilterTypeEnum.SOLR_ONLY)
            return executeDBSearch(rq)
        if (!filterTypes.contains(SearchFilterTypeEnum.DB_ONLY) && sortType != SearchFilterTypeEnum.DB_ONLY)
            return executeSOLRSearch(ctx, rq)

        // do combined searches
        val solrRequest = bclass.newInstance
        val dbRequest   = bclass.newInstance
        splitSearches(rq.searchFilter, solrRequest, dbRequest)
        LOGGER.debug("Original request {} split to Solr Rq {} and DB rq {}", rq, solrRequest, dbRequest)
        if (solrRequest.searchFilter === null || dbRequest.searchFilter === null)
            throw new T9tException(T9tException.ILE_SOLR_DB_COMBINED_FILTERS)

        val finalResultList = new ArrayList<ENTITY>(rq.limit)
        if (sortType === null) {
            // must do SOLR driven search in order to get ordering by relevance
            executeBOTHSearchDrivenBySolr(ctx, rq, solrRequest, dbRequest, finalResultList)
        } else if (sortType == SearchFilterTypeEnum.DB_ONLY || (
            !filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY) && !filterTypes.contains(SearchFilterTypeEnum.BOTH)
          )) {
            // must do DB driven search if sort is by DB_ONLY criteria, and should do if no SOLR filter available
            executeBOTHSearchDrivenByDb(ctx, rq, solrRequest, dbRequest, finalResultList)
        } else {
            // default: Solr driven combined query
            executeBOTHSearchDrivenBySolr(ctx, rq, solrRequest, dbRequest, finalResultList)
        }
        return mapper.createReadAllResponse(finalResultList, rq.getSearchOutputTarget());
    }

    def protected ReadAll28Response<DTO, TRACKING> executeDBSearch(REQ rq) {
        // database search
        // preprocess prefixes for DB
        LOGGER.debug("Using DB only search")
        mapper.processSearchPrefixForDB(rq); // convert the field with searchPrefix
        // delegate to database search
        return mapper.createReadAllResponse(resolver.search(rq, null), rq.getSearchOutputTarget());
    }

    def protected ReadAll28Response<DTO, TRACKING> executeSOLRSearch(RequestContext ctx, REQ rq) {
        // preprocess args for SOLR
        searchTools.mapNames(rq, textSearchFieldMappings)
        LOGGER.debug("SOLR request mapped: {}", rq)
        val refs = textSearch.search(ctx, rq, documentName, keyFieldName)

        // end here if there are no results - DB query would return an error
        if (refs.isEmpty)
            return new ReadAll28Response<DTO, TRACKING> => [
                dataList = #[]
            ]
        // obtain DTOs for the refs
        val newSearchRq = bclass.newInstance => [
            searchFilter = new LongFilter("objectRef", null, null, null, refs)
            sortColumns = rq.sortColumns
            searchOutputTarget = rq.searchOutputTarget
        ]
        LOGGER.debug("solr only new searchReq: {}", newSearchRq)
        mapper.processSearchPrefixForDB(newSearchRq); // convert the field with searchPrefix
        return mapper.createReadAllResponse(resolver.search(newSearchRq, null), newSearchRq.getSearchOutputTarget());
    }

    // when this method is called, there are filter criteria which are SOLR only as well as some which are DB only, or DB only sort criteria
    def protected void executeBOTHSearchDrivenBySolr(RequestContext ctx, REQ rq,
        Search28Request<DTO,TRACKING> solrRequest, Search28Request<DTO,TRACKING> dbRequest, ArrayList<ENTITY> finalResultList
    ) {
        LOGGER.debug("SOLR driven combined search performed")
        val byRelevance = rq.sortColumns.nullOrEmpty;
        solrRequest.sortColumns = rq.sortColumns     // we know it is not DB_ONLY if we are here
        dbRequest.sortColumns = rq.sortColumns       // ask DB to sort by same
        dbRequest.limit = rq.limit
        val dbRequestFilter = dbRequest.searchFilter // save for later use
        searchTools.mapNames(solrRequest, textSearchFieldMappings)
        mapper.processSearchPrefixForDB(dbRequest);

        var resultsToSkip = rq.offset
        var increasedLimit = Math.min(rq.limit*4, 1000)  // use a higher limit because we expect to lose some when intersecting with the DB results, but respect Oracle's limitations
        var iteration = 0
        var boolean doMoreSearchRequests = true  // set to false once we know SOLR will not return more results
        var int foundResults = 0
        solrRequest.limit = increasedLimit

        // iterate SOLR requests until we have enough data
        while (doMoreSearchRequests && foundResults < rq.limit) {
            solrRequest.offset = increasedLimit * iteration // consecutively increase the solr offset to include more results if they are needed

            val refs = textSearch.search(ctx, solrRequest, documentName, keyFieldName)
            if (refs.size < increasedLimit) {
                doMoreSearchRequests = false  // end of data
            }

            // just return finalResultList if no more refs can be found with the given offset/limit (shortcut)
            if (refs.isEmpty)
                return;
            dbRequest.searchFilter = new AndFilter(dbRequestFilter, new LongFilter => [
                fieldName = "objectRef"
                valueList = refs
            ])
             // only query INITIAL_CALLER_LIMIT - RESULTS_FOUND + RESULTS_TO_SKIP. This will ensure we always get enough because a certain amount has to be skipped
            dbRequest.limit = rq.limit - foundResults + resultsToSkip

            // execute search and save result
            var tempResult = resolver.search(dbRequest, null)

            if (resultsToSkip > 0) { // if the caller supplied an offset we have to skip the first X (offset) results
                if (resultsToSkip >= tempResult.size) {
                    // skip everything!
                    resultsToSkip -= tempResult.size
                } else {
                    // skip some, add some
                    val sizeToAdd = Math.min(tempResult.size - resultsToSkip, rq.limit - foundResults)
                    xfer(refs, finalResultList, tempResult.subList(resultsToSkip, sizeToAdd), byRelevance)
                    foundResults += sizeToAdd
                    resultsToSkip = 0
                }
            } else {
                // add everything we got (this could be an empty list)
                if (tempResult.size <= rq.limit - foundResults) {
                    // add everything we got
                    xfer(refs, finalResultList, tempResult, byRelevance)
                    foundResults += tempResult.size // after dropping enough from the caller's offset we can save the results
                } else {
                    // add the missing entries and we are done
                    xfer(refs, finalResultList, tempResult.subList(0, rq.limit - foundResults), byRelevance)
                    foundResults = rq.limit
                }
            }
            if (iteration++ >= MAX_ITERATIONS) { // increase iteration count, and check for security stop
                LOGGER.warn("combined search prematurely stopped due to iteration count limit")
                doMoreSearchRequests = false
            }
        }
    }

    // add the entities of the temporary result in order, or, if that search was done without sort, in order of the input refs
    def protected void xfer(List<Long> orderForNoSort, ArrayList<ENTITY> finalResultList, List<ENTITY> temp, boolean byRelevance) {
        if (!byRelevance)
            finalResultList.addAll(temp)
        else {
            // build a map
            val indexMap = new HashMap<Long, ENTITY>(2 * temp.size)
            for (e: temp)
                indexMap.put(e.ret$Key, e)
            for (r: orderForNoSort) {
                val ENTITY ee = indexMap.get(r)
                if (ee !== null)
                    finalResultList.add(ee)
            }
        }
    }

    def protected void executeBOTHSearchDrivenByDb(RequestContext ctx, REQ rq,
        Search28Request<DTO,TRACKING> solrRequest, Search28Request<DTO,TRACKING> dbRequest, ArrayList<ENTITY> finalResultList
    ) {
        LOGGER.debug("DB driven combined search performed")
        dbRequest.sortColumns = rq.sortColumns       // ask (only) DB to sort
        solrRequest.limit = rq.limit
        val solrRequestFilter = dbRequest.searchFilter // save for later use
        searchTools.mapNames(solrRequest, textSearchFieldMappings)
        mapper.processSearchPrefixForDB(dbRequest);

        var resultsToSkip = rq.offset
        var increasedLimit = Math.min(rq.limit*4, 1000)  // use a higher limit because we expect to lose some when intersecting with the DB results, but respect Oracle's limitations
        var iteration = 0
        var boolean doMoreSearchRequests = true  // set to false once we know SOLR will not return more results
        var int foundResults = 0
        dbRequest.limit = increasedLimit

        // iterate SOLR requests until we have enough data
        while (doMoreSearchRequests && foundResults < rq.limit) {
            dbRequest.offset = increasedLimit * iteration // consecutively increase the solr offset to include more results if they are needed

            val refs = resolver.searchKey(dbRequest)
            if (refs.size < increasedLimit) {
                doMoreSearchRequests = false  // end of data
            }

            // just return finalResultList if no more refs can be found with the given offset/limit (shortcut)
            if (refs.isEmpty)
                return;
            solrRequest.searchFilter = new AndFilter(solrRequestFilter, new LongFilter => [
                fieldName = "objectRef"
                valueList = refs
            ])
             // only query INITIAL_CALLER_LIMIT - RESULTS_FOUND + RESULTS_TO_SKIP. This will ensure we always get enough because a certain amount has to be skipped
            solrRequest.limit = rq.limit - foundResults + resultsToSkip

            // execute search and save result
            val tempResult = textSearch.search(ctx, solrRequest, documentName, keyFieldName)

            if (resultsToSkip > 0) { // if the caller supplied an offset we have to skip the first X (offset) results
                if (resultsToSkip >= tempResult.size) {
                    // skip everything!
                    resultsToSkip -= tempResult.size
                } else {
                    // skip some, add some
                    val sizeToAdd = Math.min(tempResult.size - resultsToSkip, rq.limit - foundResults)
                    xfer(refs, finalResultList, tempResult.subList(resultsToSkip, sizeToAdd))
                    foundResults += sizeToAdd
                    resultsToSkip = 0
                }
            } else {
                // add everything we got (this could be an empty list)
                if (tempResult.size <= rq.limit - foundResults) {
                    // add everything we got
                    xfer(refs, finalResultList, tempResult)
                    foundResults += tempResult.size // after dropping enough from the caller's offset we can save the results
                } else {
                    // add the missing entries and we are done
                    xfer(refs, finalResultList, tempResult.subList(0, rq.limit - foundResults))
                    foundResults = rq.limit
                }
            }
            if (iteration++ >= MAX_ITERATIONS) { // increase iteration count, and check for security stop
                LOGGER.warn("combined search prematurely stopped due to iteration count limit")
                doMoreSearchRequests = false
            }
        }
    }

    // add the entities of the temporary result in order, or, if that search was done without sort, in order of the input refs
    def protected void xfer(List<Long> orderForNoSort, ArrayList<ENTITY> finalResultList, List<Long> temp) {
        // build a set from the list entries
        val indexMap = new HashSet<Long>(2 * temp.size)
        indexMap.addAll(temp)
        val em = resolver.entityManager
        for (r: orderForNoSort) {
            if (indexMap.contains(r)) {
                val ENTITY ee = em.find(resolver.baseJpaEntityClass, r)
                if (ee === null) {
                    LOGGER.warn("Could not find entity of key {} any more!", r)
                } else {
                    finalResultList.add(ee)
                }
            }
        }
    }

    /**
     * Method splits the searchFilters in the original request into two requests that are solr only and dbOnly
     */
    def protected void splitSearches(SearchFilter originalFilter, Search28Request<DTO, TRACKING> solrRequest, Search28Request<DTO, TRACKING> dbRequest) {
        if (originalFilter instanceof FieldFilter) {
            switch (filterTypeForField(originalFilter.fieldName)) {
                case DB_ONLY:   dbRequest.searchFilter   = SearchFilters.and(dbRequest.searchFilter, originalFilter)
                case SOLR_ONLY: solrRequest.searchFilter = SearchFilters.and(solrRequest.searchFilter, originalFilter)
                default: {  // merge the filter into BOTH searches!
                    dbRequest.searchFilter   = SearchFilters.and(dbRequest.searchFilter, originalFilter)
                    solrRequest.searchFilter = SearchFilters.and(solrRequest.searchFilter, originalFilter)
                }
            }
        } else if (originalFilter instanceof AndFilter) {
            splitSearches(originalFilter.filter1, solrRequest, dbRequest)
            splitSearches(originalFilter.filter2, solrRequest, dbRequest)
        }
    }

    /**
     * Determines which search engine(s) can process all filters. Also validates that only AND conditions are used.
     * For performance reasons, the result is passed in by reference (avoids GC overhead due to temp object constructions).
     * The passed in parameters may not be null.
     */
    def protected void decideFilterAssociation(SearchFilter originalFilter, SearchFilterTypes result) {
        if (originalFilter instanceof FieldFilter) {
            result.add(filterTypeForField(originalFilter.fieldName))
        } else if (originalFilter instanceof NotFilter) {
            decideFilterAssociation(originalFilter.filter, result)
        } else if (originalFilter instanceof AndFilter) {
            decideFilterAssociation(originalFilter.filter1, result)
            decideFilterAssociation(originalFilter.filter2, result)
        } else {
            throw new T9tException(T9tException.ILLEGAL_SOLR_DB_COMBINED_FILTER_EXPRESSION, originalFilter.ret$PQON)
        }
    }

    /**
     * Determines which search engine(s) can process a certain field.
     * The data is available in the list textSearchPathElements. If no entry can be found, a default of DBONLY is assumed.
     * Never returns null.
     */
    def protected SearchFilterTypeEnum filterTypeForField(String path) {
        val entry = textSearchPathElements.findFirst[thatMatches(path)]
        if (entry === null)
            return SearchFilterTypeEnum.DB_ONLY
        return entry.searchType
    }

    /** Returns true if path matches the mapping entry, depending on type of match, otherwise false. */
    def private boolean thatMatches(SearchTypeMappingEntry it, String path) {
        switch (matchType) {
        case EXACT:     return path == name
        case START:     return path.startsWith(name)
        case SUBSTRING: return path.contains(name)
        }
        return false
    }
}
