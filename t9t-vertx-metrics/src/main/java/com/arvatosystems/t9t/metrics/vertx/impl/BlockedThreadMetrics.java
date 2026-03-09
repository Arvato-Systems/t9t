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
package com.arvatosystems.t9t.metrics.vertx.impl;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.internal.threadchecker.BlockedThreadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MeterBinder} for exporting Vert.x blocked thread metrics to Prometheus.<br>
 * Tracks occurrences and duration of thread blocking events on event loop and worker threads.<br>
 * <br>
 * Sample output:
 * <pre>
 * vertx_blocked_thread_total{pool_type="eventloop",} 5.0
 * vertx_blocked_thread_total{pool_type="worker",} 2.0
 * vertx_blocked_thread_total{pool_type="internal-blocking",} 0.0
 * vertx_blocked_thread_duration_seconds{pool_type="eventloop",quantile="0.5",} 0.250123
 * vertx_blocked_thread_duration_seconds{pool_type="worker",quantile="0.5",} 1.050456
 * </pre>
 */
public class BlockedThreadMetrics implements MeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockedThreadMetrics.class);

    private static final String BLOCKED_THREAD_TOTAL_METRIC = "vertx.blocked.thread.total";
    private static final String BLOCKED_THREAD_TOTAL_DESCRIPTION = "Total number of blocked thread events detected";

    private static final String BLOCKED_THREAD_DURATION_METRIC = "vertx.blocked.thread.duration";
    private static final String BLOCKED_THREAD_DURATION_DESCRIPTION = "Duration of blocked thread events in seconds";

    private static final String POOL_TYPE_TAG = "pool_type";

    private Counter eventLoopBlockedCounter;
    private Counter workerBlockedCounter;
    private Counter internalBlockingBlockedCounter;

    private Timer eventLoopBlockedTimer;
    private Timer workerBlockedTimer;
    private Timer internalBlockingBlockedTimer;

    /**
     * Returns a handler that can be used with {@code BlockedThreadChecker.setThreadBlockedHandler()}.
     * This handler will record metrics for blocked thread events while preserving the default logging behavior.
     * @return a Handler for blocked thread events
     */
    public Handler<BlockedThreadEvent> getBlockedThreadHandler() {
        return event -> {
            if (event == null) {
                return;
            }

            // Apply default BlockedThreadChecker behavior: log warnings with optional stack trace
            applyDefaultBlockedThreadLogging(event);

            final String threadName = event.thread().getName();
            final String poolType = extractPoolType(threadName);
            final long durationNanos = event.duration();

            // Record metrics for blocked thread event
            switch (poolType) {
                case "eventloop" -> {
                    if (eventLoopBlockedCounter != null) {
                        eventLoopBlockedCounter.increment();
                    }
                    if (eventLoopBlockedTimer != null) {
                        eventLoopBlockedTimer.record(durationNanos, TimeUnit.NANOSECONDS);
                    }
                }
                case "worker" -> {
                    if (workerBlockedCounter != null) {
                        workerBlockedCounter.increment();
                    }
                    if (workerBlockedTimer != null) {
                        workerBlockedTimer.record(durationNanos, TimeUnit.NANOSECONDS);
                    }
                }
                case "internal-blocking" -> {
                    if (internalBlockingBlockedCounter != null) {
                        internalBlockingBlockedCounter.increment();
                    }
                    if (internalBlockingBlockedTimer != null) {
                        internalBlockingBlockedTimer.record(durationNanos, TimeUnit.NANOSECONDS);
                    }
                }
                default -> LOGGER.debug("Unknown pool type: {}", poolType);
            }
        };
    }

    /**
     * Applies the default BlockedThreadChecker logging behavior.
     * This matches the default behavior from io.vertx.core.internal.threadchecker.BlockedThreadChecker.
     */
    private static void applyDefaultBlockedThreadLogging(BlockedThreadEvent event) {
        final String message = "Thread " + event.thread().getName()
            + " has been blocked for " + TimeUnit.NANOSECONDS.toMillis(event.duration())
            + " ms, time limit is " + TimeUnit.NANOSECONDS.toMillis(event.maxExecTime()) + " ms";

        if (event.duration() <= event.warningExceptionTime()) {
            LOGGER.warn(message);
        } else {
            VertxException stackTrace = new VertxException("Thread blocked");
            stackTrace.setStackTrace(event.thread().getStackTrace());
            LOGGER.warn(message, stackTrace);
        }
    }

    /**
     * Extracts the pool type from the thread name.
     * <ul>
     *   <li>"vert.x-eventloop-thread-*" → "eventloop"</li>
     *   <li>"vert.x-worker-thread-*" → "worker"</li>
     *   <li>"vert.x-internal-blocking-*" → "internal-blocking"</li>
     * </ul>
     *
     * @param threadName the thread name to parse
     * @return the pool type or "unknown" if not recognized
     */
    private static String extractPoolType(final String threadName) {
        if (threadName == null) {
            return "unknown";
        }

        if (threadName.startsWith("vert.x-eventloop-thread")) {
            return "eventloop";
        } else if (threadName.startsWith("vert.x-worker-thread")) {
            return "worker";
        } else if (threadName.startsWith("vert.x-internal-blocking")) {
            return "internal-blocking";
        }

        return "unknown";
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        LOGGER.info("Registering blocked thread metrics...");

        // Create counters for each pool type
        eventLoopBlockedCounter = Counter.builder(BLOCKED_THREAD_TOTAL_METRIC)
            .description(BLOCKED_THREAD_TOTAL_DESCRIPTION)
            .tag(POOL_TYPE_TAG, "eventloop")
            .register(registry);

        workerBlockedCounter = Counter.builder(BLOCKED_THREAD_TOTAL_METRIC)
            .description(BLOCKED_THREAD_TOTAL_DESCRIPTION)
            .tag(POOL_TYPE_TAG, "worker")
            .register(registry);

        internalBlockingBlockedCounter = Counter.builder(BLOCKED_THREAD_TOTAL_METRIC)
            .description(BLOCKED_THREAD_TOTAL_DESCRIPTION)
            .tag(POOL_TYPE_TAG, "internal-blocking")
            .register(registry);

        // Create timers for each pool type
        eventLoopBlockedTimer = Timer.builder(BLOCKED_THREAD_DURATION_METRIC)
            .description(BLOCKED_THREAD_DURATION_DESCRIPTION)
            .tag(POOL_TYPE_TAG, "eventloop")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .publishPercentileHistogram()
            .register(registry);

        workerBlockedTimer = Timer.builder(BLOCKED_THREAD_DURATION_METRIC)
            .description(BLOCKED_THREAD_DURATION_DESCRIPTION)
            .tag(POOL_TYPE_TAG, "worker")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .publishPercentileHistogram()
            .register(registry);

        internalBlockingBlockedTimer = Timer.builder(BLOCKED_THREAD_DURATION_METRIC)
            .description(BLOCKED_THREAD_DURATION_DESCRIPTION)
            .tag(POOL_TYPE_TAG, "internal-blocking")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .publishPercentileHistogram()
            .register(registry);

        LOGGER.info("Blocked thread metrics registered");
    }
}
