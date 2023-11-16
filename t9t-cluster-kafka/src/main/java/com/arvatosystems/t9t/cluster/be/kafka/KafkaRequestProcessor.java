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

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.TransactionOriginType;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;
import com.arvatosystems.t9t.cfg.be.StatusProvider;
import com.arvatosystems.t9t.kafka.service.IKafkaTopicReader;
import com.arvatosystems.t9t.kafka.service.impl.KafkaTopicReader;
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor;

import de.jpaw.dp.Jdp;

final class KafkaRequestProcessor implements Callable<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRequestProcessor.class);
    public static final int DEFAULT_WORKER_POOL_SIZE = 6;        // tunable: at 2 nodes and 12 partitions, this seems a reasonable default
    public static final long SLOW_PROCESSING_TIMEOUT = 15_000L;  // after how many ms to give up processing

    private final IUnauthenticatedServiceRequestExecutor requestProcessor = Jdp.getRequired(IUnauthenticatedServiceRequestExecutor.class);

    private final AtomicInteger workerThreadCounter = new AtomicInteger();
    private final ExecutorService executorKafkaWorker;
    private final IKafkaTopicReader consumer;
    private final AuthenticationParameters defaultAuthHeader;
    private final AtomicInteger pendingRequestsCounter = new AtomicInteger(0);
    private final AtomicInteger uniqueRequestsCounter = new AtomicInteger(0);

    protected KafkaRequestProcessor(final KafkaConfiguration config, final IKafkaTopicReader consumer) {
        this.consumer = consumer;

        if (config.getClusterManagerApiKey() != null)  {
            this.defaultAuthHeader = new ApiKeyAuthentication(config.getClusterManagerApiKey());
            this.defaultAuthHeader.freeze();
        } else {
            this.defaultAuthHeader = null;
        }

        final int workerPoolSize = T9tUtil.nvl(config.getClusterManagerPoolSize(), DEFAULT_WORKER_POOL_SIZE);
        executorKafkaWorker = Executors.newFixedThreadPool(workerPoolSize, (threadFactory) -> {
            final String threadName = "t9t-KafkaWorker-" + workerThreadCounter.incrementAndGet();
            LOGGER.info("Launching thread {} of {} for kafka worker", threadName, workerPoolSize);
            return new Thread(threadFactory, threadName);
        });
    }

    private void processRequest(final ServiceRequest srq, final int partition, final long offset, final String key) {
        srq.setPartitionUsed(partition);
        final RequestParameters rp = srq.getRequestParameters();
        final ServiceRequestHeader optHdr = srq.getRequestHeader();
        final UUID messageId = rp.getMessageId() == null ? optHdr != null ? optHdr.getMessageId() : null : rp.getMessageId();
        if (srq.getAuthentication() == null) {
            // no specific user provided: use default (consider security!) or fail, if none configured (default)
            if (defaultAuthHeader != null) {
                srq.setAuthentication(defaultAuthHeader);
            } else {
                LOGGER.error("Received request {} (ID {}) without authentication header", rp.ret$PQON(), messageId);
                return;
            }
        }
        if (rp.getTransactionOriginType() == null) {
            rp.setTransactionOriginType(TransactionOriginType.KAFKA); // some other kafka based source
        }
        final int uniqueId = uniqueRequestsCounter.incrementAndGet();
        final int before = pendingRequestsCounter.incrementAndGet();

        LOGGER.debug("Submitting task {} (ID {}), PQON {}, key {}, partition {}, pending = {}", uniqueId, messageId, rp.ret$PQON(), key, partition, before);
        executorKafkaWorker.submit(() -> {
            try {
                requestProcessor.execute(srq);
            } catch (Exception e) {
                LOGGER.error("Task {} (ID {}), PQON {} failed due to {}:{}", uniqueId, messageId, rp.ret$PQON(), e.getClass().getSimpleName(), e.getMessage());
            }
            final int after = pendingRequestsCounter.decrementAndGet();
            LOGGER.debug("Completed execute of task {} (ID {}), PQON {}, pending = {}", uniqueId, messageId, rp.ret$PQON(), after);
        });
    }

    protected void commitAfterRequestsProcessed(final AtomicInteger baseline, final KafkaConsumer<String, byte[]> kafkaConsumer) {
        // first, wait for requests to have fully processed
        int iteration = 0;
        final int baselineBefore = baseline.get();
        final long processingStart = System.currentTimeMillis();
        for (;;) {
            int pending = pendingRequestsCounter.get();
            if (pending < 0) {
                LOGGER.error("Bug: reported {} pending requests", pending);
                pendingRequestsCounter.set(0);
                pending = 0;
                break;
            } else if (pending == 0) {
                break;
            } else if (pending <= baselineBefore) {
                // should be OK, we did not become worse
                break;
            } else {
                // wait some time to allow requests to finish
                ++iteration;
                if (iteration > 5) {
                    final long now = System.currentTimeMillis();
                    LOGGER.warn("SLOW request processing! {} pending requests after delay of {} ms", pending, now - processingStart);
                    if (now - processingStart >= SLOW_PROCESSING_TIMEOUT) {
                        // do not stall for more than 15 seconds
                        break;
                    }
                }
                T9tUtil.sleepAndWarnIfInterrupted(pending > 5 ? 1000L : 200L, LOGGER, "SLOW request processing / sleep interrupted");
            }
        }
        final int pending = pendingRequestsCounter.get();
        if (pending == 0) {
            LOGGER.debug("Will commit - all requests done");
        } else {
            LOGGER.warn("Will commit - but {} requests still pending", pending);
        }
        if (pending != baselineBefore) {
            baseline.set(pending >= 0 ? pending : 0);
        }
        // finally, commit the messages
        kafkaConsumer.commitAsync();
    }

    @Override
    public Boolean call() {
        final AtomicInteger numberOfPolls = new AtomicInteger(0);
        final AtomicInteger baseline = new AtomicInteger(0);
        long lastinfo = System.nanoTime();
        while (!StatusProvider.isShutdownInProgress()) {
            final int currentNum = numberOfPolls.incrementAndGet();
            final long startChunk = System.nanoTime();
            if (startChunk - lastinfo >= 60_000_000_000L) {
                // 60 seconds have elapsed... Update info
                LOGGER.debug("Polling for the {}th time", currentNum);
                lastinfo = startChunk;
            }
            final int recordsProcessed = consumer.pollAndProcess(this::processRequest, ServiceRequest.class, KafkaTopicReader.DEFAULT_POLL_INTERVAL,
              kafkaConsumer -> commitAfterRequestsProcessed(baseline, kafkaConsumer));
            if (recordsProcessed < 0) {
                // some exception (it is OK during shutdown)
                if (StatusProvider.isShutdownInProgress()) {
                    break;
                }
                LOGGER.error("Error polling kafka - sleeping a while, then retry");
                T9tUtil.sleepAndWarnIfInterrupted(1_000L, LOGGER, null);
            }
        }
        LOGGER.info("End requested, waiting for all pending commands...");
        try {
            consumer.close();
            executorKafkaWorker.awaitTermination(10000L, TimeUnit.MILLISECONDS);
            LOGGER.info("Shutting down executor pool...");
            executorKafkaWorker.shutdown();
            LOGGER.info("All tasks terminated, committing SYNC before ending thread...");
        } catch (final InterruptedException e) {
            LOGGER.error("There is a problem, tasks not finished within shutdown timeout: {}", e.getClass().getSimpleName());
//            // only submit the ones which have been processed
//            checkForFinishedRequests();
//            LOGGER.debug("Sending sync commit for {} records (out of {})", finishedRequests.size(), pendingRequests.size());
//            consumer.commitSync(finishedRequests, null);
        }
        return Boolean.TRUE;
    }
}
