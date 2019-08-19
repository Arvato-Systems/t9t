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
package com.arvatosystems.t9t.base.be.stubs;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.server.ExecutionSummary;
import com.arvatosystems.t9t.server.InternalHeaderParameters;
import com.arvatosystems.t9t.server.services.IRequestLogger;

import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Fallback
@Any
@Singleton
public class NoopMsglogWriter implements IRequestLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopMsglogWriter.class);
    private final AtomicInteger countGood = new AtomicInteger();
    private final AtomicInteger countErrors = new AtomicInteger();
    private final AtomicLong totalTime = new AtomicLong();

    @Override
    public void open() {
        LOGGER.warn("No-OP message log selected, be sure this is for testing only!");
    }

    @Override
    public void logRequest(InternalHeaderParameters hdr, ExecutionSummary summary, RequestParameters params, ServiceResponse response) {
        // silently discard them (but aggregate data)...
        if (ApplicationException.isOk(summary.getReturnCode()))
            countGood.incrementAndGet();
        else
            countErrors.incrementAndGet();
        totalTime.addAndGet(summary.getProcessingTimeInMillisecs());
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        LOGGER.info("No-OP message log: Normal shutdown after {} successful and {} error requests. Total processing time was {} ms.",
                countGood.get(), countErrors.get(), totalTime.get());
    }
}
