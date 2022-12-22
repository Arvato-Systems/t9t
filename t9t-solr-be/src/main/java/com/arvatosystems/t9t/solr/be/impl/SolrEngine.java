/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.solr.be.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.ITextSearch;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.SearchConfiguration;
import com.arvatosystems.t9t.solr.SolrModuleCfgDTO;
import com.arvatosystems.t9t.solr.be.IFilterToSolrConverter;
import com.arvatosystems.t9t.solr.be.ISolrClient;
import com.arvatosystems.t9t.solr.be.impl.response.QueryResponse;
import com.arvatosystems.t9t.solr.services.ISolrModuleCfgDtoResolver;

import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("SOLR")
public class SolrEngine implements ITextSearch {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrEngine.class);

    protected final ISolrModuleCfgDtoResolver solrModuleCfgDtoResolver = Jdp.getRequired(ISolrModuleCfgDtoResolver.class);
    protected final IFilterToSolrConverter    filterToSolrConverter    = Jdp.getRequired(IFilterToSolrConverter.class);
    protected final ISolrClient               solrClient               = Jdp.getRequired(ISolrClient.class);

    public SolrEngine() {
        final SearchConfiguration sc = ConfigProvider.getConfiguration().getSearchConfiguration();
        final String url = sc == null ? null : sc.getDefaultUrl();
        LOGGER.info("SOLR search engine configured - default URL is {}",
                url == null ? "NOT configured in config.xml" : url);
    }

    @Override
    public List<Long> search(final RequestContext ctx, final SearchCriteria sc, final String documentName, final String resultFieldName) {
        try {
            if (documentName == null) {
                throw new T9tException(T9tException.NO_DOCUMENT_NAME_DEFINED);
            }

            final SolrModuleCfgDTO moduleCfg = solrModuleCfgDtoResolver.getModuleConfiguration();
            final String solrBaseUrl = moduleCfg.getBaseUrl() == null ? getDefaultBaseUrl() : moduleCfg.getBaseUrl();

            if (solrBaseUrl == null) {
                throw new T9tException(T9tException.MISSING_CONFIGURATION,
                        "No SEARCH base URL defined, neither in config.xml nor in database");
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SOLR search on {} (offset={}, limit={}) with filter criteria {} and sort {}", documentName,
                        sc.getOffset(), sc.getLimit(),
                        sc.getSearchFilter() == null ? "NONE" : sc.getSearchFilter(),
                        sc.getSortColumns()  == null ? "NONE" : ToStringHelper.toStringSL(sc.getSortColumns())
                );
            }

            final QueryResponse solrResponse = solrClient.query(solrBaseUrl + "/" + documentName + "/query", buildSolrQueryBody(ctx, sc));

            LOGGER.debug("SOLR query took {} ms overall, pure query time {} ms, and returned {} results",
                    solrResponse.getElapsedTime(), solrResponse.getQTime(), solrResponse.getResults().size());

            // map the results.... always expect an object ref
            ArrayList<Object> results = solrResponse.getResults();
            return mapResults(resultFieldName, results);

        } catch (Throwable _e) {
            throw new T9tException(T9tException.SOLR_EXCEPTION, "error while performing query on SOLR");
        }
    }

    /** Constructs the search specification object passwed to the SOLR engine from the t9t SearchCriteria. */
    protected QueryBody buildSolrQueryBody(final RequestContext ctx, final SearchCriteria sc) {
        // create the query
        final QueryBody solrQuery = new QueryBody();
        solrQuery.setOffset(sc.getOffset());
        solrQuery.setLimit(sc.getLimit() > 0 ? sc.getLimit() : Integer.MAX_VALUE);
        String query = sc.getExpression();
        if (query == null) {
            final String query2 = filterToSolrConverter.toSolrCondition(sc.getSearchFilter());
            if (query2 == null) {
                query = "*";
            } else {
                query = query2;
            }
        }
        solrQuery.setQuery(query);

        solrQuery.setSort("");  // never leave it null
        if (sc.getSortColumns() != null) {
            final StringBuilder sb = new StringBuilder(1000);
            for (SortColumn col : sc.getSortColumns()) {
                if (sb.length() > 0) {
                    // append to previous
                    sb.append(',').append(' ');
                }
                // Sorting will ALWAYS be done on a field with the suffix "_sort" !!
                sb.append(col.getFieldName()).append("_sort").append(col.getDescending() ? " desc" : " asc");
            }
            solrQuery.setSort(sb.toString());  // use the real sort criteria
        }

        // always add a filter on the requesters tenant_ref
        solrQuery.setFilter(T9tConstants.TENANT_ID_FIELD_NAME + ":" + ctx.tenantId);
        return solrQuery;
    }

    protected List<Long> mapResults(final String resultFieldName, final ArrayList<Object> results) {
        ArrayList<Long> refs = new ArrayList<Long>(results.size());

        for (Object solrDocument : results) {
            Long ref = getRef(resultFieldName, solrDocument);
            if (ref != null)
                refs.add(ref);
        }

        return refs;
    }

    protected Long getRef(final String resultFieldName, Object solrDocument) {
        if (solrDocument instanceof Map<?, ?> map) {
            Object field = map.get(resultFieldName);

            if (field instanceof Long fLong) {
                // correct type, no conversion required
                return fLong;
            }
            if (field instanceof Number fNumber) {
                // requires unboxing / autoboxing
                return fNumber.longValue();
            }
        }
        return null;
    }

    protected String getDefaultBaseUrl() {
        return ConfigProvider.getConfiguration().getSearchConfiguration() == null ? null
                : ConfigProvider.getConfiguration().getSearchConfiguration().getDefaultUrl();
    }
}
