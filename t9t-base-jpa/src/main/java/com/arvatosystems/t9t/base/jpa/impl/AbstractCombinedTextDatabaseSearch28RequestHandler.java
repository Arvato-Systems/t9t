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
//import com.arvatosystems.t9t.base.jpa.IEntityMapper28
//import com.arvatosystems.t9t.base.search.ReadAll28Response
//import com.arvatosystems.t9t.base.search.Search28Request
//import com.arvatosystems.t9t.base.search.SearchFilterMatchTypeEnum
//import com.arvatosystems.t9t.base.search.SearchFilterTypeEnum
//import com.arvatosystems.t9t.base.search.SearchFilterTypes
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
//import java.util.HashSet
//import java.util.List
//import java.util.Map
//import org.eclipse.xtend.lib.annotations.Data
//import de.jpaw.bonaparte.pojos.api.NotFilter
//import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey28
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
//abstract class AbstractCombinedTextDatabaseSearch28RequestHandler<REF extends Ref, DTO extends REF, TRACKING extends TrackingBase, REQ extends Search28Request<DTO, TRACKING>, ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>> extends AbstractSearchRequestHandler<REQ> {
// final static int MAX_ITERATIONS = 50;  // limit the number of loop iterations. Will produce results too small, but that is better than choking the system.
//
// protected final IExecutor     executor    = Jdp.getRequired(IExecutor)
// protected final ISearchTools  searchTools = Jdp.getRequired(ISearchTools)
// protected final ITextSearch   textSearch  = Jdp.getRequired(ITextSearch)
// protected IResolverSurrogateKey28<REF, TRACKING, ENTITY> resolver          // set by constructor
// protected IEntityMapper28<Long, DTO, TRACKING, ENTITY>   mapper            // set by constructor
// protected final List<SearchTypeMappingEntry> textSearchPathElements      // set by constructor
// protected final Map<String, String>          textSearchFieldMappings     // set by constructor
// protected final String documentName // set by constructor
// protected final String keyFieldName // set by constructor
// protected final BonaPortableClass<Search28Request<DTO, TRACKING>> bclass // set by constructor
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
// override ReadAll28Response<DTO, TRACKING> execute(RequestContext ctx, REQ rq) {
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
//     if (solrRequest.searchFilter === null || dbRequest.searchFilter === null)
//         throw new T9tException(T9tException.ILE_SOLR_DB_COMBINED_FILTERS)
//
//     val finalResultList = new ArrayList<ENTITY>(rq.limit)
//     if (sortType === null) {
//         // must do SOLR driven search in order to get ordering by relevance
//         executeBOTHSearchDrivenBySolr(ctx, rq, solrRequest, dbRequest, finalResultList)
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
// def protected ReadAll28Response<DTO, TRACKING> executeDBSearch(REQ rq) {
//     // database search
//     // preprocess prefixes for DB
//     LOGGER.debug("Using DB only search")
//     mapper.processSearchPrefixForDB(rq); // convert the field with searchPrefix
//     // delegate to database search
//     return mapper.createReadAllResponse(resolver.search(rq, null), rq.getSearchOutputTarget());
// }
//
// def protected ReadAll28Response<DTO, TRACKING> executeSOLRSearch(RequestContext ctx, REQ rq) {
//     // preprocess args for SOLR
//     searchTools.mapNames(rq, textSearchFieldMappings)
//     LOGGER.debug("SOLR request mapped: {}", rq)
//     val refs = textSearch.search(ctx, rq, documentName, keyFieldName)
//
//     // end here if there are no results - DB query would return an error
//     if (refs.isEmpty)
//         return new ReadAll28Response<DTO, TRACKING> => [
//             dataList = #[]
//         ]
//     // obtain DTOs for the refs
//     val newSearchRq = bclass.newInstance => [
//         searchFilter = new LongFilter("objectRef", null, null, null, refs)
//         sortColumns = rq.sortColumns
//         searchOutputTarget = rq.searchOutputTarget
//     ]
//     LOGGER.debug("solr only new searchReq: {}", newSearchRq)
//     mapper.processSearchPrefixForDB(newSearchRq); // convert the field with searchPrefix
//     return mapper.createReadAllResponse(resolver.search(newSearchRq, null), newSearchRq.getSearchOutputTarget());
// }
//
// // when this method is called, there are filter criteria which are SOLR only as well as some which are DB only, or DB only sort criteria
// def protected void executeBOTHSearchDrivenBySolr(RequestContext ctx, REQ rq,
//     Search28Request<DTO,TRACKING> solrRequest, Search28Request<DTO,TRACKING> dbRequest, ArrayList<ENTITY> finalResultList
// ) {
//     LOGGER.debug("SOLR driven combined search performed")
//     val byRelevance = rq.sortColumns.nullOrEmpty;
//     solrRequest.sortColumns = rq.sortColumns     // we know it is not DB_ONLY if we are here
//     dbRequest.sortColumns = rq.sortColumns       // ask DB to sort by same
//     dbRequest.limit = rq.limit
//     val dbRequestFilter = dbRequest.searchFilter // save for later use
//     searchTools.mapNames(solrRequest, textSearchFieldMappings)
//     mapper.processSearchPrefixForDB(dbRequest);
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
//     Search28Request<DTO,TRACKING> solrRequest, Search28Request<DTO,TRACKING> dbRequest, ArrayList<ENTITY> finalResultList
// ) {
//     LOGGER.debug("DB driven combined search performed")
//     dbRequest.sortColumns = rq.sortColumns       // ask (only) DB to sort
//     solrRequest.limit = rq.limit
//     val solrRequestFilter = dbRequest.searchFilter // save for later use
//     searchTools.mapNames(solrRequest, textSearchFieldMappings)
//     mapper.processSearchPrefixForDB(dbRequest);
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
// def protected void splitSearches(SearchFilter originalFilter, Search28Request<DTO, TRACKING> solrRequest, Search28Request<DTO, TRACKING> dbRequest) {
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
import com.arvatosystems.t9t.base.jpa.IEntityMapper28;
import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey28;
import com.arvatosystems.t9t.base.search.ReadAll28Response;
import com.arvatosystems.t9t.base.search.Search28Request;
import com.arvatosystems.t9t.base.search.SearchFilterMatchTypeEnum;
import com.arvatosystems.t9t.base.search.SearchFilterTypeEnum;
import com.arvatosystems.t9t.base.search.SearchFilterTypes;
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
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public abstract class AbstractCombinedTextDatabaseSearch28RequestHandler<REF extends Ref, DTO extends REF, TRACKING extends TrackingBase, REQ extends Search28Request<DTO, TRACKING>, ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>> extends AbstractSearchRequestHandler<REQ> {
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
      AbstractCombinedTextDatabaseSearch28RequestHandler.SearchTypeMappingEntry other = (AbstractCombinedTextDatabaseSearch28RequestHandler.SearchTypeMappingEntry) obj;
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
      ToStringBuilder b = new ToStringBuilder(this);
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

  protected final IResolverSurrogateKey28<REF, TRACKING, ENTITY> resolver;

  protected final IEntityMapper28<Long, DTO, TRACKING, ENTITY> mapper;

  protected final List<AbstractCombinedTextDatabaseSearch28RequestHandler.SearchTypeMappingEntry> textSearchPathElements;

  protected final Map<String, String> textSearchFieldMappings;

  protected final String documentName;

  protected final String keyFieldName;

  protected final BonaPortableClass<Search28Request<DTO, TRACKING>> bclass;

  @Override
  public ReadAll28Response<DTO, TRACKING> execute(final RequestContext ctx, final REQ rq) {
    try {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(rq.getExpression());
      boolean _not = (!_isNullOrEmpty);
      if (_not) {
        return this.executeSOLRSearch(ctx, rq);
      }
      final SearchFilterTypes filterTypes = new SearchFilterTypes();
      SearchFilter _searchFilter = rq.getSearchFilter();
      if (_searchFilter!=null) {
        this.decideFilterAssociation(_searchFilter, filterTypes);
      }
      SearchFilterTypeEnum _xifexpression = null;
      boolean _isNullOrEmpty_1 = IterableExtensions.isNullOrEmpty(rq.getSortColumns());
      boolean _not_1 = (!_isNullOrEmpty_1);
      if (_not_1) {
        _xifexpression = this.filterTypeForField(rq.getSortColumns().get(0).getFieldName());
      }
      final SearchFilterTypeEnum sortType = _xifexpression;
      AbstractCombinedTextDatabaseSearch28RequestHandler.LOGGER.debug("Filters indicate search types {} and sort type {} for expression {}", filterTypes, sortType, rq.getSearchFilter());
      if (((!filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY)) && (!Objects.equal(sortType, SearchFilterTypeEnum.SOLR_ONLY)))) {
        return this.executeDBSearch(rq);
      }
      if (((!filterTypes.contains(SearchFilterTypeEnum.DB_ONLY)) && (!Objects.equal(sortType, SearchFilterTypeEnum.DB_ONLY)))) {
        return this.executeSOLRSearch(ctx, rq);
      }
      final Search28Request<DTO, TRACKING> solrRequest = this.bclass.newInstance();
      final Search28Request<DTO, TRACKING> dbRequest = this.bclass.newInstance();
      this.splitSearches(rq.getSearchFilter(), solrRequest, dbRequest);
      AbstractCombinedTextDatabaseSearch28RequestHandler.LOGGER.debug("Original request {} split to Solr Rq {} and DB rq {}", rq, solrRequest, dbRequest);
      if (((solrRequest.getSearchFilter() == null) || (dbRequest.getSearchFilter() == null))) {
        throw new T9tException(T9tException.ILE_SOLR_DB_COMBINED_FILTERS);
      }
      int _limit = rq.getLimit();
      final ArrayList<ENTITY> finalResultList = new ArrayList<ENTITY>(_limit);
      if ((sortType == null)) {
        this.executeBOTHSearchDrivenBySolr(ctx, rq, solrRequest, dbRequest, finalResultList);
      } else {
        if ((Objects.equal(sortType, SearchFilterTypeEnum.DB_ONLY) ||
          ((!filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY)) && (!filterTypes.contains(SearchFilterTypeEnum.BOTH))))) {
          this.executeBOTHSearchDrivenByDb(ctx, rq, solrRequest, dbRequest, finalResultList);
        } else {
          this.executeBOTHSearchDrivenBySolr(ctx, rq, solrRequest, dbRequest, finalResultList);
        }
      }
      return this.mapper.createReadAllResponse(finalResultList, rq.getSearchOutputTarget());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected ReadAll28Response<DTO, TRACKING> executeDBSearch(final REQ rq) {
    try {
      AbstractCombinedTextDatabaseSearch28RequestHandler.LOGGER.debug("Using DB only search");
      this.mapper.processSearchPrefixForDB(rq);
      return this.mapper.createReadAllResponse(this.resolver.search(rq, null), rq.getSearchOutputTarget());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected ReadAll28Response<DTO, TRACKING> executeSOLRSearch(final RequestContext ctx, final REQ rq) {
    try {
      this.searchTools.mapNames(rq, this.textSearchFieldMappings);
      AbstractCombinedTextDatabaseSearch28RequestHandler.LOGGER.debug("SOLR request mapped: {}", rq);
      final List<Long> refs = this.textSearch.search(ctx, rq, this.documentName, this.keyFieldName);
      boolean _isEmpty = refs.isEmpty();
      if (_isEmpty) {
        ReadAll28Response<DTO, TRACKING> _readAll28Response = new ReadAll28Response<DTO, TRACKING>();
        final Procedure1<ReadAll28Response<DTO, TRACKING>> _function = (ReadAll28Response<DTO, TRACKING> it) -> {
          it.setDataList(Collections.<DataWithTrackingS<DTO, TRACKING>>unmodifiableList(CollectionLiterals.<DataWithTrackingS<DTO, TRACKING>>newArrayList()));
        };
        return ObjectExtensions.<ReadAll28Response<DTO, TRACKING>>operator_doubleArrow(_readAll28Response, _function);
      }
      Search28Request<DTO, TRACKING> _newInstance = this.bclass.newInstance();
      final Procedure1<Search28Request<DTO, TRACKING>> _function_1 = (Search28Request<DTO, TRACKING> it) -> {
        LongFilter _longFilter = new LongFilter("objectRef", null, null, null, refs);
        it.setSearchFilter(_longFilter);
        it.setSortColumns(rq.getSortColumns());
        it.setSearchOutputTarget(rq.getSearchOutputTarget());
      };
      final Search28Request<DTO, TRACKING> newSearchRq = ObjectExtensions.<Search28Request<DTO, TRACKING>>operator_doubleArrow(_newInstance, _function_1);
      AbstractCombinedTextDatabaseSearch28RequestHandler.LOGGER.debug("solr only new searchReq: {}", newSearchRq);
      this.mapper.processSearchPrefixForDB(newSearchRq);
      return this.mapper.createReadAllResponse(this.resolver.search(newSearchRq, null), newSearchRq.getSearchOutputTarget());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void executeBOTHSearchDrivenBySolr(final RequestContext ctx, final REQ rq, final Search28Request<DTO, TRACKING> solrRequest, final Search28Request<DTO, TRACKING> dbRequest, final ArrayList<ENTITY> finalResultList) {
    AbstractCombinedTextDatabaseSearch28RequestHandler.LOGGER.debug("SOLR driven combined search performed");
    final boolean byRelevance = IterableExtensions.isNullOrEmpty(rq.getSortColumns());
    solrRequest.setSortColumns(rq.getSortColumns());
    dbRequest.setSortColumns(rq.getSortColumns());
    dbRequest.setLimit(rq.getLimit());
    final SearchFilter dbRequestFilter = dbRequest.getSearchFilter();
    this.searchTools.mapNames(solrRequest, this.textSearchFieldMappings);
    this.mapper.processSearchPrefixForDB(dbRequest);
    int resultsToSkip = rq.getOffset();
    int _limit = rq.getLimit();
    int _multiply = (_limit * 4);
    int increasedLimit = Math.min(_multiply, 1000);
    int iteration = 0;
    boolean doMoreSearchRequests = true;
    int foundResults = 0;
    solrRequest.setLimit(increasedLimit);
    while ((doMoreSearchRequests && (foundResults < rq.getLimit()))) {
      {
        solrRequest.setOffset((increasedLimit * iteration));
        final List<Long> refs = this.textSearch.search(ctx, solrRequest, this.documentName, this.keyFieldName);
        int _size = refs.size();
        boolean _lessThan = (_size < increasedLimit);
        if (_lessThan) {
          doMoreSearchRequests = false;
        }
        boolean _isEmpty = refs.isEmpty();
        if (_isEmpty) {
          return;
        }
        LongFilter _longFilter = new LongFilter();
        final Procedure1<LongFilter> _function = (LongFilter it) -> {
          it.setFieldName("objectRef");
          it.setValueList(refs);
        };
        LongFilter _doubleArrow = ObjectExtensions.<LongFilter>operator_doubleArrow(_longFilter, _function);
        AndFilter _andFilter = new AndFilter(dbRequestFilter, _doubleArrow);
        dbRequest.setSearchFilter(_andFilter);
        int _limit_1 = rq.getLimit();
        int _minus = (_limit_1 - foundResults);
        int _plus = (_minus + resultsToSkip);
        dbRequest.setLimit(_plus);
        List<ENTITY> tempResult = this.resolver.search(dbRequest, null);
        if ((resultsToSkip > 0)) {
          int _size_1 = tempResult.size();
          boolean _greaterEqualsThan = (resultsToSkip >= _size_1);
          if (_greaterEqualsThan) {
            int _resultsToSkip = resultsToSkip;
            int _size_2 = tempResult.size();
            resultsToSkip = (_resultsToSkip - _size_2);
          } else {
            int _size_3 = tempResult.size();
            int _minus_1 = (_size_3 - resultsToSkip);
            int _limit_2 = rq.getLimit();
            int _minus_2 = (_limit_2 - foundResults);
            final int sizeToAdd = Math.min(_minus_1, _minus_2);
            this.xfer(refs, finalResultList, tempResult.subList(resultsToSkip, sizeToAdd), byRelevance);
            int _foundResults = foundResults;
            foundResults = (_foundResults + sizeToAdd);
            resultsToSkip = 0;
          }
        } else {
          int _size_4 = tempResult.size();
          int _limit_3 = rq.getLimit();
          int _minus_3 = (_limit_3 - foundResults);
          boolean _lessEqualsThan = (_size_4 <= _minus_3);
          if (_lessEqualsThan) {
            this.xfer(refs, finalResultList, tempResult, byRelevance);
            int _foundResults_1 = foundResults;
            int _size_5 = tempResult.size();
            foundResults = (_foundResults_1 + _size_5);
          } else {
            int _limit_4 = rq.getLimit();
            int _minus_4 = (_limit_4 - foundResults);
            this.xfer(refs, finalResultList, tempResult.subList(0, _minus_4), byRelevance);
            foundResults = rq.getLimit();
          }
        }
        int _plusPlus = iteration++;
        boolean _greaterEqualsThan_1 = (_plusPlus >= AbstractCombinedTextDatabaseSearch28RequestHandler.MAX_ITERATIONS);
        if (_greaterEqualsThan_1) {
          AbstractCombinedTextDatabaseSearch28RequestHandler.LOGGER.warn("combined search prematurely stopped due to iteration count limit");
          doMoreSearchRequests = false;
        }
      }
    }
  }

  protected void xfer(final List<Long> orderForNoSort, final ArrayList<ENTITY> finalResultList, final List<ENTITY> temp, final boolean byRelevance) {
    if ((!byRelevance)) {
      finalResultList.addAll(temp);
    } else {
      int _size = temp.size();
      int _multiply = (2 * _size);
      final HashMap<Long, ENTITY> indexMap = new HashMap<Long, ENTITY>(_multiply);
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

  protected void executeBOTHSearchDrivenByDb(final RequestContext ctx, final REQ rq, final Search28Request<DTO, TRACKING> solrRequest, final Search28Request<DTO, TRACKING> dbRequest, final ArrayList<ENTITY> finalResultList) {
    AbstractCombinedTextDatabaseSearch28RequestHandler.LOGGER.debug("DB driven combined search performed");
    dbRequest.setSortColumns(rq.getSortColumns());
    solrRequest.setLimit(rq.getLimit());
    final SearchFilter solrRequestFilter = dbRequest.getSearchFilter();
    this.searchTools.mapNames(solrRequest, this.textSearchFieldMappings);
    this.mapper.processSearchPrefixForDB(dbRequest);
    int resultsToSkip = rq.getOffset();
    int _limit = rq.getLimit();
    int _multiply = (_limit * 4);
    int increasedLimit = Math.min(_multiply, 1000);
    int iteration = 0;
    boolean doMoreSearchRequests = true;
    int foundResults = 0;
    dbRequest.setLimit(increasedLimit);
    while ((doMoreSearchRequests && (foundResults < rq.getLimit()))) {
      {
        dbRequest.setOffset((increasedLimit * iteration));
        final List<Long> refs = this.resolver.searchKey(dbRequest);
        int _size = refs.size();
        boolean _lessThan = (_size < increasedLimit);
        if (_lessThan) {
          doMoreSearchRequests = false;
        }
        boolean _isEmpty = refs.isEmpty();
        if (_isEmpty) {
          return;
        }
        LongFilter _longFilter = new LongFilter();
        final Procedure1<LongFilter> _function = (LongFilter it) -> {
          it.setFieldName("objectRef");
          it.setValueList(refs);
        };
        LongFilter _doubleArrow = ObjectExtensions.<LongFilter>operator_doubleArrow(_longFilter, _function);
        AndFilter _andFilter = new AndFilter(solrRequestFilter, _doubleArrow);
        solrRequest.setSearchFilter(_andFilter);
        int _limit_1 = rq.getLimit();
        int _minus = (_limit_1 - foundResults);
        int _plus = (_minus + resultsToSkip);
        solrRequest.setLimit(_plus);
        final List<Long> tempResult = this.textSearch.search(ctx, solrRequest, this.documentName, this.keyFieldName);
        if ((resultsToSkip > 0)) {
          int _size_1 = tempResult.size();
          boolean _greaterEqualsThan = (resultsToSkip >= _size_1);
          if (_greaterEqualsThan) {
            int _resultsToSkip = resultsToSkip;
            int _size_2 = tempResult.size();
            resultsToSkip = (_resultsToSkip - _size_2);
          } else {
            int _size_3 = tempResult.size();
            int _minus_1 = (_size_3 - resultsToSkip);
            int _limit_2 = rq.getLimit();
            int _minus_2 = (_limit_2 - foundResults);
            final int sizeToAdd = Math.min(_minus_1, _minus_2);
            this.xfer(refs, finalResultList, tempResult.subList(resultsToSkip, sizeToAdd));
            int _foundResults = foundResults;
            foundResults = (_foundResults + sizeToAdd);
            resultsToSkip = 0;
          }
        } else {
          int _size_4 = tempResult.size();
          int _limit_3 = rq.getLimit();
          int _minus_3 = (_limit_3 - foundResults);
          boolean _lessEqualsThan = (_size_4 <= _minus_3);
          if (_lessEqualsThan) {
            this.xfer(refs, finalResultList, tempResult);
            int _foundResults_1 = foundResults;
            int _size_5 = tempResult.size();
            foundResults = (_foundResults_1 + _size_5);
          } else {
            int _limit_4 = rq.getLimit();
            int _minus_4 = (_limit_4 - foundResults);
            this.xfer(refs, finalResultList, tempResult.subList(0, _minus_4));
            foundResults = rq.getLimit();
          }
        }
        int _plusPlus = iteration++;
        boolean _greaterEqualsThan_1 = (_plusPlus >= AbstractCombinedTextDatabaseSearch28RequestHandler.MAX_ITERATIONS);
        if (_greaterEqualsThan_1) {
          AbstractCombinedTextDatabaseSearch28RequestHandler.LOGGER.warn("combined search prematurely stopped due to iteration count limit");
          doMoreSearchRequests = false;
        }
      }
    }
  }

  protected void xfer(final List<Long> orderForNoSort, final ArrayList<ENTITY> finalResultList, final List<Long> temp) {
    int _size = temp.size();
    int _multiply = (2 * _size);
    final HashSet<Long> indexMap = new HashSet<Long>(_multiply);
    indexMap.addAll(temp);
    final EntityManager em = this.resolver.getEntityManager();
    for (final Long r : orderForNoSort) {
      boolean _contains = indexMap.contains(r);
      if (_contains) {
        final ENTITY ee = em.<ENTITY>find(this.resolver.getBaseJpaEntityClass(), r);
        if ((ee == null)) {
          AbstractCombinedTextDatabaseSearch28RequestHandler.LOGGER.warn("Could not find entity of key {} any more!", r);
        } else {
          finalResultList.add(ee);
        }
      }
    }
  }

  /**
   * Method splits the searchFilters in the original request into two requests that are solr only and dbOnly
   */
  protected void splitSearches(final SearchFilter originalFilter, final Search28Request<DTO, TRACKING> solrRequest, final Search28Request<DTO, TRACKING> dbRequest) {
    if ((originalFilter instanceof FieldFilter)) {
      SearchFilterTypeEnum _filterTypeForField = this.filterTypeForField(((FieldFilter)originalFilter).getFieldName());
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
          String _ret$PQON = originalFilter.ret$PQON();
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
    final Function1<AbstractCombinedTextDatabaseSearch28RequestHandler.SearchTypeMappingEntry, Boolean> _function = (AbstractCombinedTextDatabaseSearch28RequestHandler.SearchTypeMappingEntry it) -> {
      return Boolean.valueOf(this.thatMatches(it, path));
    };
    final AbstractCombinedTextDatabaseSearch28RequestHandler.SearchTypeMappingEntry entry = IterableExtensions.<AbstractCombinedTextDatabaseSearch28RequestHandler.SearchTypeMappingEntry>findFirst(this.textSearchPathElements, _function);
    if ((entry == null)) {
      return SearchFilterTypeEnum.DB_ONLY;
    }
    return entry.searchType;
  }

  /**
   * Returns true if path matches the mapping entry, depending on type of match, otherwise false.
   */
  private boolean thatMatches(final AbstractCombinedTextDatabaseSearch28RequestHandler.SearchTypeMappingEntry it, final String path) {
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

  private static final Logger LOGGER = LoggerFactory.getLogger(com.arvatosystems.t9t.base.jpa.impl.AbstractCombinedTextDatabaseSearch28RequestHandler.class);

  public AbstractCombinedTextDatabaseSearch28RequestHandler(final IResolverSurrogateKey28<REF, TRACKING, ENTITY> resolver, final IEntityMapper28<Long, DTO, TRACKING, ENTITY> mapper, final List<AbstractCombinedTextDatabaseSearch28RequestHandler.SearchTypeMappingEntry> textSearchPathElements, final Map<String, String> textSearchFieldMappings, final String documentName, final String keyFieldName, final BonaPortableClass<Search28Request<DTO, TRACKING>> bclass) {
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
    AbstractCombinedTextDatabaseSearch28RequestHandler<?, ?, ?, ?, ?> other = (AbstractCombinedTextDatabaseSearch28RequestHandler<?, ?, ?, ?, ?>) obj;
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
  public IResolverSurrogateKey28<REF, TRACKING, ENTITY> getResolver() {
    return this.resolver;
  }

  @Pure
  public IEntityMapper28<Long, DTO, TRACKING, ENTITY> getMapper() {
    return this.mapper;
  }

  @Pure
  public List<AbstractCombinedTextDatabaseSearch28RequestHandler.SearchTypeMappingEntry> getTextSearchPathElements() {
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
  public BonaPortableClass<Search28Request<DTO, TRACKING>> getBclass() {
    return this.bclass;
  }
}
