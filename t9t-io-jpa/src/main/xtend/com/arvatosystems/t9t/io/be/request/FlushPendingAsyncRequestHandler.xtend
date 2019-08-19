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
package com.arvatosystems.t9t.io.be.request

import com.arvatosystems.t9t.base.output.ExportStatusEnum
import com.arvatosystems.t9t.base.output.OutputSessionParameters
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.IOutputSession
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncChannelEntityResolver
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver
import com.arvatosystems.t9t.io.request.FlushPendingAsyncRequest
import com.arvatosystems.t9t.io.request.FlushPendingAsyncResponse
import com.arvatosystems.t9t.out.services.IAsyncQueue
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Provider
import com.arvatosystems.t9t.io.InMemoryMessage
import java.util.ArrayList
import java.util.List

@AddLogger
class FlushPendingAsyncRequestHandler extends AbstractRequestHandler<FlushPendingAsyncRequest> {
    @Inject IAsyncMessageEntityResolver messageResolver
    @Inject IAsyncChannelEntityResolver channelResolver
    @Inject IAsyncQueueEntityResolver queueResolver
    @Inject IAsyncQueue queueImpl;
    @Inject Provider<IOutputSession> outputSessionProvider

    override FlushPendingAsyncResponse execute(RequestContext ctx, FlushPendingAsyncRequest rq) {
        val queueRef = queueResolver.getRef(rq.onlyQueue, false)
        if (rq.markAsDone) {
            queueImpl.clearQueue(queueRef);  // clear any current entries from the in-memory
        }

        // create response
        val resp     = new FlushPendingAsyncResponse
        resp.records = new ArrayList<InMemoryMessage>();
        if (rq.exportToFile) {
            // file or file and response
            val os = outputSessionProvider.get
            resp.sinkRef = os.open(new OutputSessionParameters => [
                dataSinkId = rq.dataSinkId ?: "asyncSink"
            ])
            resp.numberOfRecords = process(ctx, os, rq, queueRef, resp.records)
            resp.filename = os.fileOrQueueName
            os.close
        } else {
            // only response
            resp.numberOfRecords = process(ctx, null, rq, queueRef, resp.records)
        }
        LOGGER.debug("Retrieved {} messages for channel {}", resp.numberOfRecords, rq.onlyChannelId ?: "(all)");
        return resp
    }

    def int process(RequestContext ctx, IOutputSession os, FlushPendingAsyncRequest rq, Long queueRef, List<InMemoryMessage> records) {
        // process a queue reference, if any
        val channels =
            if (queueRef !== null) {
                channelResolver.entityManager.createQuery(
                    "SELECT c.channelId FROM AsyncChannelEntity c WHERE c.asyncQueueRef = :queueRef",
                    String
                ).resultList
            }
        if (channels !== null && channels.size == 0) {
            // there was a condition, but it turned out to be the empty set
            LOGGER.warn("Channel restriction by queue specified, but no channels found!")
            return 0;
        }
        val extraCondition = if (rq.onlyChannelId !== null) " AND m.asyncChannelId = :channel";
        val extraCondition2 = if (channels !== null) " AND m.asyncChannelId IN :channels";
        val query = messageResolver.entityManager.createQuery('''
            SELECT m FROM AsyncMessageEntity m WHERE m.status != null AND m.tenantRef = :tenantRef«extraCondition»«extraCondition2»
             ORDER BY m.objectRef''', AsyncMessageEntity)
        query.setParameter("tenantRef", messageResolver.sharedTenantRef)
        if (rq.onlyChannelId !== null)
            query.setParameter("channel", rq.onlyChannelId);
        if (channels !== null)
            query.setParameter("channels", channels);
        val results = query.resultList
        for (m: results) {
            os?.store(m.payload)
            if (rq.markAsDone)
                m.status = ExportStatusEnum.RESPONSE_OK
            if (rq.returnInResponse) {
                records.add(new InMemoryMessage => [
                    tenantRef      = m.tenantRef
                    objectRef      = m.objectRef
                    asyncChannelId = m.asyncChannelId
                    payload        = m.payload
                ])
            }
        }
        return results.size
    }
}
