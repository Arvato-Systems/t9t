package com.arvatosystems.t9t.solr.be;

import java.io.IOException;

import com.arvatosystems.t9t.solr.be.impl.QueryBody;
import com.arvatosystems.t9t.solr.be.impl.response.QueryResponse;

public interface ISolrClient {
    /** Performs the http callout to the SOLR server. */
    QueryResponse query(String url, QueryBody query) throws IOException, InterruptedException;
}
