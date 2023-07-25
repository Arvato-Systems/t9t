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
package com.arvatosystems.t9t.cluster.be.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.TransactionOriginType;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ExceptionUtil;

final class KafkaSinglePartitionRequestProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSinglePartitionRequestProcessor.class);
    private static final int MAX_ELEMENTS_PER_BATCH = 16;  // arbitrary - it will be lower anyway

    private final Integer partition;
    private final AtomicBoolean shuttingDown;
    private final AuthenticationParameters defaultAuthHeader;
    private final LinkedTransferQueue<RequestWithOffset> queue = new LinkedTransferQueue<>();
    private final IUnauthenticatedServiceRequestExecutor requestProcessor = Jdp.getRequired(IUnauthenticatedServiceRequestExecutor.class);
    private final AtomicReference<Long> lastComittedOffset = new AtomicReference<>();  // cannot use AtomicLong because we have to support nulls
    private final TopicPartition tp;
    private final AtomicInteger uniqueRequestsCounter = new AtomicInteger(0);
    private final AtomicInteger inProgress = new AtomicInteger(0);

    private record RequestWithOffset(ServiceRequest srq, long offset) {
    }

    KafkaSinglePartitionRequestProcessor(final Integer partition, final AtomicBoolean shuttingDown, final AuthenticationParameters defaultAuthHeader,
      final String topic) {
        this.partition = partition;
        this.shuttingDown = shuttingDown;
        this.defaultAuthHeader = defaultAuthHeader;
        this.tp = new TopicPartition(topic, partition);
        LOGGER.info("Kafka worker thread started for partition {}", partition);
    }

    /** Adds a non-null value if there is a new offset to commit, otherwise null (most recent offset has been fetched already). */
    void addOffsetToCommit(final Map<TopicPartition, OffsetAndMetadata> offsets) {
        final Long newOffset = lastComittedOffset.getAndSet(null);
        if (newOffset != null) {
            offsets.put(tp, new OffsetAndMetadata(newOffset));
        }
    }

    boolean shutdownStillPending() {
        return true;
    }

    int numPending() {
        return queue.size() + inProgress.get();
    }

    void submit(final ServiceRequest srq, final long offset) {
        queue.add(new RequestWithOffset(srq, offset));
    }

    private void processRequest(final ServiceRequest srq, final long offset) {
        srq.setPartitionUsed(partition);
        final RequestParameters rp = srq.getRequestParameters();
        final ServiceRequestHeader optHdr = srq.getRequestHeader();
        final UUID messageId = rp.getMessageId() == null ? optHdr != null ? optHdr.getMessageId() : null : rp.getMessageId();
        if (srq.getAuthentication() == null) {
            // no specific user provided: use default (consider security!) or fail, if none configured (default)
            if (defaultAuthHeader != null) {
                srq.setAuthentication(defaultAuthHeader);
            } else {
                LOGGER.error("Received request {} (ID {}) for {} without authentication header", rp.ret$PQON(), messageId, rp.getEssentialKey());
                return;
            }
        }
        if (rp.getTransactionOriginType() == null) {
            rp.setTransactionOriginType(TransactionOriginType.KAFKA); // some other kafka based source
        }
        final int uniqueId = uniqueRequestsCounter.incrementAndGet();

        LOGGER.debug("Submitting task {} (ID {}), PQON {}, partition {}, offset {}",
          uniqueId, messageId, rp.ret$PQON(), partition, offset);
        try {
            requestProcessor.execute(srq);
        } catch (Exception e) {
            LOGGER.error("Task {} (ID {}), PQON {} failed due to {}:{}", uniqueId, messageId, rp.ret$PQON(), e.getClass().getSimpleName(), e.getMessage());
        }
        LOGGER.debug("Completed execute of task {} (ID {}), PQON {}", uniqueId, messageId, rp.ret$PQON());
    }

    @Override
    public void run() {
        final List<RequestWithOffset> workPool = new ArrayList<>(MAX_ELEMENTS_PER_BATCH);
        while (!shuttingDown.get()) {
            int num = 0;
            try {
                num = queue.drainTo(workPool, MAX_ELEMENTS_PER_BATCH);
                if (num == 0) {
                    T9tUtil.sleepAndWarnIfInterrupted(50L, LOGGER, "Sleep disturbed");
                } else {
                    inProgress.set(num);
                }
            } catch (final Exception e) {
                LOGGER.error("Problem pulling data from internal transfer queue: {}", ExceptionUtil.causeChain(e));
            }
            // execute all requests and poll again without waiting
            for (int i = 0; i < num && !shuttingDown.get(); ++i) {
                try {
                    final RequestWithOffset rqWo = workPool.get(i);
                    // perform the request
                    processRequest(rqWo.srq, rqWo.offset);
                    // remember the commit offset
                    lastComittedOffset.set(rqWo.offset);
                    inProgress.decrementAndGet();
                } catch (final Exception e) {
                    LOGGER.error("Cannot get or process request from transfer queue: {}", ExceptionUtil.causeChain(e));
                }
            }
        }
    }
}
