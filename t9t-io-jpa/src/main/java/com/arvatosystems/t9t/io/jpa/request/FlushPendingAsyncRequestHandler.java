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
package com.arvatosystems.t9t.io.jpa.request;

import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.InMemoryMessage;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncChannelEntityResolver;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver;
import com.arvatosystems.t9t.io.request.FlushPendingAsyncRequest;
import com.arvatosystems.t9t.io.request.FlushPendingAsyncResponse;
import com.arvatosystems.t9t.out.services.IAsyncQueue;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlushPendingAsyncRequestHandler extends AbstractRequestHandler<FlushPendingAsyncRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlushPendingAsyncRequestHandler.class);

    private final IAsyncMessageEntityResolver messageResolver = Jdp.getRequired(IAsyncMessageEntityResolver.class);
    private final IAsyncChannelEntityResolver channelResolver = Jdp.getRequired(IAsyncChannelEntityResolver.class);
    private final IAsyncQueueEntityResolver queueResolver = Jdp.getRequired(IAsyncQueueEntityResolver.class);
    private final IAsyncQueue queueImpl = Jdp.getRequired(IAsyncQueue.class);
    private final Provider<IOutputSession> outputSessionProvider = Jdp.getProvider(IOutputSession.class);

    @Override
    public FlushPendingAsyncResponse execute(final RequestContext ctx, final FlushPendingAsyncRequest rq) throws Exception {
        final Long queueRef = queueResolver.getRef(rq.getOnlyQueue(), false);
        if (rq.getMarkAsDone()) {
            queueImpl.clearQueue(queueRef); // clear any current entries from the in-memory
        }

        // create response
        final FlushPendingAsyncResponse resp = new FlushPendingAsyncResponse();
        resp.setRecords(new ArrayList<InMemoryMessage>());
        if (rq.getExportToFile()) {
            // file or file and response
            final IOutputSession os = outputSessionProvider.get();
            final OutputSessionParameters parameters = new OutputSessionParameters();
            parameters.setDataSinkId(rq.getDataSinkId() == null ? "asyncSink" : rq.getDataSinkId());
            resp.setSinkRef(os.open(parameters));
            resp.setNumberOfRecords(process(ctx, os, rq, queueRef, resp.getRecords()));
            resp.setFilename(os.getFileOrQueueName());
            os.close();
        } else {
            // only response
            resp.setNumberOfRecords(process(ctx, null, rq, queueRef, resp.getRecords()));
        }
        LOGGER.debug("Retrieved {} messages for channel {}", resp.getNumberOfRecords(), rq.getOnlyChannelId() == null ? "(all)" : rq.getOnlyChannelId());
        return resp;
    }

    public int process(final RequestContext ctx, final IOutputSession os, final FlushPendingAsyncRequest rq, final Long queueRef,
            final List<InMemoryMessage> records) {
        // process a queue reference, if any
        final List<String> channels;
        if (queueRef != null) {
            channels = channelResolver.getEntityManager()
                    .createQuery("SELECT c.channelId FROM AsyncChannelEntity c WHERE c.asyncQueueRef = :queueRef", String.class).getResultList();
        } else {
            channels = null;
        }
        if (channels != null && channels.size() == 0) {
            // there was a condition, but it turned out to be the empty set
            LOGGER.warn("Channel restriction by queue specified, but no channels found!");
            return 0;
        }
        final String extraCondition = rq.getOnlyChannelId() != null ? " AND m.asyncChannelId = :channel" : "";
        final String extraCondition2 = channels != null ? " AND m.asyncChannelId IN :channels" : "";
        final String queryString = "SELECT m FROM AsyncMessageEntity m WHERE m.status != null AND m.tenantRef = :tenantRef" + extraCondition + extraCondition2
                + " ORDER BY m.objectRef";
        final TypedQuery<AsyncMessageEntity> query = messageResolver.getEntityManager().createQuery(queryString, AsyncMessageEntity.class);
        query.setParameter("tenantRef", messageResolver.getSharedTenantRef());
        if (rq.getOnlyChannelId() != null) {
            query.setParameter("channel", rq.getOnlyChannelId());
        }
        if (channels != null) {
            query.setParameter("channels", channels);
        }
        final List<AsyncMessageEntity> results = query.getResultList();
        for (final AsyncMessageEntity m : results) {
            if (os != null) {
                os.store(m.getPayload());
            }
            if (rq.getMarkAsDone()) {
                m.setStatus(ExportStatusEnum.RESPONSE_OK);
            }
            if (rq.getReturnInResponse()) {
                final InMemoryMessage inMemoryMessage = new InMemoryMessage();
                inMemoryMessage.setTenantRef(m.getTenantRef());
                inMemoryMessage.setObjectRef(m.getObjectRef());
                inMemoryMessage.setAsyncChannelId(m.getAsyncChannelId());
                inMemoryMessage.setPayload(m.getPayload());
                records.add(inMemoryMessage);
            }
        }
        return results.size();
    }
}
