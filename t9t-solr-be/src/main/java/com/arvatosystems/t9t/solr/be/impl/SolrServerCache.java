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
package com.arvatosystems.t9t.solr.be.impl;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.solr.be.ISolrServerCache;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.jpaw.dp.Singleton;
import java.util.concurrent.ExecutionException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SolrServerCache implements ISolrServerCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrServerCache.class);
    protected final Cache<String, SolrClient> serverCache = CacheBuilder.newBuilder().build();

    @Override
    public SolrClient get(final String solrCoreUrl) {
        try {
            return serverCache.get(solrCoreUrl, () -> {
                LOGGER.info("Creating new SOLR client for URL " + solrCoreUrl);
                return new HttpSolrClient.Builder(solrCoreUrl).build();
            });
        } catch (ExecutionException e) {
            throw new T9tException(T9tException.SOLR_EXCEPTION, "Error while creating SOLR client for URL " + solrCoreUrl);
        }
    }
}
