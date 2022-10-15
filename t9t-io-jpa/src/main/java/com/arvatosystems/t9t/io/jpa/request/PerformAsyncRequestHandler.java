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

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncChannelKey;
import com.arvatosystems.t9t.io.jpa.entities.AsyncChannelEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncChannelEntityResolver;
import com.arvatosystems.t9t.io.request.PerformAsyncRequest;
import com.arvatosystems.t9t.out.services.IAsyncTransmitter;

import de.jpaw.dp.Jdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformAsyncRequestHandler extends AbstractRequestHandler<PerformAsyncRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformAsyncRequestHandler.class);

    private final IAsyncChannelEntityResolver channelResolver = Jdp.getRequired(IAsyncChannelEntityResolver.class);
    private final IAsyncTransmitter asyncTransmitter = Jdp.getRequired(IAsyncTransmitter.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final PerformAsyncRequest rq) {
        // just a check to see if the channel has been configured
        final AsyncChannelEntity channel = channelResolver.getEntityData(new AsyncChannelKey(rq.getAsyncChannelId()), true);
        String logQueueId = "NULL";
        if (channel.getAsyncQueue() != null && channel.getAsyncQueue().getAsyncQueueId() != null) {
            logQueueId = channel.getAsyncQueue().getAsyncQueueId();
        }
        LOGGER.debug("channel {} is associated with queue {}", channel.getAsyncChannelId(), logQueueId);
        final Long ref = rq.getRef() == null ? ctx.getRequestRef() : rq.getRef();
        final String category = rq.getRefType() == null ? "REQ" : rq.getRefType();
        final String identifier = rq.getRefIdentifier() == null ? ctx.userId : rq.getRefIdentifier();
        asyncTransmitter.transmitMessage(rq.getAsyncChannelId(), rq.getPayload(), ref, category, identifier,
            rq.getPartition() == 0 ? 0 : rq.getPartition());
        return ok();
    }
}
