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
/**
 *
 */
package com.arvatosystems.t9t.httppool.be;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.pool.BasicConnFactory;
import org.apache.http.impl.pool.BasicConnPool;
import org.apache.http.impl.pool.BasicPoolEntry;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IConnection;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.google.common.base.Strings;

import de.jpaw.bonaparte.util.IMarshaller;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ByteBuilder;

/**
 * @author GOET015
 *
 */
public class HttpClientPool implements IConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientPool.class);
    private static final String REQUEST_METHOD = "POST";

    private final IMarshaller marshaller;

    private final HttpRequestExecutor httpexecutor;
    private final BasicConnPool pool;
    private final HttpHost httpHost;
    private final HttpCoreContext coreContext;
    private final HttpProcessor httpProcessor;
    private final ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;
    private final List<BasicHeader> defaultHeaders = new ArrayList<>();

    public HttpClientPool(String host, int port, int maxConnections, IMarshaller marshaller) {
        LOGGER.info("Creating a new http connection pool of {} connections for http://{}:{}",
                maxConnections, host, port);

        if (Strings.isNullOrEmpty(host)) {
            throw new T9tException(T9tException.MISSING_PARAMETER, "host");

        }

        if (port <= 0 || port > 65535) {
            throw new T9tException(T9tException.MISSING_PARAMETER, "port");

        }

        if (maxConnections <= 0) {
            throw new T9tException(T9tException.MISSING_PARAMETER, "maxConnections");

        }

        if (marshaller == null) {
            throw new T9tException(T9tException.MISSING_PARAMETER, "marshaller");

        }
        this.marshaller = marshaller;
        httpHost = new HttpHost(host, port);

        coreContext = HttpCoreContext.create();
        coreContext.setTargetHost(httpHost);

        httpexecutor = new HttpRequestExecutor(1);

        pool = new BasicConnPool(new BasicConnFactory());
        pool.setDefaultMaxPerRoute(maxConnections);
        pool.setMaxTotal(maxConnections);

        httpProcessor = HttpProcessorBuilder.create().add(new RequestContent()).add(new RequestTargetHost())
                .add(new RequestConnControl()).add(new RequestUserAgent("t9t-httppool"))
                .add(new RequestExpectContinue(true)).build();

        defaultHeaders.add(new BasicHeader("Content-Type",   marshaller.getContentType()));
        defaultHeaders.add(new BasicHeader("Accept",         marshaller.getContentType()));
        defaultHeaders.add(new BasicHeader("Charset",        "utf-8"));
        defaultHeaders.add(new BasicHeader("Accept-Charset", "utf-8"));
    }

    @Override
    public ServiceResponse executeRequest(RequestParameters rq, String requestUri, String encodedJwt) {
        return executeRequest(rq, requestUri, (encodedJwt == null ? null : "Bearer " + encodedJwt), null);
    }

    @Override
    public ServiceResponse executeRequest(RequestParameters rq, String requestUri, String authentication,
            Map<String, String> extraHttpParams) {

        ServiceResponse result = null;
        BasicPoolEntry entry = null;
        boolean reusable = false;

        try {
            Future<BasicPoolEntry> future = pool.lease(this.httpHost, null);
            entry = future.get();
            HttpClientConnection conn = entry.getConnection();

            BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest(REQUEST_METHOD, requestUri);
            LOGGER.debug("Sending object {} to request URI {}", rq.ret$PQON(), request.getRequestLine().getUri());

            if (authentication != null)
                request.setHeader(new BasicHeader("Authorization", authentication));
            if (extraHttpParams != null) {
                for (Entry<String, String> e : extraHttpParams.entrySet())
                    request.setHeader(new BasicHeader(e.getKey(), e.getValue()));
            }
            for (BasicHeader b: defaultHeaders)
                request.setHeader(b);

            ByteArray serializedRequest = marshaller.marshal(rq);
            // request.setHeader(new BasicHeader("Content-Length", "" + serializedRequest.length()));
            request.setEntity(new ByteArrayEntity(serializedRequest.getBytes()));

            httpexecutor.preProcess(request, httpProcessor, coreContext);
            HttpResponse response = httpexecutor.execute(request, conn, coreContext);
            httpexecutor.postProcess(response, httpProcessor, coreContext);
            LOGGER.debug("Received response {}", response.getStatusLine());

            reusable = connStrategy.keepAlive(response, coreContext);

            InputStream is = response.getEntity().getContent();
            ByteBuilder serializedResponse = new ByteBuilder();
            serializedResponse.readFromInputStream(is, 0);
            is.close();

            result = serializedResponse.length() == 0 ? null
                    : (ServiceResponse) marshaller.unmarshal(serializedResponse);

        } catch (Exception ex) {
            LOGGER.error("Request to {} failed due to {}: {}", this.httpHost, ex.getClass().getSimpleName(), ex.getMessage());
            throw new T9tException(T9tException.GENERAL_EXCEPTION, ex.getMessage());
        } finally {
            LOGGER.debug("Connection is {}reusable", reusable ? "" : "NOT ");

            if (entry != null) {
                pool.release(entry, reusable);
            }
        }

        return result;
    }
}
