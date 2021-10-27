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
package com.arvatosystems.t9t.base.jpa.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.persistence.EntityManager;

import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

///*
//* Copyright (c) 2012 - 2020 Arvato Systems GmbH
//*
//* Licensed under the Apache License, Version 2.0 (the "License");
//* you may not use this file except in compliance with the License.
//* You may obtain a copy of the License at
//*
//*     http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing, software
//* distributed under the License is distributed on an "AS IS" BASIS,
//* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//* See the License for the specific language governing permissions and
//* limitations under the License.
//*/
//package com.arvatosystems.t9t.base.jpa.impl
//
//import com.arvatosystems.t9t.base.T9tException
//import com.arvatosystems.t9t.base.jpa.IEntityMapper42
//import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey42
//import com.arvatosystems.t9t.base.search.ReadAllResponse
//import com.arvatosystems.t9t.base.search.SearchFilterTypeEnum
//import com.arvatosystems.t9t.base.search.SearchFilterTypes
//import com.arvatosystems.t9t.base.search.SearchRequest
//import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler
//import com.arvatosystems.t9t.base.services.IExecutor
//import com.arvatosystems.t9t.base.services.ISearchTools
//import com.arvatosystems.t9t.base.services.ITextSearch
//import com.arvatosystems.t9t.base.services.RequestContext
//import de.jpaw.annotations.AddLogger
//import de.jpaw.bonaparte.api.SearchFilters
//import de.jpaw.bonaparte.core.BonaPortableClass
//import de.jpaw.bonaparte.jpa.BonaPersistableKey
//import de.jpaw.bonaparte.jpa.BonaPersistableTracking
//import de.jpaw.bonaparte.pojos.api.AndFilter
//import de.jpaw.bonaparte.pojos.api.FieldFilter
//import de.jpaw.bonaparte.pojos.api.LongFilter
//import de.jpaw.bonaparte.pojos.api.SearchFilter
//import de.jpaw.bonaparte.pojos.api.TrackingBase
//import de.jpaw.bonaparte.pojos.apiw.Ref
//import de.jpaw.dp.Jdp
//import java.util.ArrayList
//import java.util.HashMap
//import java.util.List
//import java.util.Map
//import org.eclipse.xtend.lib.annotations.Data
//import java.util.HashSet
//import com.arvatosystems.t9t.base.search.SearchFilterMatchTypeEnum
//import de.jpaw.bonaparte.pojos.api.NotFilter
//import com.arvatosystems.t9t.base.search.SearchCriteria
//
///**
//* The combined search evaluates if we have filter and/or sort criteria for DB and SOLR,
//* and selects an appropriate approach to find the best strategy (at least it attempts to do so, based on the input data).
//*
//* The strategy is as follows:
//* If filter and sort all allow a DB search, then DB search is done.
//* If filter and sort all allow a SOLR search, then SOLR search is done.
//* Otherwise, if the sort is DB only, query by DB and filter via SOLR.
//* Otherwise, sort via SOLR and intersect with DB results.
//*
//* It is assumed that the DB can do all types of sorts, which means we only have
//* - no sort
//* - DB_ONLY sort
//* - BOTH can sort
//* In case of no sort but SOLR filter criteria, the ordering of the SOLR result is preserved because it is assumed that it reflects the phonetic match quality (relevance).
//*
//*/
//@AddLogger
//@Data
//abstract class AbstractCombinedTextDatabaseSearchRequestHandler<REF extends Ref, DTO extends REF, TRACKING extends TrackingBase,
//REQ extends SearchRequest<DTO, TRACKING>, ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>
//> extends AbstractSearchRequestHandler<REQ> {
// final static int MAX_ITERATIONS = 50;  // limit the number of loop iterations. Will produce results too small, but that is better than choking the system.
//
// protected final IExecutor     executor    = Jdp.getRequired(IExecutor)
// protected final ISearchTools  searchTools = Jdp.getRequired(ISearchTools)
// protected final ITextSearch   textSearch  = Jdp.getRequired(ITextSearch)
// protected final IResolverSurrogateKey42<REF, TRACKING, ENTITY> resolver  // set by constructor
// protected final IEntityMapper42<Long, DTO, TRACKING, ENTITY>   mapper    // set by constructor
// protected final List<SearchTypeMappingEntry> textSearchPathElements      // set by constructor
// protected final Map<String, String>          textSearchFieldMappings     // set by constructor
// protected final String documentName // set by constructor
// protected final String keyFieldName // set by constructor
// protected final BonaPortableClass<SearchRequest<DTO, TRACKING>> bclass // set by constructor
//
// // optional additional mappings not replaced by processSearchPrefixForDB (due to child objects)
// // workaround.
// def protected Map<String,String> getExtraMappings() {
//     return null
// }
//
// def protected void processPrefixes(SearchCriteria rq) {
//     mapper.processSearchPrefixForDB(rq); // convert the field with searchPrefix
//     // the regular mapper is not sufficient here, because for SOLR searches, we have frequent occurrences of double Refs.
//     // we perform a hack here, replacing remaining occurrences of "Ref." by ".", which is the correct replacement in most cases.
//     val mappings = extraMappings
//     if (extraMappings !== null) {
//         searchTools.mapNames(rq, [ path |
//             if (path.contains("Ref.")) {  // upfront check for performance: by convention, any references end with "Ref"
//                 var String tmp = path
//                 for (r: mappings.entrySet)
//                     tmp = tmp.replace(r.key, r.value)
//                 return tmp
//             } else {
//                 return path  // unchanged
//             }
//         ])
//     }
// }
//
// /**
//  * List entry for a prefix which defines if a field is SOLR only, DB only, or both search engines can evaluate the field.
//  */
// @Data
// static protected class SearchTypeMappingEntry {
//     protected final String                      name;       // the path prefix (or substring)
//     protected final SearchFilterTypeEnum        searchType; // defines which search type can filter by this expression
//     protected final SearchFilterMatchTypeEnum   matchType;  // if true, then the pattern may occur anywhere, normally the path must start with it
// }
//
// override ReadAllResponse<DTO, TRACKING> execute(RequestContext ctx, REQ rq) {
//     // if the search is done by SOLR expression, it can do SOLR only - no other analysis required
//     if (!rq.expression.nullOrEmpty) {
//         return executeSOLRSearch(ctx, rq)
//     }
//     val filterTypes = new SearchFilterTypes
//     rq.searchFilter?.decideFilterAssociation(filterTypes)
//     // determine type of sort as well
//     val sortType = if (!rq.sortColumns.nullOrEmpty) filterTypeForField(rq.sortColumns.get(0).fieldName)
//     LOGGER.debug("Filters indicate search types {} and sort type {} for expression {}", filterTypes, sortType, rq.searchFilter)
//
//     // If no filter is SOLR only, we run a pure DB search. It is assumed that the DB can process all sort criteria.
//     if (!filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY) && sortType != SearchFilterTypeEnum.SOLR_ONLY)
//         return executeDBSearch(rq)
//     if (!filterTypes.contains(SearchFilterTypeEnum.DB_ONLY) && sortType != SearchFilterTypeEnum.DB_ONLY)
//         return executeSOLRSearch(ctx, rq)
//
//     // do combined searches
//     val solrRequest = bclass.newInstance
//     val dbRequest   = bclass.newInstance
//     splitSearches(rq.searchFilter, solrRequest, dbRequest)
//     LOGGER.debug("Original request {} split to Solr Rq {} and DB rq {}", rq, solrRequest, dbRequest)
//     if (solrRequest.searchFilter === null && dbRequest.searchFilter === null)
//         throw new T9tException(T9tException.ILE_SOLR_DB_COMBINED_FILTERS)
//
//     val finalResultList = new ArrayList<ENTITY>(rq.limit)
//     if (sortType === null) {
//         // must do SOLR driven search in order to get ordering by relevance
//         executeBOTHSearchDrivenBySolr(ctx, rq, solrRequest, dbRequest, finalResultList)
//     } else if (sortType == SearchFilterTypeEnum.DB_ONLY && filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY) && dbRequest.searchFilter === null) {
//         // the UNSOLVABLE one: all filters are SOLR, but we have to sort be DB.
//         // this only works if we can get all results at once
//         return executeSOLRSearchWithDbSort(ctx, rq)
//     } else if (sortType == SearchFilterTypeEnum.DB_ONLY || (
//         !filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY) && !filterTypes.contains(SearchFilterTypeEnum.BOTH)
//       )) {
//         // must do DB driven search if sort is by DB_ONLY criteria, and should do if no SOLR filter available
//         executeBOTHSearchDrivenByDb(ctx, rq, solrRequest, dbRequest, finalResultList)
//     } else {
//         // default: Solr driven combined query
//         executeBOTHSearchDrivenBySolr(ctx, rq, solrRequest, dbRequest, finalResultList)
//     }
//     return mapper.createReadAllResponse(finalResultList, rq.getSearchOutputTarget());
// }
//
// def protected ReadAllResponse<DTO, TRACKING> executeDBSearch(REQ rq) {
//     // database search
//     // preprocess prefixes for DB
//     LOGGER.debug("Using DB only search")
//     processPrefixes(rq)
//     // delegate to database search
//     return mapper.createReadAllResponse(resolver.search(rq, null), rq.getSearchOutputTarget());
// }
//
// def protected ReadAllResponse<DTO, TRACKING> executeSOLRSearch(RequestContext ctx, REQ rq) {
//     // preprocess args for SOLR
//     searchTools.mapNames(rq, textSearchFieldMappings)
//     LOGGER.debug("SOLR request mapped: {}", rq)
//     val refs = textSearch.search(ctx, rq, documentName, keyFieldName)
//
//     // end here if there are no results - DB query would return an error
//     if (refs.isEmpty)
//         return new ReadAllResponse<DTO, TRACKING> => [
//             dataList = #[]
//         ]
//     // obtain DTOs for the refs
//     val newSearchRq = bclass.newInstance => [
//         searchFilter = new LongFilter("objectRef", null, null, null, refs)
//         sortColumns = rq.sortColumns
//         searchOutputTarget = rq.searchOutputTarget
//     ]
//     LOGGER.debug("solr only new searchReq: {}", newSearchRq)
//     processPrefixes(newSearchRq); // convert the field with searchPrefix
//     return mapper.createReadAllResponse(resolver.search(newSearchRq, null), newSearchRq.getSearchOutputTarget());
// }
//
// def protected ReadAllResponse<DTO, TRACKING> executeSOLRSearchWithDbSort(RequestContext ctx, REQ rq) {
//     // preprocess args for SOLR
//     val sortCols = rq.sortColumns
//     rq.sortColumns = null
//     searchTools.mapNames(rq, textSearchFieldMappings)
//     LOGGER.debug("SOLR request mapped: {}", rq)
//     val refs = textSearch.search(ctx, rq, documentName, keyFieldName)
//
//     // end here if there are no results - DB query would return an error
//     if (refs.isEmpty)
//         return new ReadAllResponse<DTO, TRACKING> => [
//             dataList = #[]
//         ]
//     // obtain DTOs for the refs
//     val newSearchRq = bclass.newInstance => [
//         searchFilter = new LongFilter("objectRef", null, null, null, refs)
//         sortColumns  = sortCols
//         searchOutputTarget = rq.searchOutputTarget
//     ]
//     LOGGER.debug("solr only with DB sort new searchReq for DB: {}", newSearchRq)
//     processPrefixes(newSearchRq); // convert the field with searchPrefix
//     return mapper.createReadAllResponse(resolver.search(newSearchRq, null), newSearchRq.getSearchOutputTarget());
// }
//
// // when this method is called, there are filter criteria which are SOLR only as well as some which are DB only, or DB only sort criteria
// def protected void executeBOTHSearchDrivenBySolr(RequestContext ctx, REQ rq,
//     SearchRequest<DTO,TRACKING> solrRequest, SearchRequest<DTO,TRACKING> dbRequest, ArrayList<ENTITY> finalResultList
// ) {
//     LOGGER.debug("SOLR driven combined search performed")
//     val byRelevance = rq.sortColumns.nullOrEmpty;
//     solrRequest.sortColumns = rq.sortColumns     // we know it is not DB_ONLY if we are here
//     dbRequest.sortColumns = rq.sortColumns       // ask DB to sort by same
//     dbRequest.limit = rq.limit
//     val dbRequestFilter = dbRequest.searchFilter // save for later use
//     searchTools.mapNames(solrRequest, textSearchFieldMappings)
//     processPrefixes(dbRequest);
//
//     var resultsToSkip = rq.offset
//     var increasedLimit = Math.min(rq.limit*4, 1000)  // use a higher limit because we expect to lose some when intersecting with the DB results, but respect Oracle's limitations
//     var iteration = 0
//     var boolean doMoreSearchRequests = true  // set to false once we know SOLR will not return more results
//     var int foundResults = 0
//     solrRequest.limit = increasedLimit
//
//     // iterate SOLR requests until we have enough data
//     while (doMoreSearchRequests && foundResults < rq.limit) {
//         solrRequest.offset = increasedLimit * iteration // consecutively increase the solr offset to include more results if they are needed
//
//         val refs = textSearch.search(ctx, solrRequest, documentName, keyFieldName)
//         if (refs.size < increasedLimit) {
//             doMoreSearchRequests = false  // end of data
//         }
//
//         // just return finalResultList if no more refs can be found with the given offset/limit (shortcut)
//         if (refs.isEmpty)
//             return;
//         dbRequest.searchFilter = new AndFilter(dbRequestFilter, new LongFilter => [
//             fieldName = "objectRef"
//             valueList = refs
//         ])
//          // only query INITIAL_CALLER_LIMIT - RESULTS_FOUND + RESULTS_TO_SKIP. This will ensure we always get enough because a certain amount has to be skipped
//         dbRequest.limit = rq.limit - foundResults + resultsToSkip
//
//         // execute search and save result
//         var tempResult = resolver.search(dbRequest, null)
//
//         if (resultsToSkip > 0) { // if the caller supplied an offset we have to skip the first X (offset) results
//             if (resultsToSkip >= tempResult.size) {
//                 // skip everything!
//                 resultsToSkip -= tempResult.size
//             } else {
//                 // skip some, add some
//                 val sizeToAdd = Math.min(tempResult.size - resultsToSkip, rq.limit - foundResults)
//                 xfer(refs, finalResultList, tempResult.subList(resultsToSkip, sizeToAdd), byRelevance)
//                 foundResults += sizeToAdd
//                 resultsToSkip = 0
//             }
//         } else {
//             // add everything we got (this could be an empty list)
//             if (tempResult.size <= rq.limit - foundResults) {
//                 // add everything we got
//                 xfer(refs, finalResultList, tempResult, byRelevance)
//                 foundResults += tempResult.size // after dropping enough from the caller's offset we can save the results
//             } else {
//                 // add the missing entries and we are done
//                 xfer(refs, finalResultList, tempResult.subList(0, rq.limit - foundResults), byRelevance)
//                 foundResults = rq.limit
//             }
//         }
//         if (iteration++ >= MAX_ITERATIONS) { // increase iteration count, and check for security stop
//             LOGGER.warn("combined search prematurely stopped due to iteration count limit")
//             doMoreSearchRequests = false
//         }
//     }
// }
//
// // add the entities of the temporary result in order, or, if that search was done without sort, in order of the input refs
// def protected void xfer(List<Long> orderForNoSort, ArrayList<ENTITY> finalResultList, List<ENTITY> temp, boolean byRelevance) {
//     if (!byRelevance)
//         finalResultList.addAll(temp)
//     else {
//         // build a map
//         val indexMap = new HashMap<Long, ENTITY>(2 * temp.size)
//         for (e: temp)
//             indexMap.put(e.ret$Key, e)
//         for (r: orderForNoSort) {
//             val ENTITY ee = indexMap.get(r)
//             if (ee !== null)
//                 finalResultList.add(ee)
//         }
//     }
// }
//
// def protected void executeBOTHSearchDrivenByDb(RequestContext ctx, REQ rq,
//     SearchRequest<DTO,TRACKING> solrRequest, SearchRequest<DTO,TRACKING> dbRequest, ArrayList<ENTITY> finalResultList
// ) {
//     LOGGER.debug("DB driven combined search performed")
//     dbRequest.sortColumns = rq.sortColumns       // ask (only) DB to sort
//     solrRequest.limit = rq.limit
//     val solrRequestFilter = dbRequest.searchFilter // save for later use
//     searchTools.mapNames(solrRequest, textSearchFieldMappings)
//     processPrefixes(dbRequest);
//
//     var resultsToSkip = rq.offset
//     var increasedLimit = Math.min(rq.limit*4, 1000)  // use a higher limit because we expect to lose some when intersecting with the DB results, but respect Oracle's limitations
//     var iteration = 0
//     var boolean doMoreSearchRequests = true  // set to false once we know SOLR will not return more results
//     var int foundResults = 0
//     dbRequest.limit = increasedLimit
//
//     // iterate SOLR requests until we have enough data
//     while (doMoreSearchRequests && foundResults < rq.limit) {
//         dbRequest.offset = increasedLimit * iteration // consecutively increase the solr offset to include more results if they are needed
//
//         val refs = resolver.searchKey(dbRequest)
//         if (refs.size < increasedLimit) {
//             doMoreSearchRequests = false  // end of data
//         }
//
//         // just return finalResultList if no more refs can be found with the given offset/limit (shortcut)
//         if (refs.isEmpty)
//             return;
//         solrRequest.searchFilter = new AndFilter(solrRequestFilter, new LongFilter => [
//             fieldName = "objectRef"
//             valueList = refs
//         ])
//          // only query INITIAL_CALLER_LIMIT - RESULTS_FOUND + RESULTS_TO_SKIP. This will ensure we always get enough because a certain amount has to be skipped
//         solrRequest.limit = rq.limit - foundResults + resultsToSkip
//
//         // execute search and save result
//         val tempResult = textSearch.search(ctx, solrRequest, documentName, keyFieldName)
//
//         if (resultsToSkip > 0) { // if the caller supplied an offset we have to skip the first X (offset) results
//             if (resultsToSkip >= tempResult.size) {
//                 // skip everything!
//                 resultsToSkip -= tempResult.size
//             } else {
//                 // skip some, add some
//                 val sizeToAdd = Math.min(tempResult.size - resultsToSkip, rq.limit - foundResults)
//                 xfer(refs, finalResultList, tempResult.subList(resultsToSkip, sizeToAdd))
//                 foundResults += sizeToAdd
//                 resultsToSkip = 0
//             }
//         } else {
//             // add everything we got (this could be an empty list)
//             if (tempResult.size <= rq.limit - foundResults) {
//                 // add everything we got
//                 xfer(refs, finalResultList, tempResult)
//                 foundResults += tempResult.size // after dropping enough from the caller's offset we can save the results
//             } else {
//                 // add the missing entries and we are done
//                 xfer(refs, finalResultList, tempResult.subList(0, rq.limit - foundResults))
//                 foundResults = rq.limit
//             }
//         }
//         if (iteration++ >= MAX_ITERATIONS) { // increase iteration count, and check for security stop
//             LOGGER.warn("combined search prematurely stopped due to iteration count limit")
//             doMoreSearchRequests = false
//         }
//     }
// }
//
// // add the entities of the temporary result in order, or, if that search was done without sort, in order of the input refs
// def protected void xfer(List<Long> orderForNoSort, ArrayList<ENTITY> finalResultList, List<Long> temp) {
//     // build a set from the list entries
//     val indexMap = new HashSet<Long>(2 * temp.size)
//     indexMap.addAll(temp)
//     val em = resolver.entityManager
//     for (r: orderForNoSort) {
//         if (indexMap.contains(r)) {
//             val ENTITY ee = em.find(resolver.baseJpaEntityClass, r)
//             if (ee === null) {
//                 LOGGER.warn("Could not find entity of key {} any more!", r)
//             } else {
//                 finalResultList.add(ee)
//             }
//         }
//     }
// }
//
// /**
//  * Method splits the searchFilters in the original request into two requests that are solr only and dbOnly
//  */
// def protected void splitSearches(SearchFilter originalFilter, SearchRequest<DTO, TRACKING> solrRequest, SearchRequest<DTO, TRACKING> dbRequest) {
//     if (originalFilter instanceof FieldFilter) {
//         switch (filterTypeForField(originalFilter.fieldName)) {
//             case DB_ONLY:   dbRequest.searchFilter   = SearchFilters.and(dbRequest.searchFilter, originalFilter)
//             case SOLR_ONLY: solrRequest.searchFilter = SearchFilters.and(solrRequest.searchFilter, originalFilter)
//             default: {  // merge the filter into BOTH searches!
//                 dbRequest.searchFilter   = SearchFilters.and(dbRequest.searchFilter, originalFilter)
//                 solrRequest.searchFilter = SearchFilters.and(solrRequest.searchFilter, originalFilter)
//             }
//         }
//     } else if (originalFilter instanceof AndFilter) {
//         splitSearches(originalFilter.filter1, solrRequest, dbRequest)
//         splitSearches(originalFilter.filter2, solrRequest, dbRequest)
//     }
// }
//
// /**
//  * Determines which search engine(s) can process all filters. Also validates that only AND conditions are used.
//  * For performance reasons, the result is passed in by reference (avoids GC overhead due to temp object constructions).
//  * The passed in parameters may not be null.
//  */
// def protected void decideFilterAssociation(SearchFilter originalFilter, SearchFilterTypes result) {
//     if (originalFilter instanceof FieldFilter) {
//         result.add(filterTypeForField(originalFilter.fieldName))
//     } else if (originalFilter instanceof NotFilter) {
//         decideFilterAssociation(originalFilter.filter, result)
//     } else if (originalFilter instanceof AndFilter) {
//         decideFilterAssociation(originalFilter.filter1, result)
//         decideFilterAssociation(originalFilter.filter2, result)
//     } else {
//         throw new T9tException(T9tException.ILLEGAL_SOLR_DB_COMBINED_FILTER_EXPRESSION, originalFilter.ret$PQON)
//     }
// }
//
// /**
//  * Determines which search engine(s) can process a certain field.
//  * The data is available in the list textSearchPathElements. If no entry can be found, a default of DBONLY is assumed.
//  * Never returns null.
//  */
// def protected SearchFilterTypeEnum filterTypeForField(String path) {
//     val entry = textSearchPathElements.findFirst[thatMatches(path)]
//     if (entry === null)
//         return SearchFilterTypeEnum.DB_ONLY
//     return entry.searchType
// }
//
// /** Returns true if path matches the mapping entry, depending on type of match, otherwise false. */
// def private boolean thatMatches(SearchTypeMappingEntry it, String path) {
//     switch (matchType) {
//     case EXACT:     return path == name
//     case START:     return path.startsWith(name)
//     case SUBSTRING: return path.contains(name)
//     }
//     return false
// }
//}

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.jpa.IEntityMapper42;
import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey42;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.search.SearchFilterMatchTypeEnum;
import com.arvatosystems.t9t.base.search.SearchFilterTypeEnum;
import com.arvatosystems.t9t.base.search.SearchFilterTypes;
import com.arvatosystems.t9t.base.search.SearchRequest;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.ISearchTools;
import com.arvatosystems.t9t.base.services.ITextSearch;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.google.common.base.Objects;

