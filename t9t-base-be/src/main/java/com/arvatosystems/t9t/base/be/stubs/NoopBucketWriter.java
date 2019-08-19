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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.event.BucketWriteKey;
import com.arvatosystems.t9t.base.services.IBucketWriter;

import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Fallback
@Any
@Singleton
public class NoopBucketWriter implements IBucketWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopBucketWriter.class);

    public NoopBucketWriter() {
        LOGGER.warn("Discarding bucket writer selected - bucket writing commands will be ignored");
    }

    @Override
    public void writeToBuckets(Map<BucketWriteKey, Integer> cmds) {
        LOGGER.debug("No-OP bucket writer: Disarding {}", ToStringHelper.toStringML(cmds));
    }

    @Override
    public void open() {   // NO OP
    }

    @Override
    public void close() {   // NO OP
    }
}
