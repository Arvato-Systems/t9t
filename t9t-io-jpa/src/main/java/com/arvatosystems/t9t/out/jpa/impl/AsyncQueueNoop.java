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
package com.arvatosystems.t9t.out.jpa.impl;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.out.services.IAsyncQueue;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("noop")
@Singleton
public class AsyncQueueNoop implements IAsyncQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncQueueNoop.class);
    private final AtomicInteger counter = new AtomicInteger();

    public AsyncQueueNoop() {
        LOGGER.warn("NOOP async queue loaded - async messages will be discarded (ok for tests only)");
    }

    @Override
    public Long sendAsync(String asyncChannelId, BonaPortable payload, Long objectRef) {
        int num = counter.incrementAndGet();
        LOGGER.debug("async message {} of type {} sent to channel {}", num, payload.getClass().getCanonicalName(), asyncChannelId);
        return null;
    }

    @Override
    public void close() {
        LOGGER.info("NOOP async queue shutting down after {} messages", counter.get());
    }
}