import de.jpaw.annotations.AddLogger;
import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;

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
 */
@AddLogger
@Data
@SuppressWarnings("all")
public abstract class AbstractCombinedTextDatabaseSearchRequestHandler<REF extends Ref, DTO extends REF, TRACKING extends TrackingBase, REQ extends SearchRequest<DTO, TRACKING>, ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>> extends AbstractSearchRequestHandler<REQ> {
  /**
   * List entry for a prefix which defines if a field is SOLR only, DB only, or both search engines can evaluate the field.
   */
  @Data
  protected static class SearchTypeMappingEntry {
    protected final String name;

    protected final SearchFilterTypeEnum searchType;

    protected final SearchFilterMatchTypeEnum matchType;

    public SearchTypeMappingEntry(final String name, final SearchFilterTypeEnum searchType, final SearchFilterMatchTypeEnum matchType) {
      super();
      this.name = name;
      this.searchType = searchType;
      this.matchType = matchType;
    }

    @Override
    @Pure
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((this.name== null) ? 0 : this.name.hashCode());
      result = prime * result + ((this.searchType== null) ? 0 : this.searchType.hashCode());
      return prime * result + ((this.matchType== null) ? 0 : this.matchType.hashCode());
    }

    @Override
    @Pure
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final AbstractCombinedTextDatabaseSearchRequestHandler.SearchTypeMappingEntry other = (AbstractCombinedTextDatabaseSearchRequestHandler.SearchTypeMappingEntry) obj;
      if (this.name == null) {
        if (other.name != null)
          return false;
      } else if (!this.name.equals(other.name))
        return false;
      if (this.searchType == null) {
        if (other.searchType != null)
          return false;
      } else if (!this.searchType.equals(other.searchType))
        return false;
      if (this.matchType == null) {
        if (other.matchType != null)
          return false;
      } else if (!this.matchType.equals(other.matchType))
        return false;
      return true;
    }

    @Override
    @Pure
    public String toString() {
      final ToStringBuilder b = new ToStringBuilder(this);
      b.add("name", this.name);
      b.add("searchType", this.searchType);
      b.add("matchType", this.matchType);
      return b.toString();
    }

    @Pure
    public String getName() {
      return this.name;
    }

    @Pure
    public SearchFilterTypeEnum getSearchType() {
      return this.searchType;
    }

    @Pure
    public SearchFilterMatchTypeEnum getMatchType() {
      return this.matchType;
    }
  }

  private static final int MAX_ITERATIONS = 50;

  protected final IExecutor executor = Jdp.<IExecutor>getRequired(IExecutor.class);

  protected final ISearchTools searchTools = Jdp.<ISearchTools>getRequired(ISearchTools.class);

  protected final ITextSearch textSearch = Jdp.<ITextSearch>getRequired(ITextSearch.class);

  protected final IResolverSurrogateKey42<REF, TRACKING, ENTITY> resolver;

  protected final IEntityMapper42<Long, DTO, TRACKING, ENTITY> mapper;

  protected final List<AbstractCombinedTextDatabaseSearchRequestHandler.SearchTypeMappingEntry> textSearchPathElements;

  protected final Map<String, String> textSearchFieldMappings;

  protected final String documentName;

  protected final String keyFieldName;

  protected final BonaPortableClass<SearchRequest<DTO, TRACKING>> bclass;

  protected Map<String, String> getExtraMappings() {
    return null;
  }

  protected void processPrefixes(final SearchCriteria rq) {
    this.mapper.processSearchPrefixForDB(rq);
    final Map<String, String> mappings = this.getExtraMappings();
    final Map<String, String> _extraMappings = this.getExtraMappings();
    final boolean _tripleNotEquals = (_extraMappings != null);
    if (_tripleNotEquals) {
      final Function<String, String> _function = (final String path) -> {
        final boolean _contains = path.contains("Ref.");
        if (_contains) {
          String tmp = path;
          final Set<Map.Entry<String, String>> _entrySet = mappings.entrySet();
          for (final Map.Entry<String, String> r : _entrySet) {
            tmp = tmp.replace(r.getKey(), r.getValue());
          }
          return tmp;
        } else {
          return path;
        }
      };
      this.searchTools.mapNames(rq, _function);
    }
  }

  @Override
  public ReadAllResponse<DTO, TRACKING> execute(final RequestContext ctx, final REQ rq) {
    try {
      final boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(rq.getExpression());
      final boolean _not = (!_isNullOrEmpty);
      if (_not) {
        return this.executeSOLRSearch(ctx, rq);
      }
      final SearchFilterTypes filterTypes = new SearchFilterTypes();
      final SearchFilter _searchFilter = rq.getSearchFilter();
      if (_searchFilter!=null) {
        this.decideFilterAssociation(_searchFilter, filterTypes);
      }
      SearchFilterTypeEnum _xifexpression = null;
      final boolean _isNullOrEmpty_1 = IterableExtensions.isNullOrEmpty(rq.getSortColumns());
      final boolean _not_1 = (!_isNullOrEmpty_1);
      if (_not_1) {
        _xifexpression = this.filterTypeForField(rq.getSortColumns().get(0).getFieldName());
      }
      final SearchFilterTypeEnum sortType = _xifexpression;
      AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.debug("Filters indicate search types {} and sort type {} for expression {}", filterTypes, sortType, rq.getSearchFilter());
      if (((!filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY)) && (!Objects.equal(sortType, SearchFilterTypeEnum.SOLR_ONLY)))) {
        return this.executeDBSearch(rq);
      }
      if (((!filterTypes.contains(SearchFilterTypeEnum.DB_ONLY)) && (!Objects.equal(sortType, SearchFilterTypeEnum.DB_ONLY)))) {
        return this.executeSOLRSearch(ctx, rq);
      }
      final SearchRequest<DTO, TRACKING> solrRequest = this.bclass.newInstance();
      final SearchRequest<DTO, TRACKING> dbRequest = this.bclass.newInstance();
      this.splitSearches(rq.getSearchFilter(), solrRequest, dbRequest);
      AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.debug("Original request {} split to Solr Rq {} and DB rq {}", rq, solrRequest, dbRequest);
      if (((solrRequest.getSearchFilter() == null) && (dbRequest.getSearchFilter() == null))) {
        throw new T9tException(T9tException.ILE_SOLR_DB_COMBINED_FILTERS);
      }
      final int _limit = rq.getLimit();
      final ArrayList<ENTITY> finalResultList = new ArrayList<>(_limit);
      if ((sortType == null)) {
        this.executeBOTHSearchDrivenBySolr(ctx, rq, solrRequest, dbRequest, finalResultList);
      } else {
        if (((Objects.equal(sortType, SearchFilterTypeEnum.DB_ONLY) && filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY)) && (dbRequest.getSearchFilter() == null))) {
          return this.executeSOLRSearchWithDbSort(ctx, rq);
        } else {
          if ((Objects.equal(sortType, SearchFilterTypeEnum.DB_ONLY) ||
            ((!filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY)) && (!filterTypes.contains(SearchFilterTypeEnum.BOTH))))) {
            this.executeBOTHSearchDrivenByDb(ctx, rq, solrRequest, dbRequest, finalResultList);
          } else {
            this.executeBOTHSearchDrivenBySolr(ctx, rq, solrRequest, dbRequest, finalResultList);
          }
        }
      }
      return this.mapper.createReadAllResponse(finalResultList, rq.getSearchOutputTarget());
    } catch (final Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected ReadAllResponse<DTO, TRACKING> executeDBSearch(final REQ rq) {
    try {
      AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.debug("Using DB only search");
      this.processPrefixes(rq);
      return this.mapper.createReadAllResponse(this.resolver.search(rq, null), rq.getSearchOutputTarget());
    } catch (final Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected ReadAllResponse<DTO, TRACKING> executeSOLRSearch(final RequestContext ctx, final REQ rq) {
    try {
      this.searchTools.mapNames(rq, this.textSearchFieldMappings);
      AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.debug("SOLR request mapped: {}", rq);
      final List<Long> refs = this.textSearch.search(ctx, rq, this.documentName, this.keyFieldName);
      final boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        final ReadAllResponse<DTO, TRACKING> _readAllResponse = new ReadAllResponse<>();
        final Procedure1<ReadAllResponse<DTO, TRACKING>> _function = (final ReadAllResponse<DTO, TRACKING> it) -> {
          it.setDataList(Collections.<DataWithTrackingW<DTO, TRACKING>>unmodifiableList(CollectionLiterals.<DataWithTrackingW<DTO, TRACKING>>newArrayList()));
        };
        return ObjectExtensions.<ReadAllResponse<DTO, TRACKING>>operator_doubleArrow(_readAllResponse, _function);
      }
      final SearchRequest<DTO, TRACKING> _newInstance = this.bclass.newInstance();
      final Procedure1<SearchRequest<DTO, TRACKING>> _function_1 = (final SearchRequest<DTO, TRACKING> it) -> {
        final LongFilter _longFilter = new LongFilter("objectRef", null, null, null, refs);
        it.setSearchFilter(_longFilter);
        it.setSortColumns(rq.getSortColumns());
        it.setSearchOutputTarget(rq.getSearchOutputTarget());
      };
      final SearchRequest<DTO, TRACKING> newSearchRq = ObjectExtensions.<SearchRequest<DTO, TRACKING>>operator_doubleArrow(_newInstance, _function_1);
      AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.debug("solr only new searchReq: {}", newSearchRq);
      this.processPrefixes(newSearchRq);
      return this.mapper.createReadAllResponse(this.resolver.search(newSearchRq, null), newSearchRq.getSearchOutputTarget());
    } catch (final Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected ReadAllResponse<DTO, TRACKING> executeSOLRSearchWithDbSort(final RequestContext ctx, final REQ rq) {
    try {
      final List<SortColumn> sortCols = rq.getSortColumns();
      rq.setSortColumns(null);
      this.searchTools.mapNames(rq, this.textSearchFieldMappings);
      AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.debug("SOLR request mapped: {}", rq);
      final List<Long> refs = this.textSearch.search(ctx, rq, this.documentName, this.keyFieldName);
      final boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        final ReadAllResponse<DTO, TRACKING> _readAllResponse = new ReadAllResponse<>();
        final Procedure1<ReadAllResponse<DTO, TRACKING>> _function = (final ReadAllResponse<DTO, TRACKING> it) -> {
          it.setDataList(Collections.<DataWithTrackingW<DTO, TRACKING>>unmodifiableList(CollectionLiterals.<DataWithTrackingW<DTO, TRACKING>>newArrayList()));
        };
        return ObjectExtensions.<ReadAllResponse<DTO, TRACKING>>operator_doubleArrow(_readAllResponse, _function);
      }
      final SearchRequest<DTO, TRACKING> _newInstance = this.bclass.newInstance();
      final Procedure1<SearchRequest<DTO, TRACKING>> _function_1 = (final SearchRequest<DTO, TRACKING> it) -> {
        final LongFilter _longFilter = new LongFilter("objectRef", null, null, null, refs);
        it.setSearchFilter(_longFilter);
        it.setSortColumns(sortCols);
        it.setSearchOutputTarget(rq.getSearchOutputTarget());
      };
      final SearchRequest<DTO, TRACKING> newSearchRq = ObjectExtensions.<SearchRequest<DTO, TRACKING>>operator_doubleArrow(_newInstance, _function_1);
      AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.debug("solr only with DB sort new searchReq for DB: {}", newSearchRq);
      this.processPrefixes(newSearchRq);
      return this.mapper.createReadAllResponse(this.resolver.search(newSearchRq, null), newSearchRq.getSearchOutputTarget());
    } catch (final Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void executeBOTHSearchDrivenBySolr(final RequestContext ctx, final REQ rq, final SearchRequest<DTO, TRACKING> solrRequest, final SearchRequest<DTO, TRACKING> dbRequest, final ArrayList<ENTITY> finalResultList) {
    AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.debug("SOLR driven combined search performed");
    final boolean byRelevance = IterableExtensions.isNullOrEmpty(rq.getSortColumns());
    solrRequest.setSortColumns(rq.getSortColumns());
    dbRequest.setSortColumns(rq.getSortColumns());
    dbRequest.setLimit(rq.getLimit());
    final SearchFilter dbRequestFilter = dbRequest.getSearchFilter();
    this.searchTools.mapNames(solrRequest, this.textSearchFieldMappings);
    this.processPrefixes(dbRequest);
    int resultsToSkip = rq.getOffset();
    final int _limit = rq.getLimit();
    final int _multiply = (_limit * 4);
    final int increasedLimit = Math.min(_multiply, 1000);
    int iteration = 0;
    boolean doMoreSearchRequests = true;
    int foundResults = 0;
    solrRequest.setLimit(increasedLimit);
    while ((doMoreSearchRequests && (foundResults < rq.getLimit()))) {
      {
        solrRequest.setOffset((increasedLimit * iteration));
        final List<Long> refs = this.textSearch.search(ctx, solrRequest, this.documentName, this.keyFieldName);
        final int _size = refs.size();
        final boolean _lessThan = (_size < increasedLimit);
        if (_lessThan) {
          doMoreSearchRequests = false;
        }
        final boolean _isEmpty = refs.isEmpty();
        if (_isEmpty) {
          return;
        }
        final LongFilter _longFilter = new LongFilter();
        final Procedure1<LongFilter> _function = (final LongFilter it) -> {
          it.setFieldName("objectRef");
          it.setValueList(refs);
        };
        final LongFilter _doubleArrow = ObjectExtensions.<LongFilter>operator_doubleArrow(_longFilter, _function);
        final AndFilter _andFilter = new AndFilter(dbRequestFilter, _doubleArrow);
        dbRequest.setSearchFilter(_andFilter);
        final int _limit_1 = rq.getLimit();
        final int _minus = (_limit_1 - foundResults);
        final int _plus = (_minus + resultsToSkip);
        dbRequest.setLimit(_plus);
        final List<ENTITY> tempResult = this.resolver.search(dbRequest, null);
        if ((resultsToSkip > 0)) {
          final int _size_1 = tempResult.size();
          final boolean _greaterEqualsThan = (resultsToSkip >= _size_1);
          if (_greaterEqualsThan) {
            final int _resultsToSkip = resultsToSkip;
            final int _size_2 = tempResult.size();
            resultsToSkip = (_resultsToSkip - _size_2);
          } else {
            final int _size_3 = tempResult.size();
            final int _minus_1 = (_size_3 - resultsToSkip);
            final int _limit_2 = rq.getLimit();
            final int _minus_2 = (_limit_2 - foundResults);
            final int sizeToAdd = Math.min(_minus_1, _minus_2);
            this.xfer(refs, finalResultList, tempResult.subList(resultsToSkip, sizeToAdd), byRelevance);
            final int _foundResults = foundResults;
            foundResults = (_foundResults + sizeToAdd);
            resultsToSkip = 0;
          }
        } else {
          final int _size_4 = tempResult.size();
          final int _limit_3 = rq.getLimit();
          final int _minus_3 = (_limit_3 - foundResults);
          final boolean _lessEqualsThan = (_size_4 <= _minus_3);
          if (_lessEqualsThan) {
            this.xfer(refs, finalResultList, tempResult, byRelevance);
            final int _foundResults_1 = foundResults;
            final int _size_5 = tempResult.size();
            foundResults = (_foundResults_1 + _size_5);
          } else {
            final int _limit_4 = rq.getLimit();
            final int _minus_4 = (_limit_4 - foundResults);
            this.xfer(refs, finalResultList, tempResult.subList(0, _minus_4), byRelevance);
            foundResults = rq.getLimit();
          }
        }
        final int _plusPlus = iteration++;
        final boolean _greaterEqualsThan_1 = (_plusPlus >= AbstractCombinedTextDatabaseSearchRequestHandler.MAX_ITERATIONS);
        if (_greaterEqualsThan_1) {
          AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.warn("combined search prematurely stopped due to iteration count limit");
          doMoreSearchRequests = false;
        }
      }
    }
  }

  protected void xfer(final List<Long> orderForNoSort, final ArrayList<ENTITY> finalResultList, final List<ENTITY> temp, final boolean byRelevance) {
    if ((!byRelevance)) {
      finalResultList.addAll(temp);
    } else {
      final int _size = temp.size();
      final int _multiply = (2 * _size);
      final HashMap<Long, ENTITY> indexMap = new HashMap<>(_multiply);
      for (final ENTITY e : temp) {
        indexMap.put(e.ret$Key(), e);
      }
      for (final Long r : orderForNoSort) {
        {
          final ENTITY ee = indexMap.get(r);
          if ((ee != null)) {
            finalResultList.add(ee);
          }
        }
      }
    }
  }

  protected void executeBOTHSearchDrivenByDb(final RequestContext ctx, final REQ rq, final SearchRequest<DTO, TRACKING> solrRequest, final SearchRequest<DTO, TRACKING> dbRequest, final ArrayList<ENTITY> finalResultList) {
    AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.debug("DB driven combined search performed");
    dbRequest.setSortColumns(rq.getSortColumns());
    solrRequest.setLimit(rq.getLimit());
    final SearchFilter solrRequestFilter = dbRequest.getSearchFilter();
    this.searchTools.mapNames(solrRequest, this.textSearchFieldMappings);
    this.processPrefixes(dbRequest);
    int resultsToSkip = rq.getOffset();
    final int _limit = rq.getLimit();
    final int _multiply = (_limit * 4);
    final int increasedLimit = Math.min(_multiply, 1000);
    int iteration = 0;
    boolean doMoreSearchRequests = true;
    int foundResults = 0;
    dbRequest.setLimit(increasedLimit);
    while ((doMoreSearchRequests && (foundResults < rq.getLimit()))) {
      {
        dbRequest.setOffset((increasedLimit * iteration));
        final List<Long> refs = this.resolver.searchKey(dbRequest);
        final int _size = refs.size();
        final boolean _lessThan = (_size < increasedLimit);
        if (_lessThan) {
          doMoreSearchRequests = false;
        }
        final boolean _isEmpty = refs.isEmpty();
        if (_isEmpty) {
          return;
        }
        final LongFilter _longFilter = new LongFilter();
        final Procedure1<LongFilter> _function = (final LongFilter it) -> {
          it.setFieldName("objectRef");
          it.setValueList(refs);
        };
        final LongFilter _doubleArrow = ObjectExtensions.<LongFilter>operator_doubleArrow(_longFilter, _function);
        final AndFilter _andFilter = new AndFilter(solrRequestFilter, _doubleArrow);
        solrRequest.setSearchFilter(_andFilter);
        final int _limit_1 = rq.getLimit();
        final int _minus = (_limit_1 - foundResults);
        final int _plus = (_minus + resultsToSkip);
        solrRequest.setLimit(_plus);
        final List<Long> tempResult = this.textSearch.search(ctx, solrRequest, this.documentName, this.keyFieldName);
        if ((resultsToSkip > 0)) {
          final int _size_1 = tempResult.size();
          final boolean _greaterEqualsThan = (resultsToSkip >= _size_1);
          if (_greaterEqualsThan) {
            final int _resultsToSkip = resultsToSkip;
            final int _size_2 = tempResult.size();
            resultsToSkip = (_resultsToSkip - _size_2);
          } else {
            final int _size_3 = tempResult.size();
            final int _minus_1 = (_size_3 - resultsToSkip);
            final int _limit_2 = rq.getLimit();
            final int _minus_2 = (_limit_2 - foundResults);
            final int sizeToAdd = Math.min(_minus_1, _minus_2);
            this.xfer(refs, finalResultList, tempResult.subList(resultsToSkip, sizeToAdd));
            final int _foundResults = foundResults;
            foundResults = (_foundResults + sizeToAdd);
            resultsToSkip = 0;
          }
        } else {
          final int _size_4 = tempResult.size();
          final int _limit_3 = rq.getLimit();
          final int _minus_3 = (_limit_3 - foundResults);
          final boolean _lessEqualsThan = (_size_4 <= _minus_3);
          if (_lessEqualsThan) {
            this.xfer(refs, finalResultList, tempResult);
            final int _foundResults_1 = foundResults;
            final int _size_5 = tempResult.size();
            foundResults = (_foundResults_1 + _size_5);
          } else {
            final int _limit_4 = rq.getLimit();
            final int _minus_4 = (_limit_4 - foundResults);
            this.xfer(refs, finalResultList, tempResult.subList(0, _minus_4));
            foundResults = rq.getLimit();
          }
        }
        final int _plusPlus = iteration++;
        final boolean _greaterEqualsThan_1 = (_plusPlus >= AbstractCombinedTextDatabaseSearchRequestHandler.MAX_ITERATIONS);
        if (_greaterEqualsThan_1) {
          AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.warn("combined search prematurely stopped due to iteration count limit");
          doMoreSearchRequests = false;
        }
      }
    }
  }

  protected void xfer(final List<Long> orderForNoSort, final ArrayList<ENTITY> finalResultList, final List<Long> temp) {
    final int _size = temp.size();
    final int _multiply = (2 * _size);
    final HashSet<Long> indexMap = new HashSet<>(_multiply);
    indexMap.addAll(temp);
    final EntityManager em = this.resolver.getEntityManager();
    for (final Long r : orderForNoSort) {
      final boolean _contains = indexMap.contains(r);
      if (_contains) {
        final ENTITY ee = em.<ENTITY>find(this.resolver.getBaseJpaEntityClass(), r);
        if ((ee == null)) {
          AbstractCombinedTextDatabaseSearchRequestHandler.LOGGER.warn("Could not find entity of key {} any more!", r);
        } else {
          finalResultList.add(ee);
        }
      }
    }
  }

  /**
   * Method splits the searchFilters in the original request into two requests that are solr only and dbOnly
   */
  protected void splitSearches(final SearchFilter originalFilter, final SearchRequest<DTO, TRACKING> solrRequest, final SearchRequest<DTO, TRACKING> dbRequest) {
    if ((originalFilter instanceof FieldFilter)) {
      final SearchFilterTypeEnum _filterTypeForField = this.filterTypeForField(((FieldFilter)originalFilter).getFieldName());
      if (_filterTypeForField != null) {
        switch (_filterTypeForField) {
          case DB_ONLY:
            dbRequest.setSearchFilter(SearchFilters.and(dbRequest.getSearchFilter(), originalFilter));
            break;
          case SOLR_ONLY:
            solrRequest.setSearchFilter(SearchFilters.and(solrRequest.getSearchFilter(), originalFilter));
            break;
          default:
            {
              dbRequest.setSearchFilter(SearchFilters.and(dbRequest.getSearchFilter(), originalFilter));
              solrRequest.setSearchFilter(SearchFilters.and(solrRequest.getSearchFilter(), originalFilter));
            }
            break;
        }
      } else {
        {
          dbRequest.setSearchFilter(SearchFilters.and(dbRequest.getSearchFilter(), originalFilter));
          solrRequest.setSearchFilter(SearchFilters.and(solrRequest.getSearchFilter(), originalFilter));
        }
      }
    } else {
      if ((originalFilter instanceof AndFilter)) {
        this.splitSearches(((AndFilter)originalFilter).getFilter1(), solrRequest, dbRequest);
        this.splitSearches(((AndFilter)originalFilter).getFilter2(), solrRequest, dbRequest);
      }
    }
  }

  /**
   * Determines which search engine(s) can process all filters. Also validates that only AND conditions are used.
   * For performance reasons, the result is passed in by reference (avoids GC overhead due to temp object constructions).
   * The passed in parameters may not be null.
   */
  protected void decideFilterAssociation(final SearchFilter originalFilter, final SearchFilterTypes result) {
    if ((originalFilter instanceof FieldFilter)) {
      result.add(this.filterTypeForField(((FieldFilter)originalFilter).getFieldName()));
    } else {
      if ((originalFilter instanceof NotFilter)) {
        this.decideFilterAssociation(((NotFilter)originalFilter).getFilter(), result);
      } else {
        if ((originalFilter instanceof AndFilter)) {
          this.decideFilterAssociation(((AndFilter)originalFilter).getFilter1(), result);
          this.decideFilterAssociation(((AndFilter)originalFilter).getFilter2(), result);
        } else {
          final String _ret$PQON = originalFilter.ret$PQON();
          throw new T9tException(T9tException.ILLEGAL_SOLR_DB_COMBINED_FILTER_EXPRESSION, _ret$PQON);
        }
      }
    }
  }

  /**
   * Determines which search engine(s) can process a certain field.
   * The data is available in the list textSearchPathElements. If no entry can be found, a default of DBONLY is assumed.
   * Never returns null.
   */
  protected SearchFilterTypeEnum filterTypeForField(final String path) {
    final Function1<AbstractCombinedTextDatabaseSearchRequestHandler.SearchTypeMappingEntry, Boolean> _function = (final AbstractCombinedTextDatabaseSearchRequestHandler.SearchTypeMappingEntry it) -> {
      return Boolean.valueOf(this.thatMatches(it, path));
    };
    final AbstractCombinedTextDatabaseSearchRequestHandler.SearchTypeMappingEntry entry = IterableExtensions.<AbstractCombinedTextDatabaseSearchRequestHandler.SearchTypeMappingEntry>findFirst(this.textSearchPathElements, _function);
    if ((entry == null)) {
      return SearchFilterTypeEnum.DB_ONLY;
    }
    return entry.searchType;
  }

  /**
   * Returns true if path matches the mapping entry, depending on type of match, otherwise false.
   */
  private boolean thatMatches(final AbstractCombinedTextDatabaseSearchRequestHandler.SearchTypeMappingEntry it, final String path) {
    final SearchFilterMatchTypeEnum matchType = it.matchType;
    if (matchType != null) {
      switch (matchType) {
        case EXACT:
          return Objects.equal(path, it.name);
        case START:
          return path.startsWith(it.name);
        case SUBSTRING:
          return path.contains(it.name);
        default:
          break;
      }
    }
    return false;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(com.arvatosystems.t9t.base.jpa.impl.AbstractCombinedTextDatabaseSearchRequestHandler.class);

  public AbstractCombinedTextDatabaseSearchRequestHandler(final IResolverSurrogateKey42<REF, TRACKING, ENTITY> resolver, final IEntityMapper42<Long, DTO, TRACKING, ENTITY> mapper, final List<AbstractCombinedTextDatabaseSearchRequestHandler.SearchTypeMappingEntry> textSearchPathElements, final Map<String, String> textSearchFieldMappings, final String documentName, final String keyFieldName, final BonaPortableClass<SearchRequest<DTO, TRACKING>> bclass) {
    super();
    this.resolver = resolver;
    this.mapper = mapper;
    this.textSearchPathElements = textSearchPathElements;
    this.textSearchFieldMappings = textSearchFieldMappings;
    this.documentName = documentName;
    this.keyFieldName = keyFieldName;
    this.bclass = bclass;
  }

  @Override
  @Pure
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.executor== null) ? 0 : this.executor.hashCode());
    result = prime * result + ((this.searchTools== null) ? 0 : this.searchTools.hashCode());
    result = prime * result + ((this.textSearch== null) ? 0 : this.textSearch.hashCode());
    result = prime * result + ((this.resolver== null) ? 0 : this.resolver.hashCode());
    result = prime * result + ((this.mapper== null) ? 0 : this.mapper.hashCode());
    result = prime * result + ((this.textSearchPathElements== null) ? 0 : this.textSearchPathElements.hashCode());
    result = prime * result + ((this.textSearchFieldMappings== null) ? 0 : this.textSearchFieldMappings.hashCode());
    result = prime * result + ((this.documentName== null) ? 0 : this.documentName.hashCode());
    result = prime * result + ((this.keyFieldName== null) ? 0 : this.keyFieldName.hashCode());
    return prime * result + ((this.bclass== null) ? 0 : this.bclass.hashCode());
  }

  @Override
  @Pure
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final AbstractCombinedTextDatabaseSearchRequestHandler<?, ?, ?, ?, ?> other = (AbstractCombinedTextDatabaseSearchRequestHandler<?, ?, ?, ?, ?>) obj;
    if (this.executor == null) {
      if (other.executor != null)
        return false;
    } else if (!this.executor.equals(other.executor))
      return false;
    if (this.searchTools == null) {
      if (other.searchTools != null)
        return false;
    } else if (!this.searchTools.equals(other.searchTools))
      return false;
    if (this.textSearch == null) {
      if (other.textSearch != null)
        return false;
    } else if (!this.textSearch.equals(other.textSearch))
      return false;
    if (this.resolver == null) {
      if (other.resolver != null)
        return false;
    } else if (!this.resolver.equals(other.resolver))
      return false;
    if (this.mapper == null) {
      if (other.mapper != null)
        return false;
    } else if (!this.mapper.equals(other.mapper))
      return false;
    if (this.textSearchPathElements == null) {
      if (other.textSearchPathElements != null)
        return false;
    } else if (!this.textSearchPathElements.equals(other.textSearchPathElements))
      return false;
    if (this.textSearchFieldMappings == null) {
      if (other.textSearchFieldMappings != null)
        return false;
    } else if (!this.textSearchFieldMappings.equals(other.textSearchFieldMappings))
      return false;
    if (this.documentName == null) {
      if (other.documentName != null)
        return false;
    } else if (!this.documentName.equals(other.documentName))
      return false;
    if (this.keyFieldName == null) {
      if (other.keyFieldName != null)
        return false;
    } else if (!this.keyFieldName.equals(other.keyFieldName))
      return false;
    if (this.bclass == null) {
      if (other.bclass != null)
        return false;
    } else if (!this.bclass.equals(other.bclass))
      return false;
    return true;
  }

  @Override
  @Pure
  public String toString() {
    return new ToStringBuilder(this)
        .addAllFields()
        .toString();
  }

  @Pure
  public IExecutor getExecutor() {
    return this.executor;
  }

  @Pure
  public ISearchTools getSearchTools() {
    return this.searchTools;
  }

  @Pure
  public ITextSearch getTextSearch() {
    return this.textSearch;
  }

  @Pure
  public IResolverSurrogateKey42<REF, TRACKING, ENTITY> getResolver() {
    return this.resolver;
  }

  @Pure
  public IEntityMapper42<Long, DTO, TRACKING, ENTITY> getMapper() {
    return this.mapper;
  }

  @Pure
  public List<AbstractCombinedTextDatabaseSearchRequestHandler.SearchTypeMappingEntry> getTextSearchPathElements() {
    return this.textSearchPathElements;
  }

  @Pure
  public Map<String, String> getTextSearchFieldMappings() {
    return this.textSearchFieldMappings;
  }

  @Pure
  public String getDocumentName() {
    return this.documentName;
  }

  @Pure
  public String getKeyFieldName() {
    return this.keyFieldName;
  }

  @Pure
  public BonaPortableClass<SearchRequest<DTO, TRACKING>> getBclass() {
    return this.bclass;
  }
}
