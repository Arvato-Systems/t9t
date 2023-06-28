/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncHttpResponse;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.InMemoryMessage;
import com.arvatosystems.t9t.out.services.IAsyncSender;
import com.arvatosystems.t9t.out.services.IMarshallerExt;

import de.jpaw.bonaparte.core.HttpPostResponseObject;
import de.jpaw.bonaparte.sock.HttpPostClient;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;

/**
 * The PostSender implements a simple client invocation via http POST.
 */
@Dependent
@Named("POST")
public class PostSender<R> implements IAsyncSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostSender.class);
    protected AsyncQueueDTO queue;
    protected String lastUrl = "";
    protected IMarshallerExt<R> defaultMarshaller = null;
    protected HttpPostClient defaultHttpClient = null;
    private final ConcurrentMap<Long, HttpPostClient> httpClientPerChannel = new ConcurrentHashMap<>();

    @Override
    public void init(final AsyncQueueDTO myQueue) {
        LOGGER.info("Creating IAsyncSender POST for queue {}", myQueue.getAsyncQueueId());
        this.queue = myQueue;
        // create the default marshaller
        final String marshallerQualifier = myQueue.getDefaultSerializerQualifier();
        if (marshallerQualifier != null) {
            defaultMarshaller = Jdp.getRequired(IMarshallerExt.class, marshallerQualifier);
            defaultHttpClient = new HttpPostClient("http://localhost/", false, false, false, false, defaultMarshaller);
        }
    }

    /** Returns a POST client with the correct URL. */
    protected HttpPostClient getPostClient(final AsyncChannelDTO channelDto) {
        final String qualifier = channelDto.getSerializerQualifier();
        if (qualifier == null) {
            if (!channelDto.getUrl().equals(lastUrl)) {
                // adjust the URL on the default client and return that one
                lastUrl = channelDto.getUrl();
                defaultHttpClient.setBaseUrl(lastUrl);
            }
            return defaultHttpClient;
        }
        return httpClientPerChannel.computeIfAbsent(channelDto.getObjectRef(), (dummy) ->
            // no existing client, construct one!
            new HttpPostClient(channelDto.getUrl(), false, false, false, false, Jdp.getRequired(IMarshallerExt.class, qualifier))
        );
    }

    @Override
    public boolean send(final AsyncChannelDTO channelDto, final int timeout, final InMemoryMessage msg,
      final Consumer<AsyncHttpResponse> resultProcessor, final long whenStarted) throws Exception {
        // do external I/O
        final HttpPostClient httpClient = getPostClient(channelDto);
        httpClient.setTimeoutInMs(timeout);  // set the request specific timeout or fall back to the default
        if (channelDto.getAuthParam() == null || channelDto.getAuthParam().trim().length() == 0) {
            httpClient.setAuthentication(null);
        } else {
            httpClient.setAuthentication(channelDto.getAuthParam());
        }

        final HttpPostResponseObject resp =  httpClient.doIO2(msg.getPayload());
        final AsyncHttpResponse myResponse = new AsyncHttpResponse();
        myResponse.setHttpReturnCode(resp.getHttpReturnCode());
        myResponse.setHttpStatusMessage(resp.getHttpStatusMessage());
        myResponse.setResponseObject(resp.getResponseObject());
        if (resp.getResponseObject() != null && defaultMarshaller != null) {
            final R r = (R)resp.getResponseObject();
            myResponse.setClientReturnCode(defaultMarshaller.getClientReturnCode(r));
            myResponse.setClientReference(defaultMarshaller.getClientReference(r));
        }
        resultProcessor.accept(myResponse);
        return httpStatusIsOk(resp.getHttpReturnCode());
    }

    @Override
    public void close() {
        // the HttpPostClient does not offer a close method, but we can actively remove all references to instances of it
        httpClientPerChannel.clear();
    }
}
