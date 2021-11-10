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

import javax.persistence.EntityManager;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * In case of no sort but SOLR filter criteria, the ordering of the SOLR result is preserved
 * because it is assumed that it reflects the phonetic match quality (relevance).
 */
public abstract class AbstractCombinedTextDatabaseSearchRequestHandler<
  REF extends Ref,
  DTO extends REF,
  TRACKING extends TrackingBase,
  REQ extends SearchRequest<DTO, TRACKING>,
  ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>
> extends AbstractSearchRequestHandler<REQ> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCombinedTextDatabaseSearchRequestHandler.class);

    static final int MAX_ITERATIONS = 50; // limit the number of loop iterations. Will produce results too small, but that
                                          // is better than choking the system.
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);
    protected final ISearchTools searchTools = Jdp.getRequired(ISearchTools.class);
    protected final ITextSearch textSearch = Jdp.getRequired(ITextSearch.class);
    protected final IResolverSurrogateKey42<REF, TRACKING, ENTITY> resolver;
    protected final IEntityMapper42<Long, DTO, TRACKING, ENTITY> mapper;
    protected final List<SearchTypeMappingEntry> textSearchPathElements;
    protected final Map<String, String> textSearchFieldMappings;
    protected final String documentName;
    protected final String keyFieldName;
    protected final BonaPortableClass<SearchRequest<DTO, TRACKING>> bclass;

    public AbstractCombinedTextDatabaseSearchRequestHandler(final IResolverSurrogateKey42<REF, TRACKING, ENTITY> resolver,
            final IEntityMapper42<Long, DTO, TRACKING, ENTITY> mapper,
            final List<AbstractCombinedTextDatabaseSearchRequestHandler.SearchTypeMappingEntry> textSearchPathElements,
            final Map<String, String> textSearchFieldMappings, final String documentName, final String keyFieldName,
            final BonaPortableClass<SearchRequest<DTO, TRACKING>> bclass) {
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
    public ReadAllResponse<DTO, TRACKING> execute(final RequestContext ctx, final REQ rq) throws Exception {
        // if the search is done by SOLR expression, it can do SOLR only - no other
        // analysis required
        if (rq.getExpression() != null && !rq.getExpression().isEmpty()) {
            return executeSOLRSearch(ctx, rq);
        }
        final SearchFilterTypes filterTypes = new SearchFilterTypes();
        if (rq.getSearchFilter() != null) {
            decideFilterAssociation(rq.getSearchFilter(), filterTypes);
        }
        // determine type of sort as well
        SearchFilterTypeEnum sortType = null;
        if (rq.getSortColumns() != null && !rq.getSortColumns().isEmpty()) {
            sortType = filterTypeForField(rq.getSortColumns().get(0).getFieldName());
        }
        LOGGER.debug("Filters indicate search types {} and sort type {} for expression {}", filterTypes, sortType, rq.getSearchFilter());

        // If no filter is SOLR only, we run a pure DB search. It is assumed that the DB
        // can process all sort criteria.
        if (!filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY) && sortType != SearchFilterTypeEnum.SOLR_ONLY)
            return executeDBSearch(rq);
        if (!filterTypes.contains(SearchFilterTypeEnum.DB_ONLY) && sortType != SearchFilterTypeEnum.DB_ONLY)
            return executeSOLRSearch(ctx, rq);

        // do combined searches
        final SearchRequest<DTO, TRACKING> solrRequest = bclass.newInstance();
        final SearchRequest<DTO, TRACKING> dbRequest = bclass.newInstance();
        splitSearches(rq.getSearchFilter(), solrRequest, dbRequest);
        LOGGER.debug("Original request {} split to Solr Rq {} and DB rq {}", rq, solrRequest, dbRequest);
        if (solrRequest.getSearchFilter() == null && dbRequest.getSearchFilter() == null) {
            throw new T9tException(T9tException.ILE_SOLR_DB_COMBINED_FILTERS);
        }

        final ArrayList<ENTITY> finalResultList = new ArrayList<>(rq.getLimit());
        if (sortType == null) {
            // must do SOLR driven search in order to get ordering by relevance
            executeBOTHSearchDrivenBySolr(ctx, rq, solrRequest, dbRequest, finalResultList);
        } else if (sortType == SearchFilterTypeEnum.DB_ONLY && filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY) && dbRequest.getSearchFilter() == null) {
            // the UNSOLVABLE one: all filters are SOLR, but we have to sort be DB.
            // this only works if we can get all results at once
            return executeSOLRSearchWithDbSort(ctx, rq);
        } else if (sortType == SearchFilterTypeEnum.DB_ONLY
                || (!filterTypes.contains(SearchFilterTypeEnum.SOLR_ONLY) && !filterTypes.contains(SearchFilterTypeEnum.BOTH))) {
            // must do DB driven search if sort is by DB_ONLY criteria, and should do if no
            // SOLR filter available
            executeBOTHSearchDrivenByDb(ctx, rq, solrRequest, dbRequest, finalResultList);
        } else {
            // default: Solr driven combined query
            executeBOTHSearchDrivenBySolr(ctx, rq, solrRequest, dbRequest, finalResultList);
        }
        return mapper.createReadAllResponse(finalResultList, rq.getSearchOutputTarget());
    }

    // optional additional mappings not replaced by processSearchPrefixForDB (due to
    // child objects)
    // workaround.
    protected Map<String, String> getExtraMappings() {
        return null;
    }

    protected void processPrefixes(final SearchCriteria rq) {
        mapper.processSearchPrefixForDB(rq); // convert the field with searchPrefix
        // the regular mapper is not sufficient here, because for SOLR searches, we have
        // frequent occurrences of double Refs.
        // we perform a hack here, replacing remaining occurrences of "Ref." by ".",
        // which is the correct replacement in most cases.
        final Map<String, String> mappings = getExtraMappings();
        if (mappings != null) {
            searchTools.mapNames(rq, (path) -> {
                if (path.contains("Ref.")) {
                    String tmp = path;
                    for (final Map.Entry<String, String> r : mappings.entrySet()) {
                        tmp = tmp.replace(r.getKey(), r.getValue());
                        return tmp;
                    }
                }
                return path;
            });

        }
    }

    /**
     * DB Only search
     */
    protected ReadAllResponse<DTO, TRACKING> executeDBSearch(final REQ rq) throws Exception {
        // database search
        // preprocess prefixes for DB
        LOGGER.debug("Using DB only search");
        processPrefixes(rq);
        // delegate to database search
        return mapper.createReadAllResponse(resolver.search(rq, null), rq.getSearchOutputTarget());
    }

    /**
     * SOLR only search
     */
    protected ReadAllResponse<DTO, TRACKING> executeSOLRSearch(final RequestContext ctx, final REQ rq) throws Exception {
        // preprocess args for SOLR
        searchTools.mapNames(rq, textSearchFieldMappings);
        LOGGER.debug("SOLR request mapped: {}", rq);
        final List<Long> refs = textSearch.search(ctx, rq, documentName, keyFieldName);

        // end here if there are no results - DB query would return an error
        if (refs.isEmpty()) {
            final ReadAllResponse<DTO, TRACKING> response = new ReadAllResponse<>();
            response.setDataList(Collections.emptyList());
        }

        // obtain DTOs for the refs
        final SearchRequest<DTO, TRACKING> newSearchRq = this.bclass.newInstance();
        newSearchRq.setSearchFilter(new LongFilter("objectRef", null, null, null, refs));
        newSearchRq.setSortColumns(rq.getSortColumns());
        newSearchRq.setSearchOutputTarget(rq.getSearchOutputTarget());
        LOGGER.debug("solr only new searchReq: {}", newSearchRq);
        processPrefixes(newSearchRq); // convert the field with searchPrefix
        return mapper.createReadAllResponse(resolver.search(newSearchRq, null), newSearchRq.getSearchOutputTarget());
    }

    protected ReadAllResponse<DTO, TRACKING> executeSOLRSearchWithDbSort(final RequestContext ctx, final REQ rq) throws Exception {
        // preprocess args for SOLR
        final List<SortColumn> sortCols = rq.getSortColumns();
        rq.setSortColumns(null);
        searchTools.mapNames(rq, textSearchFieldMappings);
        LOGGER.debug("SOLR request mapped: {}", rq);
        final List<Long> refs = textSearch.search(ctx, rq, documentName, keyFieldName);

        // end here if there are no results - DB query would return an error
        if (refs.isEmpty()) {
            final ReadAllResponse<DTO, TRACKING> response = new ReadAllResponse<>();
            response.setDataList(Collections.emptyList());
        }
        // obtain DTOs for the refs
        final SearchRequest<DTO, TRACKING> newSearchRq = this.bclass.newInstance();
        newSearchRq.setSearchFilter(new LongFilter("objectRef", null, null, null, refs));
        newSearchRq.setSortColumns(sortCols);
        newSearchRq.setSearchOutputTarget(rq.getSearchOutputTarget());

        LOGGER.debug("solr only with DB sort new searchReq for DB: {}", newSearchRq);
        processPrefixes(newSearchRq); // convert the field with searchPrefix
        return mapper.createReadAllResponse(resolver.search(newSearchRq, null), newSearchRq.getSearchOutputTarget());
    }

    // when this method is called, there are filter criteria which are SOLR only as
    // well as some which are DB only, or DB only sort criteria
    protected void executeBOTHSearchDrivenBySolr(final RequestContext ctx, final REQ rq, final SearchRequest<DTO, TRACKING> solrRequest,
      final SearchRequest<DTO, TRACKING> dbRequest, final ArrayList<ENTITY> finalResultList) {
        LOGGER.debug("SOLR driven combined search performed");
        final boolean byRelevance = rq.getSortColumns() == null || rq.getSortColumns().isEmpty();
        solrRequest.setSortColumns(rq.getSortColumns());
        dbRequest.setSortColumns(rq.getSortColumns());
        dbRequest.setLimit(rq.getLimit());
        final SearchFilter dbRequestFilter = dbRequest.getSearchFilter(); // save for later use
        searchTools.mapNames(solrRequest, textSearchFieldMappings);
        processPrefixes(dbRequest);

        int resultsToSkip = rq.getOffset();
        final int increasedLimit = Math.min(rq.getLimit() * 4, 1000); // use a higher limit because we expect to lose some when intersecting with the
                                                                // DB results, but respect Oracle's limitations
        int iteration = 0;
        boolean doMoreSearchRequests = true; // set to false once we know SOLR will not return more results
        int foundResults = 0;
        solrRequest.setLimit(increasedLimit);
        // iterate SOLR requests until we have enough data
        while (doMoreSearchRequests && foundResults < rq.getLimit()) {
            solrRequest.setOffset(increasedLimit * iteration); // consecutively increase the solr offset to include more results if they are
                                                               // needed

            final List<Long> refs = textSearch.search(ctx, solrRequest, documentName, keyFieldName);
            if (refs.size() < increasedLimit) {
                doMoreSearchRequests = false; // end of data
            }

            // just return finalResultList if no more refs can be found with the given
            // offset/limit (shortcut)
            if (refs.isEmpty())
                return;
            dbRequest.setSearchFilter(new AndFilter(dbRequestFilter, new LongFilter("objectRef", null, null, null, refs)));
            // only query INITIAL_CALLER_LIMIT - RESULTS_FOUND + RESULTS_TO_SKIP. This will
            // ensure we always get enough because a certain amount has to be skipped
            dbRequest.setLimit(rq.getLimit() - foundResults + resultsToSkip);

            // execute search and save result
            final List<ENTITY> tempResult = resolver.search(dbRequest, null);

            if (resultsToSkip > 0) { // if the caller supplied an offset we have to skip the first X (offset) results
                if (resultsToSkip >= tempResult.size()) {
                    // skip everything!
                    resultsToSkip -= tempResult.size();
                } else {
                    // skip some, add some
                    final int sizeToAdd = Math.min(tempResult.size() - resultsToSkip, rq.getLimit() - foundResults);
                    xfer(refs, finalResultList, tempResult.subList(resultsToSkip, sizeToAdd), byRelevance);
                    foundResults += sizeToAdd;
                    resultsToSkip = 0;
                }
            } else {
                // add everything we got (this could be an empty list)
                if (tempResult.size() <= rq.getLimit() - foundResults) {
                    // add everything we got
                    xfer(refs, finalResultList, tempResult, byRelevance);
                    foundResults += tempResult.size(); // after dropping enough from the caller's offset we can save the results
                } else {
                    // add the missing entries and we are done
                    xfer(refs, finalResultList, tempResult.subList(0, rq.getLimit() - foundResults), byRelevance);
                    foundResults = rq.getLimit();
                }
            }
            if (iteration++ >= MAX_ITERATIONS) { // increase iteration count, and check for security stop
                LOGGER.warn("combined search prematurely stopped due to iteration count limit");
                doMoreSearchRequests = false;
            }
        }
    }

    protected void executeBOTHSearchDrivenByDb(final RequestContext ctx, final REQ rq, final SearchRequest<DTO, TRACKING> solrRequest,
      final SearchRequest<DTO, TRACKING> dbRequest, final ArrayList<ENTITY> finalResultList) {
        LOGGER.debug("DB driven combined search performed");
        dbRequest.setSortColumns(rq.getSortColumns()); // ask (only) DB to sort
        solrRequest.setLimit(rq.getLimit());
        final SearchFilter solrRequestFilter = dbRequest.getSearchFilter(); // save for later use
        searchTools.mapNames(solrRequest, textSearchFieldMappings);
        processPrefixes(dbRequest);

        int resultsToSkip = rq.getOffset();
        // use a higher limit because we expect to lose some when intersecting with the
        // DB results, but respect Oracle's limitations
        final int increasedLimit = Math.min(rq.getLimit() * 4, 1000);
        int iteration = 0;
        boolean doMoreSearchRequests = true; // set to false once we know SOLR will not return more results
        int foundResults = 0;
        dbRequest.setLimit(increasedLimit);

        // iterate SOLR requests until we have enough data
        while (doMoreSearchRequests && foundResults < rq.getLimit()) {
            dbRequest.setOffset(increasedLimit * iteration); // consecutively increase the solr offset to include more results if they are
                                                             // needed

            final List<Long> refs = resolver.searchKey(dbRequest);
            if (refs.size() < increasedLimit) {
                doMoreSearchRequests = false; // end of data
            }

            // just return finalResultList if no more refs can be found with the given
            // offset/limit (shortcut)
            if (refs.isEmpty()) {
                return;
            }
            solrRequest.setSearchFilter(new AndFilter(solrRequestFilter, new LongFilter("objectRef", null, null, null, refs)));
            // only query INITIAL_CALLER_LIMIT - RESULTS_FOUND + RESULTS_TO_SKIP.
            // This will ensure we always get enough because a certain amount has to be
            // skipped
            solrRequest.setLimit(rq.getLimit() - foundResults + resultsToSkip);

            // execute search and save result
            final List<Long> tempResult = textSearch.search(ctx, solrRequest, documentName, keyFieldName);

            if (resultsToSkip > 0) { // if the caller supplied an offset we have to skip the first X (offset) results
                if (resultsToSkip >= tempResult.size()) {
                    // skip everything!
                    resultsToSkip -= tempResult.size();
                } else {
                    // skip some, add some
                    final int sizeToAdd = Math.min(tempResult.size() - resultsToSkip, rq.getLimit() - foundResults);
                    xfer(refs, finalResultList, tempResult.subList(resultsToSkip, sizeToAdd));
                    foundResults += sizeToAdd;
                    resultsToSkip = 0;
                }
            } else {
                // add everything we got (this could be an empty list)
                if (tempResult.size() <= rq.getLimit() - foundResults) {
                    // add everything we got
                    xfer(refs, finalResultList, tempResult);
                    foundResults += tempResult.size(); // after dropping enough from the caller's offset we can save the results
                } else {
                    // add the missing entries and we are done
                    xfer(refs, finalResultList, tempResult.subList(0, rq.getLimit() - foundResults));
                    foundResults = rq.getLimit();
                }
            }
            if (iteration++ >= MAX_ITERATIONS) { // increase iteration count, and check for security stop
                LOGGER.warn("combined search prematurely stopped due to iteration count limit");
                doMoreSearchRequests = false;
            }
        }
    }

    // add the entities of the temporary result in order, or, if that search was
    // done without sort, in order of the input refs
    protected void xfer(final List<Long> orderForNoSort, final ArrayList<ENTITY> finalResultList, final List<Long> temp) {
        // build a set from the list entries
        final Set<Long> indexMap = new HashSet<>(2 * temp.size());
        indexMap.addAll(temp);
        final EntityManager em = resolver.getEntityManager();
        for (final Long r : orderForNoSort) {
            if (indexMap.contains(r)) {
                final ENTITY ee = em.find(resolver.getBaseJpaEntityClass(), r);
                if (ee == null) {
                    LOGGER.warn("Could not find entity of key {} any more!", r);
                } else {
                    finalResultList.add(ee);
                }
            }
        }
    }

    // add the entities of the temporary result in order, or, if that search was
    // done without sort, in order of the input refs
    protected void xfer(final List<Long> orderForNoSort, final ArrayList<ENTITY> finalResultList, final List<ENTITY> temp, final boolean byRelevance) {
        if (!byRelevance)
            finalResultList.addAll(temp);
        else {
            // build a map
            final Map<Long, ENTITY> indexMap = new HashMap<>(2 * temp.size());
            for (final ENTITY e : temp) {
                indexMap.put(e.ret$Key(), e);
            }
            for (final Long r : orderForNoSort) {
                final ENTITY ee = indexMap.get(r);
                if (ee != null)
                    finalResultList.add(ee);
            }
        }
    }

    /**
     * Method splits the searchFilters in the original request into two requests
     * that are solr only and dbOnly
     */
    protected void splitSearches(final SearchFilter originalFilter, final SearchRequest<DTO, TRACKING> solrRequest,
      final SearchRequest<DTO, TRACKING> dbRequest) {
        if (originalFilter instanceof FieldFilter) {
            switch (filterTypeForField(((FieldFilter) originalFilter).getFieldName())) {
            case DB_ONLY:
                dbRequest.setSearchFilter(SearchFilters.and(dbRequest.getSearchFilter(), originalFilter));
            case SOLR_ONLY:
                solrRequest.setSearchFilter(SearchFilters.and(solrRequest.getSearchFilter(), originalFilter));
            default: { // merge the filter into BOTH searches!
                dbRequest.setSearchFilter(SearchFilters.and(dbRequest.getSearchFilter(), originalFilter));
                solrRequest.setSearchFilter(SearchFilters.and(solrRequest.getSearchFilter(), originalFilter));
            }
            }
        } else if (originalFilter instanceof AndFilter) {
            splitSearches(((AndFilter) originalFilter).getFilter1(), solrRequest, dbRequest);
            splitSearches(((AndFilter) originalFilter).getFilter2(), solrRequest, dbRequest);
        }
    }

    /**
     * Determines which search engine(s) can process all filters. Also validates
     * that only AND conditions are used. For performance reasons, the result is
     * passed in by reference (avoids GC overhead due to temp object constructions).
     * The passed in parameters may not be null.
     */
    protected void decideFilterAssociation(final SearchFilter originalFilter, final SearchFilterTypes result) {
        if (originalFilter instanceof FieldFilter) {
            result.add(filterTypeForField(((FieldFilter) originalFilter).getFieldName()));
        } else if (originalFilter instanceof NotFilter) {
            decideFilterAssociation(((NotFilter) originalFilter).getFilter(), result);
        } else if (originalFilter instanceof AndFilter) {
            decideFilterAssociation(((AndFilter) originalFilter).getFilter1(), result);
            decideFilterAssociation(((AndFilter) originalFilter).getFilter2(), result);
        } else {
            throw new T9tException(T9tException.ILLEGAL_SOLR_DB_COMBINED_FILTER_EXPRESSION, originalFilter.ret$PQON());
        }
    }

    /**
     * Determines which search engine(s) can process a certain field. The data is
     * available in the list textSearchPathElements. If no entry can be found, a
     * default of DBONLY is assumed. Never returns null.
     */
    protected SearchFilterTypeEnum filterTypeForField(final String path) {
        final SearchTypeMappingEntry entry = textSearchPathElements.stream().filter(e -> thatMatches(e, path)).findFirst().orElse(null);
        if (entry == null)
            return SearchFilterTypeEnum.DB_ONLY;
        return entry.searchType;
    }

    /**
     * Returns true if path matches the mapping entry, depending on type of match,
     * otherwise false.
     */
    private boolean thatMatches(final SearchTypeMappingEntry it, final String path) {
        switch (it.matchType) {
        case EXACT:
            return path == it.name;
        case START:
            return path.startsWith(it.name);
        case SUBSTRING:
            return path.contains(it.name);
        }
        return false;
    }

    /**
     * List entry for a prefix which defines if a field is SOLR only, DB only, or
     * both search engines can evaluate the field.
     * FIXME: All boilerplate, use record once we have Java 17!!!
     */
    public static class SearchTypeMappingEntry {
        protected final String name; // the path prefix (or substring)
        protected final SearchFilterTypeEnum searchType; // defines which search type can filter by this expression
        protected final SearchFilterMatchTypeEnum matchType; // if true, then the pattern may occur anywhere, normally the path must start

        public SearchTypeMappingEntry(final String name, final SearchFilterTypeEnum searchType, final SearchFilterMatchTypeEnum matchType) {
            this.name = name;
            this.searchType = searchType;
            this.matchType = matchType;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
            result = prime * result + ((this.searchType == null) ? 0 : this.searchType.hashCode());
            return prime * result + ((this.matchType == null) ? 0 : this.matchType.hashCode());
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final SearchTypeMappingEntry other = (SearchTypeMappingEntry) obj;
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
        public String toString() {
            final ToStringBuilder b = new ToStringBuilder(this);
            b.add("name", this.name);
            b.add("searchType", this.searchType);
            b.add("matchType", this.matchType);
            return b.toString();
        }

        public String getName() {
            return this.name;
        }

        public SearchFilterTypeEnum getSearchType() {
            return this.searchType;
        }

        public SearchFilterMatchTypeEnum getMatchType() {
            return this.matchType;
        }
    }
}
