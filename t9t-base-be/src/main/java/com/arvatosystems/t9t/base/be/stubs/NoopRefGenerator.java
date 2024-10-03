/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IRefGenerator;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * This implementation serves as a stub in case no jpa module is available.
 */
@Fallback
@Any
@Singleton
public class NoopRefGenerator implements IRefGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopRefGenerator.class);

    private final T9tServerConfiguration configuration = Jdp.getRequired(T9tServerConfiguration.class);
    private final long scaledOffsetForLocation;
    private final Map<String, AtomicLong> generatorMap = new HashMap<>(500);
    private final AtomicLong[] generatorTab = new AtomicLong[OFFSET_BACKUP_LOCATION];
    /** current time minus some offset to keep numbers as small as possible (FT-3222) */
    private final long randomOffset = (System.currentTimeMillis() - 1492_000_000_000L) * 10000;

    public NoopRefGenerator() {
        LOGGER.warn("Time based in memory Ref generator selected, be sure this is for testing / single instance only!");

        scaledOffsetForLocation = (long)configuration.getKeyPrefetchConfiguration().getLocationOffset() * OFFSET_BACKUP_LOCATION;
        for (int i = 0; i < OFFSET_BACKUP_LOCATION; ++i) {
            generatorTab[i] = new AtomicLong();
        }
    }

    @Override
    public long generateRef(final String tablename, final int rttiOffset) {
        if ((rttiOffset < 0) || (rttiOffset >= OFFSET_BACKUP_LOCATION)) {
            throw new InvalidParameterException("Bad rtti offset: " + rttiOffset);
        }
        return (generatorTab[rttiOffset].incrementAndGet() * KEY_FACTOR) + scaledOffsetForLocation + rttiOffset + randomOffset;
    }

    @Override
    public long generateUnscaledRef(String sequenceName) {
        return generatorMap.computeIfAbsent(sequenceName, x -> new AtomicLong()).getAndIncrement();
    }
}
