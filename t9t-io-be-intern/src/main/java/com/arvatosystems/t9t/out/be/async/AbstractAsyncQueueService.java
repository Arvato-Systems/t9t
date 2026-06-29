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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.cfg.be.AsyncTransmitterConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.InMemoryMessage;
import com.arvatosystems.t9t.io.request.QueueStatus;
import com.arvatosystems.t9t.out.services.IAsyncMessageUpdater;
import com.arvatosystems.t9t.out.services.IAsyncQueue;

public abstract class AbstractAsyncQueueService implements IAsyncQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAsyncQueueService.class);

    protected final IAsyncMessageUpdater messageUpdater = Jdp.getRequired(IAsyncMessageUpdater.class);
    protected final ConcurrentMap<Long, AbstractAsyncQueueData> queueDataMap;
    protected final AsyncTransmitterConfiguration asyncTransmitterConfig = ConfigProvider.getConfiguration().getAsyncMsgConfiguration();

    public AbstractAsyncQueueService() {
        LOGGER.info("Async queue loaded");

        // read the configured queues and launch a process for each of them...
        final List<AsyncQueueDTO> queueDTOs = messageUpdater.getActiveQueues();
        queueDataMap = new ConcurrentHashMap<>(3 * queueDTOs.size());
        if (!queueDTOs.isEmpty()) {
            for (final AsyncQueueDTO q: queueDTOs) {
                try {
                    queueDataMap.put(q.getObjectRef(), createQueueData(q));
                } catch (final Exception e) {
                    LOGGER.error("Error occurred while launching queue {}. ", q.getAsyncQueueId(), e);
                }
            }
        }
    }

    @Override
    public void close() {
        for (final AbstractAsyncQueueData queueData: queueDataMap.values()) {
            queueData.shutdown();
        }
        queueDataMap.clear();
    }

    @Override
    public void close(final Long queueRef) {
        final AbstractAsyncQueueData queueData = queueDataMap.get(queueRef);
        if (queueData != null) {
            queueData.shutdown();
            queueDataMap.remove(queueRef);
        } else {
            LOGGER.error("Cannot find queue data {} to close", queueRef);
        }
    }

    @Override
    public void clearQueue(@Nullable final Long queueRef) {
        if (queueRef == null) {
            for (final AbstractAsyncQueueData queueData: queueDataMap.values()) {
                queueData.clearQueue();
            }
        } else {
            final AbstractAsyncQueueData queueData = queueDataMap.get(queueRef);
            if (queueData != null) {
                queueData.clearQueue();
            } else {
                LOGGER.error("Cannot find queue data {} to clear", queueRef);
            }
        }
    }

    @Override
    public void open(@Nonnull final AsyncQueueDTO q) {
        queueDataMap.computeIfAbsent(q.getObjectRef(), (x) -> createQueueData(q));
    }

    @Nonnull
    @Override
    public QueueStatus getQueueStatus(@Nonnull final Long queueRef, @Nonnull final String queueId) {
        final AbstractAsyncQueueData queueData = queueDataMap.get(queueRef);
        final QueueStatus status = new QueueStatus();
        status.setAsyncQueueId(queueId);
        if (queueData == null) {
            status.setRunning(false);
        } else {
            status.setRunning(true);
            status.setIsGreen(isQueueGreen(queueData));
            status.setLastMessageSent(queueData.getLastMessageSent());
            status.setShuttingDown(queueData.isShutdownInProgress());
        }
        return status;
    }

    @Nonnull
    protected InMemoryMessage getMessage(@Nonnull final String tenantId, @Nonnull final String asyncChannelId, @Nullable final Long objectRef, @Nonnull final BonaPortable payload) {
        final InMemoryMessage m = new InMemoryMessage();
        m.setTenantId(tenantId);
        m.setAsyncChannelId(asyncChannelId);
        m.setObjectRef(objectRef);
        m.setPayload(payload);
        return m;
    }

    @Nonnull
    protected abstract AbstractAsyncQueueData createQueueData(@Nonnull AsyncQueueDTO queueConfig);

    @Nullable
    protected Boolean isQueueGreen(@Nonnull final AbstractAsyncQueueData queueData) {
        return null;
    }
}
