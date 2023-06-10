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
package com.arvatosystems.t9t.out.jpa.impl;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver;
import com.arvatosystems.t9t.out.services.IAsyncQueue;
import com.arvatosystems.t9t.out.services.IAsyncTools;
import com.arvatosystems.t9t.out.services.IAsyncTransmitter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import jakarta.persistence.Query;

/** Default implementation of the persisted asynchronous message bus. */

@Singleton
public class DefaultAsyncTransmitter implements IAsyncTransmitter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAsyncTransmitter.class);
    protected final IAsyncMessageEntityResolver asyncMessageResolver = Jdp.getRequired(IAsyncMessageEntityResolver.class);
    protected final IAsyncQueue asyncQueueSender = Jdp.getRequired(IAsyncQueue.class);
    private final IAsyncTools asyncTools = Jdp.getRequired(IAsyncTools.class);
    protected final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);

    public DefaultAsyncTransmitter() {
        LOGGER.info("Created an asynchronous message sender");
    }

    private void persistInDb(final Long objectRef, final Long queueRef, final String asyncChannelId, final BonaPortable payload, final Long ref,
      final String category, final String identifier) {
        final AsyncMessageEntity m = asyncMessageResolver.newEntityInstance();
        m.setObjectRef(objectRef);
        m.setAsyncChannelId(asyncChannelId);
        m.setStatus(ExportStatusEnum.READY_TO_EXPORT);
        m.setWhenSent(Instant.now());
        m.setAttempts(0);
        m.setPayload(payload);
        m.setRef(ref);
        m.setRefIdentifier(identifier);
        m.setRefType(category);
        m.setAsyncQueueRef(queueRef);
        asyncMessageResolver.save(m);
    }

    @Override
    public void retransmitMessage(final RequestContext ctx, final String asyncChannelId, final BonaPortable payload, final Long objectRef,
      final int partition, final String recordKey) {
        // redundant check to see if the channel exists (to get exception in sync thread already). Should not cost too much time due to caching
        final AsyncChannelDTO cfg = asyncTools.getCachedAsyncChannelDTO(ctx.tenantId, asyncChannelId);
        if (!cfg.getIsActive() || cfg.getAsyncQueueRef() == null) {
            LOGGER.debug("Discarding async message to inactive or unassociated channel {}", asyncChannelId);
        }
        asyncQueueSender.sendAsync(ctx, cfg, payload, objectRef, partition, recordKey);
    }

    @Override
    public Long transmitMessage(final String asyncChannelId, final BonaPortable payload, final Long ref, final String category, final String identifier,
      final int partition) {
        final RequestContext ctx = ctxProvider.get();
        // check if the message is valid (due to the asynchronous nature, invalid messages would cause hard to detect problems)
        payload.validate();
        final AsyncChannelDTO cfg = asyncTools.getCachedAsyncChannelDTO(ctx.tenantId, asyncChannelId);
        if (!cfg.getIsActive() || cfg.getAsyncQueueRef() == null) {
            LOGGER.debug("Discarding async message to inactive or unassociated channel {}", asyncChannelId);
            return null;
        }
        final Long objectRef = asyncMessageResolver.createNewPrimaryKey();
        asyncQueueSender.sendAsync(ctx, cfg, payload, objectRef, partition, null);

        if (asyncQueueSender.persistInDb()) {
            // persisting the message should be done into the relational database
            persistInDb(objectRef, cfg.getAsyncQueueRef().getObjectRef(), asyncChannelId, payload, ref, category, identifier);
        }
        return objectRef;
    }

    @Override
    public void stopRetries(final Long messageRef) {
        final Query q = asyncMessageResolver.getEntityManager().createQuery(
            "UPDATE " + AsyncMessageEntity.class.getSimpleName() + " a "
            + "SET a.status = null, a.whenSent = null WHERE a.objectRef = :messageRef AND a.status IS NOT NULL");
        q.setParameter("messageRef", messageRef);
        q.executeUpdate();
    }
}
