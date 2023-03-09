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
package com.arvatosystems.t9t.cluster.be.kafka;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;
import com.arvatosystems.t9t.kafka.service.IKafkaTopicReader;
import com.arvatosystems.t9t.kafka.service.impl.KafkaTopicReader;
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor;

import de.jpaw.dp.Jdp;

final class KafkaRequestProcessor implements Callable<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRequestProcessor.class);
    public static final int DEFAULT_WORKER_POOL_SIZE = 4;

    private final IUnauthenticatedServiceRequestExecutor requestProcessor = Jdp.getRequired(IUnauthenticatedServiceRequestExecutor.class);

    private final AtomicInteger workerThreadCounter = new AtomicInteger();
    private final ExecutorService executorKafkaWorker;
    private final IKafkaTopicReader consumer;
    private final AtomicBoolean pleaseStop;
    private final AuthenticationParameters authHeader;
    private final AtomicInteger pendingRequestsCounter = new AtomicInteger(0);

    protected KafkaRequestProcessor(final KafkaConfiguration config, final IKafkaTopicReader consumer, final AtomicBoolean pleaseStop) {
        this.consumer = consumer;
        this.authHeader = new ApiKeyAuthentication(config.getClusterManagerApiKey());
        this.authHeader.freeze();
        this.pleaseStop = pleaseStop;

        final int workerPoolSize = T9tUtil.nvl(config.getClusterManagerPoolSize(), DEFAULT_WORKER_POOL_SIZE);
        executorKafkaWorker = Executors.newFixedThreadPool(workerPoolSize, (threadFactory) -> {
            final String threadName = "t9t-KafkaWorker-" + workerThreadCounter.incrementAndGet();
            LOGGER.info("Launching thread {} of {} for kafka worker", threadName, workerPoolSize);
            return new Thread(threadFactory, threadName);
        });
    }

    private void processRequest(final String key, final RequestParameters rp) {
        executorKafkaWorker.submit(() -> {
            pendingRequestsCounter.incrementAndGet();
            final ServiceRequest srq = new ServiceRequest();
            srq.setRequestParameters(rp);
            srq.setAuthentication(authHeader);
            try {
                requestProcessor.execute(srq);
            } catch (Exception e) {
                LOGGER.error("Request {} failed due to {}:{}", rp.ret$PQON(), e.getClass().getSimpleName(), e.getMessage());
            }
            pendingRequestsCounter.decrementAndGet();
        });
    }

    protected void commitAfterRequestsProcessed(final AtomicInteger baseline, final KafkaConsumer<String, byte[]> kafkaConsumer) {
        // first, wait for requests to have fully processed
        int iteration = 0;
        final int baselineBefore = baseline.get();
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
                    LOGGER.warn("SLOW request processing! {} pending requests after delays", pending);
                } else {
                    try {
                        Thread.sleep(pending > 5 ? 1000L : 200L);
                    } catch (InterruptedException e) {
                        LOGGER.warn("SLOW request processing / sleep interrupted");
                    }
                }
            }
        }
        final int pending = pendingRequestsCounter.get();
        if (pending == 0) {
            LOGGER.debug("Will commit - all requests done");
        } else {
            LOGGER.debug("Will commit - {} requests still pending", pending);
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
        while (!pleaseStop.get()) {
            final int currentNum = numberOfPolls.incrementAndGet();
            final long startChunk = System.nanoTime();
            if (startChunk - lastinfo >= 60_000_000_000L) {
                // 60 seconds have elapsed... Update info
                LOGGER.debug("Polling for the {}th time", currentNum);
                lastinfo = startChunk;
            }
            final int recordsProcessed = consumer.pollAndProcess(this::processRequest, RequestParameters.class, KafkaTopicReader.DEFAULT_POLL_INTERVAL,
              kafkaConsumer -> commitAfterRequestsProcessed(baseline, kafkaConsumer));
            if (recordsProcessed < 0) {
                // some exception (it is OK during shutdown)
                if (pleaseStop.get()) {
                    break;
                }
                LOGGER.error("Error polling kafka - sleeping a while, then retry");
                try {
                    Thread.sleep(1_000L);
                } catch (InterruptedException e) {
                }
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