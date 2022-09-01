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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.solr.be.ISolrServerCache;

import de.jpaw.dp.Singleton;

@Singleton
public class SolrServerCache implements ISolrServerCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrServerCache.class);
    protected final Map<String, SolrClient> serverCache = new ConcurrentHashMap<>();

    @Override
    public SolrClient get(final String solrCoreUrl) {
        return serverCache.computeIfAbsent(solrCoreUrl, unused -> {
            LOGGER.info("Creating new SOLR client for URL " + solrCoreUrl);
            return new HttpSolrClient.Builder(solrCoreUrl).build();
        });
    }
}
