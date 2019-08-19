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
package com.arvatosystems.t9t.msglog.be.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.services.IMsglogPersistenceAccess;
import com.arvatosystems.t9t.server.ExecutionSummary;
import com.arvatosystems.t9t.server.InternalHeaderParameters;
import com.arvatosystems.t9t.server.services.IRequestLogger;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
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

    // tunable parameters
    private static final int MAX_ELEMENTS_PER_BATCH                 = 200;  // max num of requests to log at a time
    private static final int MIN_ELEMENTS_FOR_RERUN                 = 100;  // if min. this num of requests had to be processed, rerun immediately!

    private static final long LONG_SLEEP_DURATION_AFTER_IDLE        = 500L; // time to sleep after no requests have been found
    private static final long SHORT_SLEEP_DURATION_AFTER_ACTIVITY   = 250L; // time to sleep after requests have been processed

    private final AtomicInteger countGood = new AtomicInteger();
    private final AtomicInteger countErrors = new AtomicInteger();
    private final AtomicLong totalTime = new AtomicLong();

    private final LinkedTransferQueue<MessageDTO> queue = new LinkedTransferQueue<>();
    private ExecutorService executor;
    private Future<Boolean> writerResult;
    private final IMsglogPersistenceAccess persistenceAccess = Jdp.getRequired(IMsglogPersistenceAccess.class);

    private class WriterThread implements Callable<Boolean> {
        private int count = 0;

        @Override
        public Boolean call() throws Exception {
            boolean atEnd = false;
            final List<MessageDTO> workPool = new ArrayList<>(MAX_ELEMENTS_PER_BATCH);
            do {
                try {
                    int num = queue.drainTo(workPool, MAX_ELEMENTS_PER_BATCH);
                    if (num == 0) {
                        Thread.sleep(LONG_SLEEP_DURATION_AFTER_IDLE);
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
                            persistenceAccess.write(workPool);
                            if (num < MIN_ELEMENTS_FOR_RERUN) {
                                // do not attempt to rerun now
                                workPool.clear();
                                Thread.sleep(SHORT_SLEEP_DURATION_AFTER_ACTIVITY);
                            }
                        }
                    }
                } catch (Exception e) {
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
    }

    @Override
    public void logRequest(InternalHeaderParameters hdr, ExecutionSummary summary, RequestParameters params, ServiceResponse response) {
        // silently discard them (but aggregate data)...
        if (ApplicationException.isOk(summary.getReturnCode()))
            countGood.incrementAndGet();
        else
            countErrors.incrementAndGet();
        totalTime.addAndGet(summary.getProcessingTimeInMillisecs());

        final JwtInfo jwt = hdr.getJwtInfo();
        final MessageDTO m = new MessageDTO();
        m.setObjectRef              (hdr.getProcessRef());
        m.setSessionRef             (jwt.getSessionRef());
        m.setTenantRef              (jwt.getTenantRef());
        m.setUserId                 (jwt.getUserId());
        m.setExecutionStartedAt     (hdr.getExecutionStartedAt());
        m.setLanguageCode           (hdr.getLanguageCode());
        m.setRequestParameterPqon   (hdr.getRequestParameterPqon());

        ServiceRequestHeader h = hdr.getRequestHeader();
        if (h != null) {
            m.setMessageId           (h.getMessageId());
            m.setRecordNo            (h.getRecordNo());
            m.setIdempotencyBehaviour(h.getIdempotencyBehaviour());
            m.setPlannedRunDate      (h.getPlannedRunDate());
            m.setInvokingProcessRef  (h.getInvokingProcessRef());
        }
        m.setRequestParameters          (params);
        m.setResponse                   (response);
        m.setProcessingTimeInMillisecs  (summary.getProcessingTimeInMillisecs());
        m.setReturnCode                 (summary.getReturnCode());
        m.setErrorDetails               (summary.getErrorDetails());

        queue.put(m);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        LOGGER.info("Async message log: Normal shutdown after {} successful and {} error requests. Total processing time was {} ms.",
                countGood.get(), countErrors.get(), totalTime.get());
        // drain queue
        long start = System.currentTimeMillis();
        queue.put(SHUTDOWN_RQ);
        try {
            writerResult.get();
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted:", e);
        } catch (ExecutionException e) {
            LOGGER.error("ExecutionException:", e);
        }
        long end = System.currentTimeMillis();
        LOGGER.info("Queue drained after {} ms.", end - start);
        executor.shutdown();
        persistenceAccess.close();   // close disk channel
    }
}
