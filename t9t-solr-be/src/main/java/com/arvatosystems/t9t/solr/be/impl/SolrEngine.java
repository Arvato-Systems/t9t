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

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.ITextSearch;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.SearchConfiguration;
import com.arvatosystems.t9t.solr.SolrModuleCfgDTO;
import com.arvatosystems.t9t.solr.be.IFilterToSolrConverter;
import com.arvatosystems.t9t.solr.be.ISolrServerCache;
import com.arvatosystems.t9t.solr.services.ISolrModuleCfgDtoResolver;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named("SOLR")
public class SolrEngine implements ITextSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrEngine.class);
    protected final ISolrServerCache solrServerCache = Jdp.getRequired(ISolrServerCache.class);
    protected final ISolrModuleCfgDtoResolver solrModuleCfgDtoResolver = Jdp.getRequired(ISolrModuleCfgDtoResolver.class);
    protected final IFilterToSolrConverter filterToSolrConverter = Jdp.getRequired(IFilterToSolrConverter.class);

    public SolrEngine() {
        final SearchConfiguration sc = ConfigProvider.getConfiguration().getSearchConfiguration();
        final String url = sc == null ? null : sc.getDefaultUrl();
        LOGGER.info("SOLR search engine configured - default URL is {}", url == null ? "NOT configured in config.xml" : url);
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
                throw new T9tException(T9tException.MISSING_CONFIGURATION, "No SEARCH base URL defined, neither in config.xml nor in database");
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SOLR search on {} (offset={}, limit={}) with filter criteria {}", documentName, sc.getOffset(), sc.getLimit(),
                        sc.getSearchFilter() == null ? "NONE" : sc.getSearchFilter());
            }

            // Get the server that provides access to the SOLR core
            final SolrClient solrServer = solrServerCache.get(solrBaseUrl + "/" + documentName);

            // create the query
            final SolrQuery solrQuery = new SolrQuery();
            solrQuery.setStart(sc.getOffset());
            solrQuery.setRows(sc.getLimit() > 0 ? sc.getLimit() : Integer.MAX_VALUE);
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

            if (sc.getSortColumns() != null) {
                for (SortColumn col : sc.getSortColumns()) {
                    // Sorting will ALWAYS be done on a field with the suffix "_sort" !!
                    final ORDER order = col.getDescending() ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc;
                    solrQuery.addSort(col.getFieldName() + "_sort", order);
                }
            }

            // always add a filter on the requesters tenant_ref
            solrQuery.setFilterQueries(T9tConstants.TENANT_REF_FIELD_NAME + ":" + ctx.tenantRef);

            LOGGER.debug("SOLR expression is {}", solrQuery.toString());

            final QueryResponse solrResponse = solrServer.query(solrQuery);
            LOGGER.debug("SOLR query took {} ms overall, pure query time {} ms, and returned {} results", solrResponse.getElapsedTime(),
                    solrResponse.getQTime(), solrResponse.getResults().getNumFound());

            // map the results.... always expect an object ref
            return solrResponse.getResults().stream().map((SolrDocument solrDoc) -> (Long) solrDoc.getFieldValue(resultFieldName)).collect(Collectors.toList());
        } catch (Throwable _e) {
            throw new T9tException(T9tException.SOLR_EXCEPTION, "error while performing query on SOLR");
        }
    }

    private String getDefaultBaseUrl() {
        return ConfigProvider.getConfiguration().getSearchConfiguration() == null ? null
                : ConfigProvider.getConfiguration().getSearchConfiguration().getDefaultUrl();
    }
}
