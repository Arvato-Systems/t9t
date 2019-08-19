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
package com.arvatosystems.t9t.solr.be.impl

import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.search.SearchCriteria
import com.arvatosystems.t9t.base.services.ITextSearch
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.cfg.be.ConfigProvider
import com.arvatosystems.t9t.solr.be.IFilterToSolrConverter
import com.arvatosystems.t9t.solr.be.ISolrServerCache
import com.arvatosystems.t9t.solr.services.ISolrModuleCfgDtoResolver
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton
import de.jpaw.util.ApplicationException
import java.util.List
import org.apache.solr.client.solrj.SolrQuery

@AddLogger
@Singleton
@Named("SOLR")
class SolrEngine implements ITextSearch {

    new() {
        LOGGER.info("SOLR search engine configured - default URL is {}",
            ConfigProvider.getConfiguration.searchConfiguration?.defaultUrl ?: "NOT configured in config.xml"
        );
    }

    @Inject protected ISolrServerCache              solrServerCache
    @Inject protected ISolrModuleCfgDtoResolver     moduleCfgResolver
    @Inject protected IFilterToSolrConverter        filterToSolrConverter

    override List<Long> search(
        RequestContext ctx,
        SearchCriteria sc,
        String documentName,
        String resultFieldName
    ) {
        if (documentName === null)
            throw new ApplicationException(T9tException.NO_DOCUMENT_NAME_DEFINED)

        val moduleCfg = moduleCfgResolver.moduleConfiguration
        val solrBaseUrl = moduleCfg.baseUrl ?: ConfigProvider.getConfiguration.searchConfiguration?.defaultUrl

        if (solrBaseUrl === null)
            throw new ApplicationException(T9tException.MISSING_CONFIGURATION, "No SEARCH base URL defined, neither in config.xml nor in database")

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SOLR search on {} (offset={}, limit={}) with filter criteria {}",
                documentName,
                sc.offset,
                sc.getLimit(),
                sc.searchFilter ?: "NONE");
        }

        // Get the server that provides access to the SOLR core
        val solrServer = solrServerCache.get(solrBaseUrl + "/" + documentName);

        // create the query
        val solrQuery = new SolrQuery => [
            start = sc.offset
            rows  = if (sc.limit > 0) sc.limit else Integer.MAX_VALUE
            query = sc.expression ?: filterToSolrConverter.toSolrCondition(sc.searchFilter) ?: "*"
        ]

        if (sc.sortColumns !== null) {
            for (sortOrder : sc.sortColumns) {

                // Sorting will ALWAYS be done on a field with the suffix "_sort" !!
                solrQuery.addSort(sortOrder.fieldName + "_sort", if (sortOrder.descending) SolrQuery.ORDER.desc else SolrQuery.ORDER.asc)
            }
        }

        // always add a filter on the requesters tenant_ref
        solrQuery.filterQueries = T9tConstants.TENANT_REF_FIELD_NAME + ":" + ctx.tenantRef

        LOGGER.debug("SOLR expression is {}", solrQuery.toString)

        val solrResponse = solrServer.query(solrQuery)
        LOGGER.debug("SOLR query took {} ms overall, pure query time {} ms, and returned {} results",
            solrResponse.elapsedTime, solrResponse.QTime, solrResponse.results.numFound
        )

        // map the results.... always expect an object ref
        return solrResponse.results.map[getFieldValue(resultFieldName) as Long].toList
    }
}
