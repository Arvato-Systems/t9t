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
package com.arvatosystems.t9t.bucket.be.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.event.BucketWriteKey;
import com.arvatosystems.t9t.base.services.IBucketWriter;
import com.arvatosystems.t9t.bucket.services.IBucketPersistenceAccess;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

@Singleton
public class AsyncBucketWriter implements IBucketWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncBucketWriter.class);
    private static final Map<BucketWriteKey, Integer> SHUTDOWN_RQ = new HashMap<BucketWriteKey, Integer>();  // empoty map for shutdown

    // tunable parameters
    private static final int MAX_ELEMENTS_TOTAL                     = 1000; // max num of entries at a time (do not increase, Oracle limit)
    private static final int MAX_ELEMENTS_PER_BATCH                 = 200;  // max num of transactions to log at a time
    private static final int MIN_ELEMENTS_FOR_RERUN                 = 100;  // if min. this num of requests had to be processed, rerun immediately!

    private static final long LONG_SLEEP_DURATION_AFTER_IDLE        = 500L; // time to sleep after no requests have been found
    private static final long SHORT_SLEEP_DURATION_AFTER_ACTIVITY   = 250L; // time to sleep after requests have been processed

    private final LinkedTransferQueue<Map<BucketWriteKey, Integer>> queue = new LinkedTransferQueue<Map<BucketWriteKey, Integer>>();
    private ExecutorService executor;
    private Future<Boolean> writerResult;
    private final IBucketPersistenceAccess persistenceAccess = Jdp.getRequired(IBucketPersistenceAccess.class);

    private class WriterThread implements Callable<Boolean> {
        private int count = 0;

        @Override
        public Boolean call() throws Exception {
            boolean atEnd = false;
            List<Map<BucketWriteKey, Integer>> workPool = new ArrayList<Map<BucketWriteKey, Integer>>(MAX_ELEMENTS_PER_BATCH);
            do {
                int num = queue.drainTo(workPool, MAX_ELEMENTS_PER_BATCH);
                if (num == 0) {
                    Thread.sleep(LONG_SLEEP_DURATION_AFTER_IDLE);
                } else {
                    // check for end marker
                    for (int i = 0; i < num; ++i) {
                        if (workPool.get(i) == SHUTDOWN_RQ) {
                            LOGGER.info("Bucket writer shutdown received after {} entries", count + i);
                            atEnd = true;
                            workPool.remove(i);
                            --num;
                        }
                    }
                    try {
                        if (num > 0) {
                            count += num;

                            // combine all the entries. For this create a common map, keeping the insertion order
                            int currentSize = 0;
                            Map<BucketWriteKey, Integer> sortedPool = new LinkedHashMap<BucketWriteKey, Integer>(2 * MAX_ELEMENTS_TOTAL);
                            for (Map<BucketWriteKey, Integer> e1: workPool) {
                                if (currentSize + e1.size() > MAX_ELEMENTS_TOTAL && currentSize > 0) {
                                    LOGGER.info("EXTRA writing bucket required due to MAX_TOTAL limit");
                                    flushPool(sortedPool, num, currentSize);
                                    currentSize = 0;
                                }
                                if (e1.size() > MAX_ELEMENTS_TOTAL) {
                                    LOGGER.warn("Single Transaction too big to write buckets - fallback: Buckets of single source TX may be written by multiple writer TX");
                                    for (Map.Entry<BucketWriteKey, Integer> bi: e1.entrySet()) {
                                        sortedPool.merge(bi.getKey(), bi.getValue(), (a, b) -> Integer.valueOf(a.intValue() | b.intValue()));
                                        // flush once max. number of tx has been reached
                                        if (sortedPool.size() >= MAX_ELEMENTS_TOTAL)
                                            flushPool(sortedPool, num, MAX_ELEMENTS_TOTAL);
                                    }
                                    if (!sortedPool.isEmpty())
                                        flushPool(sortedPool, num, MAX_ELEMENTS_TOTAL);
                                } else {
                                    // aggregated approach - faster
                                    for (Map.Entry<BucketWriteKey, Integer> bi: e1.entrySet()) {
                                        sortedPool.merge(bi.getKey(), bi.getValue(), (a, b) -> Integer.valueOf(a.intValue() | b.intValue()));
                                    }
                                    currentSize += e1.size();
                                }
                            }
                            flushPool(sortedPool, num, currentSize);

                            if (num < MIN_ELEMENTS_FOR_RERUN) {
                                // do not attempt to rerun now
                                workPool.clear();
                                Thread.sleep(SHORT_SLEEP_DURATION_AFTER_ACTIVITY);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Exception caught in AsyncBucketWriter - discarding some entries and recovering", ExceptionUtil.causeChain(e));
                    }
                }
                workPool.clear();
            } while (!atEnd);
            workPool = null;
            return Boolean.TRUE;
        }
    }

    protected void flushPool(Map<BucketWriteKey, Integer> sortedPool, int num, int currentSize) {
        LOGGER.info("Writing bucket entries of {} transactions to disk: combined {} total entries to {}", num, currentSize, sortedPool.size());
        persistenceAccess.write(sortedPool);
        LOGGER.debug("Writing complete");
        sortedPool.clear();
    }

    @Override
    public void open() {
        LOGGER.info("Async bucket writer selected - any bucket entries will be written by a separate thread");
        persistenceAccess.open();   // open disk channel
        // launch a separate thread which continuously drains the transfer queue
        executor = Executors.newSingleThreadExecutor((call) -> { return new Thread(call, "t9t-BucketWriter"); });
        writerResult = executor.submit(new WriterThread());
    }

    @Override
    public void writeToBuckets(Map<BucketWriteKey, Integer> cmds) {
        queue.put(cmds);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        LOGGER.info("Async bucket writer: Normal shutdown.");
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
