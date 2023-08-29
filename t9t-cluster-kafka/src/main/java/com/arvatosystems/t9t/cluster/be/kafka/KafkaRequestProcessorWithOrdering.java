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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;
import com.arvatosystems.t9t.kafka.service.IKafkaConsumer;
import com.arvatosystems.t9t.kafka.service.IKafkaTopicReader;
import com.arvatosystems.t9t.kafka.service.impl.KafkaTopicReader;

/**
 * @deprecated replaced by {@link KafkaSimplePartitionOrderedRequestProcessor}.
 */
final class KafkaRequestProcessorWithOrdering implements Callable<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRequestProcessorWithOrdering.class);
    private static final int NUMBER_OF_PARTITIONS       = 12;       // initial size of maps
    private static final int SLOW_PROCESSING_TIMEOUT    = 15_000;   // after how many ms to give up processing
    private static final int WAIT_INTERVAL              = 50;       // period to sleep after a poll
    private static final int WAIT_INTERVAL_IDLE         = 200;      // period to sleep after a poll without any new requests
    private static final int DEFAULT_MAX_PENDING        =  3;       // how many maximum pending processes can exist to allow polling more
    private static final int DEFAULT_HARD_LIMIT_PENDING = 24;       // how many maximum pending processes can exist to stop polling at all

    private final IKafkaTopicReader consumer;
    private final AtomicBoolean shuttingDown;
    private final AuthenticationParameters defaultAuthHeader;
    private final Map<Integer, KafkaSinglePartitionRequestProcessor> workerForPartition = new HashMap<>(NUMBER_OF_PARTITIONS);
    private final int maxPending;
    private final int hardLimitPending;
    private final long slowTimeoutInMs;
    private final long waitIntervalInMs;
    private final boolean commitSync;

    protected KafkaRequestProcessorWithOrdering(final KafkaConfiguration config, final IKafkaTopicReader consumer, final AtomicBoolean shuttingDown) {
        this.consumer = consumer;
        this.shuttingDown = shuttingDown;
        if (config.getClusterManagerApiKey() != null)  {
            this.defaultAuthHeader = new ApiKeyAuthentication(config.getClusterManagerApiKey());
            this.defaultAuthHeader.freeze();
        } else {
            this.defaultAuthHeader = null;
        }
        maxPending       = T9tUtil.nvl(config.getMaxPending(),            DEFAULT_MAX_PENDING);
        hardLimitPending = T9tUtil.nvl(config.getHardLimitPending(),      DEFAULT_HARD_LIMIT_PENDING);
        slowTimeoutInMs  = T9tUtil.nvl(config.getSlowProcessingTimeout(), SLOW_PROCESSING_TIMEOUT).longValue();
        waitIntervalInMs = T9tUtil.nvl(config.getWaitInterval(),          WAIT_INTERVAL).longValue();
        commitSync       = Boolean.TRUE.equals(config.getCommitSync());
    }

    @Override
    public Boolean call() {
        final AtomicInteger numberOfPolls = new AtomicInteger(0);
        boolean allowPolling = true;
        boolean busy = false; // indicates there is uncommitted work to do
        long lastinfo = System.nanoTime();
        final IKafkaConsumer<ServiceRequest> processor = (srq, partition, offset, key) -> {
            final Integer partitionObj = partition;
            workerForPartition.computeIfAbsent(partitionObj,
                dummy -> {
                    final KafkaSinglePartitionRequestProcessor instance
                      = new KafkaSinglePartitionRequestProcessor(partitionObj, shuttingDown, defaultAuthHeader, consumer.getKafkaTopic());
                    final Thread t = new Thread(instance, "t9t-KafkaWorker-" + partition);
                    t.start();
                    return instance;
                }).submit(srq, offset);
        };
        while (!shuttingDown.get()) {
            final int currentNum = numberOfPolls.incrementAndGet();
            final long startChunk = System.nanoTime();
            if (startChunk - lastinfo >= 60_000_000_000L) {
                // 60 seconds have elapsed... Update info
                LOGGER.debug("Polling for the {}th time", currentNum);
                lastinfo = startChunk;
            }
            if (allowPolling) {
                final int recordsProcessed = consumer.pollAndProcess(processor, ServiceRequest.class, KafkaTopicReader.DEFAULT_POLL_INTERVAL, null);
                if (recordsProcessed < 0) {
                    // some exception (it is OK during shutdown)
                    if (shuttingDown.get()) {
                        break;
                    }
                    LOGGER.error("Error polling kafka - sleeping a while, then retry");
                    T9tUtil.sleepAndWarnIfInterrupted(1_000L, LOGGER, null);
                } else if (recordsProcessed > 0) {
                    LOGGER.debug("Polling kafka returned {} new tasks", recordsProcessed);
                    busy = true;
                } else {
                    T9tUtil.sleepAndWarnIfInterrupted(WAIT_INTERVAL_IDLE, LOGGER, "Sleep after idle interrupted");
                    continue;
                }
            }
            if (busy) {
                // check how many we have in our pool and wait until sufficiently processed work
                final long processingStart = System.currentTimeMillis();
                while (!shuttingDown.get()) {
                    int numPending = 0;
                    for (final KafkaSinglePartitionRequestProcessor instance: workerForPartition.values()) {
                        numPending += instance.numPending();
                    }
                    if (numPending <= maxPending) {
                        // sufficiently low enough pending requests
                        allowPolling = true;
                        if (busy) {
                            LOGGER.debug("Sufficient work completed ({} left), next poll after {} ms",
                              numPending, System.currentTimeMillis() - processingStart);
                        }
                        busy = numPending == 0;  // single LoC to unset busy
                        break;
                    }
                    if (System.currentTimeMillis() > processingStart + slowTimeoutInMs) {
                        LOGGER.warn("SLOW: Still {} pending processes after {} ms wait", numPending, slowTimeoutInMs);
                        allowPolling = numPending < hardLimitPending;
                        break;
                    }
                    T9tUtil.sleepAndWarnIfInterrupted(waitIntervalInMs, LOGGER, null);
                }
                collectOffsetsToCommit(commitSync);
            }
        }
        LOGGER.info("End requested, waiting for all pending commands...");
        try {
            final long loosePatienceAfter = System.currentTimeMillis() + 10_000L;
            for (;;) {
                // check every instance if done
                int numberOfThreadsStillBusy = 0;
                for (final KafkaSinglePartitionRequestProcessor instance: workerForPartition.values()) {
                    if (instance.shutdownStillPending()) {
                        ++numberOfThreadsStillBusy;
                    }
                }
                if (numberOfThreadsStillBusy == 0) {
                    LOGGER.info("All child threads have terminated, regular shut down");
                    break;
                }
                Thread.sleep(waitIntervalInMs);
                if (System.currentTimeMillis() > loosePatienceAfter) {
                    LOGGER.warn("Not all child threads have terminated, but maximum wait time reached, {} tasks still busy at last check",
                        numberOfThreadsStillBusy);
                    return Boolean.TRUE;
                }
            }
            // final collect of offsets - commit sync
            collectOffsetsToCommit(true);  // always sync, because we close right after
            consumer.close();
            return Boolean.TRUE;
        } catch (final InterruptedException e) {
            LOGGER.error("There is a problem, tasks not finished within shutdown timeout: {}", e.getClass().getSimpleName());
        }
        return Boolean.FALSE;
    }

    // collect offsets to commit
    private void collectOffsetsToCommit(final boolean sync) {
        final Map<TopicPartition, OffsetAndMetadata> partialCommits = new HashMap<>(NUMBER_OF_PARTITIONS);
        for (final KafkaSinglePartitionRequestProcessor instance: workerForPartition.values()) {
            instance.addOffsetToCommit(partialCommits);
        }
        if (!partialCommits.isEmpty()) {
            LOGGER.debug("Sending partial commits for {} partitions", partialCommits.size());
            consumer.performPartialCommits(partialCommits, sync);
        }
    }
}
