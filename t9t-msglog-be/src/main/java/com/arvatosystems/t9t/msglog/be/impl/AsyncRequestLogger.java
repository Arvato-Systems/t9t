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
package com.arvatosystems.t9t.msglog.be.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.LogWriterConfiguration;
import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.services.IMsglogPersistenceAccess;
import com.arvatosystems.t9t.server.ExecutionSummary;
import com.arvatosystems.t9t.server.InternalHeaderParameters;
import com.arvatosystems.t9t.server.services.IRequestLogger;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;

@Named("asynchronous")
@Singleton
public class AsyncRequestLogger implements IRequestLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncRequestLogger.class);
    private static final MessageDTO SHUTDOWN_RQ = new MessageDTO();
    private static final Integer INT_ZERO = 0;

    // tunable parameters
    private static final int DEFAULT_ALERT_ON_QUEUE_SIZE            = 1000; // max num of entries in queue before alert is triggered
    private static final int MAX_ELEMENTS_PER_BATCH                 = 200;  // max num of requests to log at a time
    private static final int MIN_ELEMENTS_FOR_RERUN                 = 100;  // if min. this num of requests had to be processed, rerun immediately!

    private static final long LONG_SLEEP_DURATION_AFTER_IDLE        = 500L; // time to sleep after no requests have been found
    private static final long SHORT_SLEEP_DURATION_AFTER_ACTIVITY   = 250L; // time to sleep after requests have been processed

    private final AtomicInteger countGood = new AtomicInteger();
    private final AtomicInteger countErrors = new AtomicInteger();
    private final AtomicInteger countCheck = new AtomicInteger();
    private final AtomicLong totalTime = new AtomicLong();

    private final LinkedTransferQueue<MessageDTO> queue = new LinkedTransferQueue<>();
    private final IMsglogPersistenceAccess persistenceAccess = Jdp.getRequired(IMsglogPersistenceAccess.class);
    private final Map<String, Function<ServiceResponse, String>> businessKeyExtractorRegistry = new ConcurrentHashMap<>();

    @IsLogicallyFinal  // set by open() method
    private ExecutorService executor;
    @IsLogicallyFinal  // set by open() method
    private Future<Boolean> writerResult;
    @IsLogicallyFinal  // set by open() method
    private LogWriterConfiguration logWriterConfiguration;
    @IsLogicallyFinal  // set by open() method
    private int alertOnQueueSize;

    private final class WriterThread implements Callable<Boolean> {
        private int count = 0;

        @Override
        public Boolean call() throws Exception {
            boolean atEnd = false;
            final List<MessageDTO> workPool = new ArrayList<>(MAX_ELEMENTS_PER_BATCH);
            do {
                try {
                    int num = queue.drainTo(workPool, MAX_ELEMENTS_PER_BATCH);
                    if (num == 0) {
                        T9tUtil.sleepAndWarnIfInterrupted(LONG_SLEEP_DURATION_AFTER_IDLE, LOGGER, null);
                    } else {
                        // check for end marker
                        for (int i = 0; i < num; ++i) {
                            if (workPool.get(i) == SHUTDOWN_RQ) {
                                LOGGER.info("Message log shutdown received after {} entries", count + i);
                                atEnd = true;
                                workPool.remove(i);
                                --num;
                            }
                        }
                        if (num > 0) {
                            count += num;
                            LOGGER.info("Logging {} messages to disk", num);
                            final long beforeWrite = System.nanoTime();
                            persistenceAccess.write(workPool);
                            if (logWriterConfiguration.getMaxWriteTimeInMillis() != null) {
                                final long writingTime = (System.nanoTime() - beforeWrite) / 1000L;
                                if (writingTime < 1000L * logWriterConfiguration.getMaxWriteTimeInMillis().longValue()) {
                                    LOGGER.debug("Writing {} entries took {} us - GREEN", num, writingTime);
                                } else {
                                    LOGGER.warn("Writing {} entries took {} us", num, writingTime);
                                }
                            }
                            if (num < MIN_ELEMENTS_FOR_RERUN) {
                                // do not attempt to rerun now
                                workPool.clear();
                                T9tUtil.sleepAndWarnIfInterrupted(SHORT_SLEEP_DURATION_AFTER_ACTIVITY, LOGGER, null);
                            }
                        }
                    }
                } catch (final Exception e) {
                    LOGGER.error("Exception {} while writing messages to disk", ExceptionUtil.causeChain(e));
                    LOGGER.error("Stack trace is ", e);
                    LOGGER.info("Skipping messages and continuing");
                }
                workPool.clear();
            } while (!atEnd);
            return Boolean.TRUE;
        }
    }


    @Override
    public void open() {
        LOGGER.info("Async Log writer selected - any message logs will be written by a separate thread");
        persistenceAccess.open();   // open disk channel
        // launch a separate thread which continuously drains the transfer queue
        executor = Executors.newSingleThreadExecutor(call -> new Thread(call, "t9t-MsgLog"));
        writerResult = executor.submit(new WriterThread());
        logWriterConfiguration = ConfigProvider.getConfiguration().getLogWriterConfiguration();
        if (logWriterConfiguration == null) {
            logWriterConfiguration = new LogWriterConfiguration();  // create a default one, to avoid double null checks
        }
        alertOnQueueSize = logWriterConfiguration.getAlertOnQueueSize() == null
            ? DEFAULT_ALERT_ON_QUEUE_SIZE : logWriterConfiguration.getAlertOnQueueSize().intValue();
    }

    @Override
    public void logRequest(final InternalHeaderParameters hdr, final ExecutionSummary summary, final RequestParameters params, final ServiceResponse response, final int retriesDone) {

        final JwtInfo jwt = hdr.getJwtInfo();
        final UserLogLevelType logLevel = calculateLogLevel(jwt, summary.getReturnCode());

        final MessageDTO m = new MessageDTO();
        m.setObjectRef              (hdr.getProcessRef());
        m.setSessionRef             (jwt.getSessionRef());
        m.setTenantId               (jwt.getTenantId());
        m.setUserId                 (jwt.getUserId());
        m.setExecutionStartedAt     (hdr.getExecutionStartedAt());
        m.setLanguageCode           (hdr.getLanguageCode());
        m.setRequestParameterPqon   (hdr.getRequestParameterPqon());
        m.setMessageId              (hdr.getMessageId());
        m.setEssentialKey           (hdr.getEssentialKey());
        if (retriesDone > 0) {
            // only set it to nonnull if retries were done
            m.setRetriesDone(retriesDone);
        }
        if (m.getEssentialKey() == null && response != null) {
            // try to extract the business key from the response
            final Function<ServiceResponse, String> extractor = businessKeyExtractorRegistry.get(response.ret$PQON());
            if (extractor != null) {
                m.setEssentialKey(extractor.apply(response));
            }
        }

        final ServiceRequestHeader h = hdr.getRequestHeader();
        if (h != null) {
            m.setRecordNo            (h.getRecordNo());
            m.setIdempotencyBehaviour(h.getIdempotencyBehaviour());
            m.setPlannedRunDate      (h.getPlannedRunDate());
            m.setInvokingProcessRef  (h.getInvokingProcessRef());
        }
        m.setRequestParameters(logLevel.ordinal() >= UserLogLevelType.REQUESTS.ordinal() ? params : null);
        m.setResponse(logLevel.ordinal() >= UserLogLevelType.FULL.ordinal() ? response : null);
        m.setProcessingTimeInMillisecs  (summary.getProcessingTimeInMillisecs());
        m.setReturnCode                 (summary.getReturnCode());
        m.setErrorDetails               (summary.getErrorDetails());
        m.setHostname                   (MessagingUtil.HOSTNAME);
        m.setPartition                  (summary.getPartitionUsed());
        if (params != null) {
            m.setTransactionOriginType(params.getTransactionOriginType());
            if (params.getWhenSent() != null) {
                // when sent via kafka, or triggered by scheduler, or issued as executeAsynchronously: calculate latency from time of initiation
                final long d = hdr.getExecutionStartedAt().toEpochMilli() - params.getWhenSent();
                m.setProcessingDelayInMillisecs(d > 0 ? Integer.valueOf((int)d) : INT_ZERO);
            }
            if (!params.was$Frozen()) {
                // unless the params are frozen, clear duplicate data which is persisted at top level
                params.setEssentialKey   (null);
                params.setMessageId      (null);
                params.setWhenSent       (null);
                params.setTransactionOriginType(null);
                params.setIdempotencyBehaviour (null);
            }
        }
        // silently discard them (but aggregate data)...
        if (ApplicationException.isOk(summary.getReturnCode()))
            countGood.incrementAndGet();
        else
            countErrors.incrementAndGet();
        totalTime.addAndGet(summary.getProcessingTimeInMillisecs());

        queue.put(m);
        if (logWriterConfiguration.getAlertInterval() != null) {
            final int alertCounter = countCheck.incrementAndGet();
            if (alertCounter >= logWriterConfiguration.getAlertInterval().intValue()) {
                // must check queue size
                countCheck.set(0);  // reset
                final int currentQueueSize = queue.size();
                if (currentQueueSize <= alertOnQueueSize) {
                    LOGGER.debug("Log message queue size is currently {} - GREEN", currentQueueSize);
                } else {
                    LOGGER.warn("Log message queue size is currently {}", currentQueueSize);
                }
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        LOGGER.info("Async message log: Normal shutdown after {} successful and {} error requests. Total processing time was {} ms.",
                countGood.get(), countErrors.get(), totalTime.get());
        // drain queue
        final long start = System.nanoTime();
        queue.put(SHUTDOWN_RQ);
        try {
            writerResult.get();
        } catch (final InterruptedException e) {
            LOGGER.error("Interrupted:", e);
        } catch (final ExecutionException e) {
            LOGGER.error("ExecutionException:", e);
        }
        final long end = System.nanoTime();
        LOGGER.info("Queue drained after {} us.", (end - start) / 1000L);
        executor.shutdown();
        persistenceAccess.close();   // close disk channel
    }

    @Override
    public void registerBusinessKeyExtractor(final String pqon, final Function<ServiceResponse, String> extractor) {
        businessKeyExtractorRegistry.put(pqon, extractor);
    }
}
