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
package com.arvatosystems.t9t.msglog.be.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.services.IMsglogPersistenceAccess;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Fallback
@Singleton
public class RequestLoggerToDevNull implements IMsglogPersistenceAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggerToDevNull.class);
    private final AtomicInteger count = new AtomicInteger();

    public int getCount() {
        return count.get();
    }

    @Override
    public void open() {
        LOGGER.warn("Msglog to /dev/null - do not use in production");
    }

    @Override
    public void write(final List<MessageDTO> entries) {
        count.incrementAndGet();
    }

    @Override
    public void close() {
    }
}
