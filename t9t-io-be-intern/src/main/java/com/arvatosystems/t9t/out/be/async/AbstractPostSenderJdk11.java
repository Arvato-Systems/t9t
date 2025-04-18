/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.out.be.async;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpTimeoutException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.arvatosystems.t9t.io.oauth.AccessTokenDTO;
import com.arvatosystems.t9t.out.services.oauth.IAccessTokenClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncHttpResponse;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.CommunicationTargetChannelType;
import com.arvatosystems.t9t.io.DataReference;
import com.arvatosystems.t9t.io.InMemoryMessage;
import com.arvatosystems.t9t.out.services.IAsyncIdempotencyHeader;
import com.arvatosystems.t9t.out.services.IAsyncSender;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ExceptionUtil;

/**
 * The PostSender implements a simple client invocation via http POST of the JDK 11 HttpClient.
 */
public abstract class AbstractPostSenderJdk11<T> implements IAsyncSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPostSenderJdk11.class);

    protected final IAsyncIdempotencyHeader idempotencyHeaderGenerator = Jdp.getRequired(IAsyncIdempotencyHeader.class);
    protected final AtomicLong previousSend = new AtomicLong(0);
    protected AsyncQueueDTO queue;
    protected String lastUrl = "";
    protected HttpClient defaultHttpClient = null;
    protected IAccessTokenClient<AccessTokenCacheKey> accessTokenClient = Jdp.getRequired(IAccessTokenClient.class);

    protected record AccessTokenCacheKey(String tenantId, String channelId) {
    }

    @Override
    public void init(final AsyncQueueDTO myQueue) {
        this.queue = myQueue;
        defaultHttpClient = HttpClient.newBuilder()
            .version(Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    }

    /**
     * Returns a publisher for the provided data and sets content-type / accept headers.
     */
    protected abstract void addDefaultPublisherForPayload(HttpRequest.Builder httpRequestBuilder, BonaPortable payload) throws Exception;

    protected void setHeaders(final HttpRequest.Builder httpRequestBuilder, final DataReference dr) {
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CONTENT_TYPE, dr.getContentType());
        if (dr.getAccept() != null) {
            httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT, dr.getAccept());
        }
        if (dr.getIsCompressed()) {
            httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CONTENT_ENCODING, T9tConstants.HTTP_ENCODING_GZIPPED);
        }
    }

    /**
     * Returns a publisher for the provided data.
     * This implementation is able to handle indirect data references, and falls back to the default publisher.
     */
    protected void addPublisherForPayload(final HttpRequest.Builder httpRequestBuilder, final BonaPortable payload) throws Exception {
        if (payload instanceof DataReference dr) {
            if (dr.getReferenceType() == CommunicationTargetChannelType.FILE) {
                LOGGER.debug("ASYNC: Reading contents of FILE {} as POST payload", dr.getFileOrQueueName());
                final Path path = Path.of(dr.getFileOrQueueName());
                httpRequestBuilder.POST(BodyPublishers.ofFile(path));
                setHeaders(httpRequestBuilder, dr);
                return;
            }
        }
        addDefaultPublisherForPayload(httpRequestBuilder, payload);
    }

    protected HttpRequest buildRequest(final AsyncChannelDTO channel, final Long messageRef, final BonaPortable payload, int timeout,
        final String tenantId) throws Exception {
        final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(new URI(channel.getUrl()))
            .version(Version.HTTP_2)
            .timeout(Duration.ofMillis(timeout));
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CHARSET,        T9tConstants.HTTP_CHARSET_UTF8);
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT_CHARSET, T9tConstants.HTTP_CHARSET_UTF8);
        addPublisherForPayload(httpRequestBuilder, payload);
        addAuthentication(httpRequestBuilder, channel, tenantId);
        addIdempotency(httpRequestBuilder, channel, messageRef, payload);

        return httpRequestBuilder.build();
    }

    /**
     * Adds an authentication header to the http request.
     *
     * @param httpRequestBuilder
     * @param channel
     */
    protected void addAuthentication(final HttpRequest.Builder httpRequestBuilder, final AsyncChannelDTO channel, final String tenatId) {
        final String authentication = channel.getAuthParam();
        if (T9tUtil.isNotBlank(channel.getAuthServerUrl())) {
            final AccessTokenCacheKey key = new AccessTokenCacheKey(tenatId, channel.getAsyncChannelId());
            final AccessTokenDTO token = accessTokenClient.getAccessToken(key, channel.getClientId(), channel.getAuthParam(), channel.getAuthServerUrl(),
                channel.getAuthType(), channel.getZ());
            httpRequestBuilder.header("Authorization", token.getTokenType() + " " + token.getAccessToken());
        } else if (authentication != null && authentication.length() > 0) {
            final String httpHeaderVariable = T9tUtil.nvl(channel.getAuthType(), "Authorization");
            httpRequestBuilder.header(httpHeaderVariable, authentication);
        }
    }

    /**
     * Adds an authentication header to the http request.
     *
     * @param httpRequestBuilder
     * @param channel
     */
    protected void addIdempotency(final HttpRequest.Builder httpRequestBuilder, final AsyncChannelDTO channel,
            final Long messageRef, final BonaPortable payload) {
        final String value = idempotencyHeaderGenerator.getIdempotencyHeaderVariable(channel, messageRef, payload);
        if (value != null) {
            httpRequestBuilder.header(idempotencyHeaderGenerator.getIdempotencyHeader(channel), value);
        }
    }

    /** Returns a body handler of the required type. */
    protected abstract BodyHandler<T> getBodyHandler();

    @Override
    public boolean send(final AsyncChannelDTO channel, final int timeout, final InMemoryMessage msg,
            final Consumer<AsyncHttpResponse> resultProcessor, final long whenStarted) throws Exception {
        return send(channel, timeout, msg, resultProcessor, whenStarted, true);
    }

    private boolean send(final AsyncChannelDTO channel, final int timeout, final InMemoryMessage msg,
        final Consumer<AsyncHttpResponse> resultProcessor, final long whenStarted, final boolean retry) throws Exception {
        // smart throttling, if required
        if (channel.getDelayAfterSend() != null) {
            final long actualDelay = whenStarted - previousSend.get(); // duration from previous to current invocation in ms (huge for initial call)
            final long desiredDelay = channel.getDelayAfterSend().longValue();
            previousSend.set(whenStarted); // update for next invocation
            if (desiredDelay > actualDelay) {
                if (desiredDelay - actualDelay >= 1000L) {
                    // we do a significant delay: inform (should occur on test environments only)
                    LOGGER.debug("Sleeping for {} ms on channel {} to achieve required delay", desiredDelay - actualDelay, channel.getAsyncChannelId());
                }
                // be nice to slow 3rd party receivers...
                Thread.sleep(desiredDelay - actualDelay);
            }
        }

        // do external I/O
        LOGGER.debug("Now sending ref {} of type {} to channel {}", msg.getObjectRef(), msg.getPayload().ret$PQON(), channel.getAsyncChannelId());
        final HttpRequest httpRq = buildRequest(channel, msg.getObjectRef(), msg.getPayload(), timeout, msg.getTenantId());
        final BodyHandler<T> serializedResponse = getBodyHandler();
        final CompletableFuture<HttpResponse<T>> futureResponse = defaultHttpClient.sendAsync(httpRq, serializedResponse);
        final Consumer<HttpResponse<T>> completeResultProcessor = httpResponse -> {
            final AsyncHttpResponse asyncResponse = new AsyncHttpResponse();
            asyncResponse.setHttpReturnCode(httpResponse.statusCode());
            parseResponse(asyncResponse, httpResponse);
            resultProcessor.accept(asyncResponse);
        };
        // depending on the configuration, wait for the response, or proceed
        if (Boolean.TRUE.equals(channel.getParallel())) {
            // true async processing
            futureResponse.whenComplete((r, ex) -> {
                if (ex != null) {
                    // deal with exception
                    final String cause = ExceptionUtil.causeChain(ex);
                    LOGGER.error("ASYNC: Could not complete async callout for channel {} ref {} due to {}",
                            channel.getAsyncChannelId(), msg.getObjectRef(), cause);
                    // construct a synthetic error response to allow storing of result
                    final AsyncHttpResponse asyncResponse = new AsyncHttpResponse();
                    if (ex instanceof CompletionException && ex.getCause() instanceof HttpTimeoutException) {
                        // remote did not respond
                        asyncResponse.setHttpReturnCode(T9tConstants.HTTP_STATUS_INTERNAL_TIMEOUT);  // 408 is request timeout
                    } else {
                        asyncResponse.setHttpReturnCode(T9tConstants.HTTP_STATUS_INTERNAL_EXCEPTION);
                        asyncResponse.setErrorDetails(cause);
                        asyncResponse.setHttpStatusMessage(cause);
                    }
                    resultProcessor.accept(asyncResponse);
                } else {
                    if (r.statusCode() == 401 && T9tUtil.isNotBlank(channel.getAuthServerUrl())  && retry) {
                        // 401 Unauthorized with oauth, access token might be invalid. Invalidate token and then retry with new token
                        LOGGER.error("Received 401 from async request for channelId {} and message ref {}, retrying the request", channel.getAsyncChannelId(),
                            msg.getObjectRef());
                        accessTokenClient.invalidateAccessToken(new AccessTokenCacheKey(msg.getTenantId(), channel.getAsyncChannelId()));
                        try {
                            send(channel, timeout, msg, resultProcessor, whenStarted, false);
                        } catch (Exception e) {
                            LOGGER.error("Error occurred when retrying async request for channelId {} and message ref {}. ", channel.getAsyncChannelId(),
                                msg.getObjectRef(), e);
                        }
                    } else {
                        completeResultProcessor.accept(r);
                    }
                }
            });
            return true;
        } else {
            // blocking I/O
            final HttpResponse<T> resp = futureResponse.get();  // this is not asynchronous!
            if (resp.statusCode() == 401 && T9tUtil.isNotBlank(channel.getAuthServerUrl())  && retry) {
                // 401 Unauthorized with oauth, access token might be invalid. Invalidate token and then retry with new token
                LOGGER.error("Received 401 from blocking request for channelId {} and message ref {}, retrying the request.", channel.getAsyncChannelId(),
                    msg.getObjectRef());
                accessTokenClient.invalidateAccessToken(new AccessTokenCacheKey(msg.getTenantId(), channel.getAsyncChannelId()));
                return send(channel, timeout, msg, resultProcessor, whenStarted, false);
            }
            completeResultProcessor.accept(resp);
            return httpStatusIsOk(resp.statusCode());
        }
    }

    protected void parseResponse(final AsyncHttpResponse myResponse, final HttpResponse<T> resp) {
        myResponse.setHttpStatusMessage(null);
        if (myResponse.getHttpReturnCode() <= 299) {
            // OK response
            myResponse.setClientReference(MessagingUtil.truncField(resp.body(), AsyncHttpResponse.meta$$clientReference.getLength()));
        } else {
            myResponse.setErrorDetails(MessagingUtil.truncField(resp.body(), AsyncHttpResponse.meta$$errorDetails.getLength()));
        }
    }

    @Override
    public void close() {
        // the HttpPostClient does not offer a close method, but we can actively remove all references to instances of it
        defaultHttpClient = null;
    }
}
