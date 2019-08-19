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

import com.arvatosystems.t9t.solr.be.ISolrServerCache
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Singleton
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.HttpSolrClient

/**
 * A cache for http SOLR server instances. They have to be reused, creating servers for each and every request is quite slow and may lead to connection leaks
 */
@AddLogger
@Singleton
class SolrServerCache implements ISolrServerCache {
    //val Map<String, SolrServer> serverCache = new ConcurrentHashMap<String, SolrServer>
    val Cache<String, SolrClient> serverCache = CacheBuilder.newBuilder.build
//     [ url |
//        LOGGER.info('''Creating new SOLR client for URL «url»''')
//        new HttpSolrServer(url)
//    ];

    override get(String solrCoreUrl) {

        return serverCache.get(solrCoreUrl) [
            LOGGER.info('''Creating new SOLR client for URL «solrCoreUrl»''')
            new HttpSolrClient(solrCoreUrl)
        ];
    }
}
