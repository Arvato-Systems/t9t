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
package com.arvatosystems.t9t.out.jpa.impl;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
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
    public void sendAsync(final RequestContext ctx, final AsyncChannelDTO channel, final BonaPortable payload, final Long objectRef,
            final int partition, final String recordKey) {
        final int num = counter.incrementAndGet();
        LOGGER.debug("async message {} of type {} sent to channel {} and partition {}, record key {}",
          num, payload.getClass().getCanonicalName(), channel.getAsyncChannelId(), partition, recordKey);
    }

    @Override
    public void close() {
        LOGGER.info("NOOP async queue shutting down after {} messages", counter.get());
    }
}
