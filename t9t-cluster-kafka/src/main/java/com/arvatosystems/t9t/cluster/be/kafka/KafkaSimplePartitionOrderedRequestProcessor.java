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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;
import com.arvatosystems.t9t.kafka.service.IKafkaTopicReader;
import com.arvatosystems.t9t.kafka.service.impl.KafkaTopicReader;

/**
 * Kafka prossing implementation with following key features:
 * <ul>
 *   <li>Multithreaded with the help of a fixed thread pool (based on CPUs or {@code clusterManagerPoolSize} config.</li>
 *   <li>Grouping of records by partitions. Starting a processing thread for each batch of records (with same partition)</li>
 *   <li>Keeping order of records within one partition, i.e. sequential processing of each partition.</li>
 *   <li>Procossor pauses consumer for 'busy' partitions and resumse once the last batch has been finished.</li>
 *   <li>Processed offsets will be collected and committed after a configured time interval.</li>
 *   <li>Handling of revoked partitions via {@link KafkaClusterRebalancer}</li>
 * </ul>
 * Processor will be shut down by {@link KafkaRequestProcessorAndClusterManagerInitializer#onShutdown()}
 */
final class KafkaSimplePartitionOrderedRequestProcessor implements Callable<Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSimplePartitionOrderedRequestProcessor.class);

    private static final int WAIT_INTERVAL_IDLE = 200; // period to sleep after a poll without any new requests
    private static final int DEFAULT_COMMIT_INTERVAL = 3000; // commit interval
    private static final int DEFAULT_MONITOR_INTERVAL = 5000; // monitor interval
    private static final int DEFAULT_TIMEOUT_THREADPOOL_SHUTDOWN_MS = 10_000;
    private static final int DEFAULT_WORKER_POOL_SIZE = 6;

    private final IKafkaTopicReader consumer;
    private final AtomicBoolean shuttingDown;
    private final AuthenticationParameters defaultAuthHeader;
    private final boolean commitSync;

    private final AtomicInteger workerThreadCounter = new AtomicInteger();
    private final ExecutorService executorKafkaWorker;
    private final Map<TopicPartition, KafkaSimpleMultipleRecordsProcessor> activeProcessors;
    private final Map<TopicPartition, OffsetAndMetadata> offsetsToCommit;
    private long lastCommitTime;
    private long lastMonitorTime;
    private final Map<TopicPartition, PartitionMonitor> partitionStatusTable;
    private final long commitIntervalInMs;
    private final long monitorIntervalInMs;
    private final long shutdownThreadpoolIntervalInMs;

    protected KafkaSimplePartitionOrderedRequestProcessor(final KafkaConfiguration config, final IKafkaTopicReader consumer, final AtomicBoolean shuttingDown) {
        LOGGER.info("Starting " + this.getClass().getSimpleName());

        // init constructor params
        if (config.getClusterManagerApiKey() != null) {
            this.defaultAuthHeader = new ApiKeyAuthentication(config.getClusterManagerApiKey());
            this.defaultAuthHeader.freeze();
        } else {
            this.defaultAuthHeader = null;
        }
        this.consumer = consumer;
        this.shuttingDown = shuttingDown;

        // init others
        this.commitSync = T9tUtil.isTrue(config.getCommitSync());

        final int numberOfPartitions = consumer.getNumberOfPartitions();
        this.activeProcessors = new HashMap<>(numberOfPartitions);
        this.offsetsToCommit = new HashMap<>(numberOfPartitions);
        this.partitionStatusTable = new HashMap<>(numberOfPartitions);
        this.lastCommitTime = System.currentTimeMillis();
        this.lastMonitorTime = System.currentTimeMillis();

        // parameters from config
        this.commitIntervalInMs = T9tUtil.nvl(config.getCommitInterval(), DEFAULT_COMMIT_INTERVAL).longValue();
        this.monitorIntervalInMs = T9tUtil.nvl(config.getMonitorInterval(), DEFAULT_MONITOR_INTERVAL).longValue();
        this.shutdownThreadpoolIntervalInMs = T9tUtil.nvl(config.getShutdownThreadpoolInterval(), DEFAULT_TIMEOUT_THREADPOOL_SHUTDOWN_MS).longValue();

        final int workerPoolSize = this.getPoolSize(config, numberOfPartitions);
        LOGGER.info("Launching kafka processing thread pool with size: {}", workerPoolSize);
        this.executorKafkaWorker = Executors.newFixedThreadPool(workerPoolSize, (threadFactory) -> {
            final String threadName = "t9t-KafkaWorker-" + workerThreadCounter.incrementAndGet();
            LOGGER.info("Launching thread {} of {} for kafka worker", threadName, workerPoolSize);
            return new Thread(threadFactory, threadName);
        });
    }

    @Override
    public Boolean call() {
        final AtomicInteger numberOfPolls = new AtomicInteger(0);
        long lastinfo = System.nanoTime();
        while (!shuttingDown.get()) {
            final int currentNum = numberOfPolls.incrementAndGet();
            final long startChunk = System.nanoTime();
            if (startChunk - lastinfo >= 60_000_000_000L) {
                // 60 seconds have elapsed... Update info
                LOGGER.debug("Polling for the {}th time", currentNum);
                lastinfo = startChunk;
            }

            // poll records
            final ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(KafkaTopicReader.DEFAULT_POLL_INTERVAL);
            if (consumerRecords == null) {
                // some exception (it is OK during shutdown)
                if (shuttingDown.get()) {
                    break;
                }
                LOGGER.error("Error polling kafka - sleeping a while, then retry");
                T9tUtil.sleepAndWarnIfInterrupted(1_000L, LOGGER, null);
            } else if (consumerRecords.count() > 0) {
                LOGGER.debug("Polling kafka returned {} new records", consumerRecords.count());
                // groups of records by partition
                final Set<TopicPartition> partitions = consumerRecords.partitions();
                for (final TopicPartition partition : partitions) {
                    final List<ConsumerRecord<String, byte[]>> recordsByPartition = consumerRecords.records(partition);
                    final KafkaSimpleMultipleRecordsProcessor kafkaMultipleRecordsProcessor = new KafkaSimpleMultipleRecordsProcessor(partition,
                            recordsByPartition, defaultAuthHeader);
                    executorKafkaWorker.submit(kafkaMultipleRecordsProcessor);
                    activeProcessors.put(partition, kafkaMultipleRecordsProcessor);
                }

                pause(partitions);
            }

            if (activeProcessors.isEmpty() && offsetsToCommit.isEmpty()) {
                // not busy after polling and nothing to commit, we can idle for a longer time
                T9tUtil.sleepAndWarnIfInterrupted(WAIT_INTERVAL_IDLE, LOGGER, "Sleep after idle interrupted");
            }

            // collect offsets from finished processors
            final Collection<TopicPartition> finishedPartitions = new ArrayList<>(activeProcessors.size());
            activeProcessors.forEach((partition, processor) -> {
                if (processor.isFinished()) {
                    finishedPartitions.add(partition);
                }
                // collect current state even if not finished yet
                final long lastProcessedOffset = processor.getLastProcessedOffset();
                if (lastProcessedOffset > 0) { // initial or error value is -1
                    offsetsToCommit.put(partition, new OffsetAndMetadata(lastProcessedOffset));
                }
            });

            finishedPartitions.forEach(partition -> activeProcessors.remove(partition));

            // resume finished partitions (if not empty)
            resume(finishedPartitions);

            // show what you are busy with (if interval reached)
            printMonitor();

            // commit current state (if interval reached)
            commitAllOffsets(commitSync);
        } // end shutdown loop

        LOGGER.info("End requested, waiting for all pending commands...");
        try {
            // do not worry about uncommitted offsets, this is handled by rebalancer
            consumer.closeReally();

            LOGGER.info("Shutting down executor pool...");
            executorKafkaWorker.shutdown();

            LOGGER.info("Await termination of executor pool...");
            executorKafkaWorker.awaitTermination(shutdownThreadpoolIntervalInMs, TimeUnit.MILLISECONDS);
            // we strongly believe in the rebalancer, which handles uncommitted offsets
            return Boolean.TRUE;
        } catch (final Exception exc) {
            LOGGER.error("There is a problem, tasks not finished within shutdown timeout: {}", exc.getClass().getSimpleName());
        }
        return Boolean.FALSE;
    }

    /* CONTROL HELPER */

    private void pause(final Set<TopicPartition> partitions) {
        final long pausedAtTime = System.currentTimeMillis();
        LOGGER.debug("PAUSE partitions {}", Arrays.toString(partitions.toArray()));
        consumer.pause(partitions);
        partitions.forEach(partition -> {
            // in theory: should always be 'absent'
            partitionStatusTable.computeIfAbsent(partition, key -> {
                return new PartitionMonitor(partition.partition(), pausedAtTime, activeProcessors.get(partition));
            });
        });
    }

    private void resume(final Collection<TopicPartition> partitions) {
        if (partitions.isEmpty()) {
            return;
        }
        LOGGER.debug("RESUME partitions {}", Arrays.toString(partitions.toArray()));
        consumer.resume(partitions);
        partitions.forEach(elem -> partitionStatusTable.remove(elem));
    }

    private void commitAllOffsets(final boolean sync) {
        commitOffsets(offsetsToCommit, sync);
    }

    // called by rebalancer
    protected void commitOffsets(final Map<TopicPartition, OffsetAndMetadata> offsets, final boolean sync) {
        if (offsets.isEmpty()) {
            return;
        }
        final long thisCommitTime = System.currentTimeMillis();
        final long timePassed = thisCommitTime - lastCommitTime;
        if (sync || timePassed > commitIntervalInMs) {
            offsets.entrySet().forEach(entry -> {
                LOGGER.trace("Sending partial commit for partition {} and offset {}", entry.getKey().partition(), entry.getValue().offset());
            });
            consumer.performPartialCommits(offsets, sync);
            lastCommitTime = thisCommitTime;
            offsets.clear();
        } else {
            LOGGER.trace("It is not time to commit... wait {}ms", commitIntervalInMs - timePassed);
        }
    }

    private int getPoolSize(final KafkaConfiguration config, final int numberOfPartitions) {
        int availableProcessors = DEFAULT_WORKER_POOL_SIZE;
        try {
            availableProcessors = Runtime.getRuntime().availableProcessors();
        } catch (final Exception exc) {
            LOGGER.error("Could not get info about available CPUs: {}", exc.getMessage());
        }
        int workerPoolSize = T9tUtil.nvl(config.getClusterManagerPoolSize(), availableProcessors);
        if (workerPoolSize > numberOfPartitions) {
            LOGGER.warn("Configured more workers ({}) than available partitions ({}) - cutting to number of overall partitions", workerPoolSize,
                    numberOfPartitions);
            workerPoolSize = numberOfPartitions;
        }
        return workerPoolSize;
    }

    // called by rebalancer as well (also at shutdown)
    protected void revokePartitions(final Collection<TopicPartition> partitions) {
        LOGGER.info("Revoke called with {} active processor threads and {} uncommitted partitions", activeProcessors.size(), offsetsToCommit.keySet().size());

        // stop processors of given partition
        final List<KafkaSimpleMultipleRecordsProcessor> stoppedProcessors = new ArrayList<>();
        for (final TopicPartition partition : partitions) {
            final KafkaSimpleMultipleRecordsProcessor removedProcessor = activeProcessors.remove(partition);
            if (removedProcessor != null) {
                removedProcessor.stopProcessing();
                LOGGER.info("Stopped processor for partition {} with {} pending records", partition, removedProcessor.getNumPending());
                stoppedProcessors.add(removedProcessor);
            }
        }

        // wait for completion of last record and shutdown gracefully
        stoppedProcessors.forEach(stoppedProcessor -> {
            LOGGER.info("Waiting on processing last record within partition {} at lastProcessedOffset {}", stoppedProcessor.getPartition(),
                    stoppedProcessor.getLastProcessedOffset());
            final long lastOffset = stoppedProcessor.waitForCompletion();
            if (lastOffset > 0) {
                offsetsToCommit.put(stoppedProcessor.getPartition(), new OffsetAndMetadata(lastOffset));
                LOGGER.info("Finished waiting on processing last record {} (-1?) within partition {} ", lastOffset,
                        stoppedProcessor.getPartition().partition());
            } else {
                LOGGER.error("Error during waiting on finishing record on partition {}", stoppedProcessor.getPartition());
            }
        });

        // remove revoked partitions from active processor and commit separately
        final Map<TopicPartition, OffsetAndMetadata> revokedOffsets = new HashMap<>(partitions.size());
        partitions.forEach(partition -> {
            final OffsetAndMetadata revokedOffset = offsetsToCommit.remove(partition);
            if (revokedOffset != null) {
                LOGGER.info("Committing offset {} from partition {} due to rebalancing", revokedOffset.offset(), partition);
                revokedOffsets.put(partition, revokedOffset);
            } else {
                LOGGER.info("NO uncommitted/pending offsets for partition {} due to rebalancing", partition);
            }
            // remove from monitor
            partitionStatusTable.remove(partition);
        });

        // commit finally
        commitOffsets(revokedOffsets, true);
    }

    /* MONITORING SECTION */

    private void printMonitor() {
        if (LOGGER.isDebugEnabled() && !partitionStatusTable.isEmpty()) {
            final long thisMonitorTime = System.currentTimeMillis();
            final long timePassed = thisMonitorTime - lastMonitorTime;
            if (timePassed > monitorIntervalInMs) {
                final StringBuilder info = new StringBuilder();
                info.append("MONITOR: ");
                for (final PartitionMonitor monitor : partitionStatusTable.values()) {
                    info.append(monitor.toString()).append(" ");
                }
                LOGGER.debug(info.toString());
                lastMonitorTime = thisMonitorTime;
            }
        }
    }

    Map<TopicPartition, PartitionMonitor> getPartitionStatusTable() {
        return this.partitionStatusTable;
    }

    class PartitionMonitor {
        final int partition;
        final long pausedWhen;
        final KafkaSimpleMultipleRecordsProcessor relatedProcessor;

        PartitionMonitor(final int partition, final long pausedWhen, final KafkaSimpleMultipleRecordsProcessor relatedProcessor) {
            this.partition = partition;
            this.pausedWhen = pausedWhen;
            this.relatedProcessor = relatedProcessor;
        }

        public int getPartition() {
            return partition;
        }

        public long getProcessingTime() {
            return System.currentTimeMillis() - pausedWhen;
        }

        public int getNumPending() {
            return relatedProcessor.getNumPending();
        }

        public int getNumRecords() {
            return relatedProcessor.getNumRecords();
        }

        @Override
        public String toString() {
            String info = "(" + getPartition() + "):[";
            info += "PausedSince: " + getProcessingTime() + "ms";
            if (relatedProcessor != null) {
                // - 1 because we added 1 for commit, and here we want to know which has been processed INDEED
                long offset = relatedProcessor.getLastProcessedOffset() - 1;
                info += ", Pending: " + relatedProcessor.getNumPending() + "/" + relatedProcessor.getNumRecords();
                info += ", LastOffset: " + (offset > 0 ? offset : "not-started");
            } else {
                LOGGER.warn("Related processor of Monitor [{}] is null", getPartition());
            }
            info += "]";
            return info;
        }

    }

}
