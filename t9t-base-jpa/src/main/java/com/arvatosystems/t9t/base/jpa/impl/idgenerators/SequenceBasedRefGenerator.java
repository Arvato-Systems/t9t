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
package com.arvatosystems.t9t.base.jpa.impl.idgenerators;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;
import java.util.function.LongSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.IRefGenerator;
import com.arvatosystems.t9t.base.services.ISingleRefGenerator;
import com.arvatosystems.t9t.cfg.be.DatabaseBrandType;
import com.arvatosystems.t9t.cfg.be.KeyPrefetchConfiguration;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;

/**
 * Provides generators for technical Ids (database table primary keys). Standard JPA auto generated keys cannot be used for several reasons:
 * <ul>
 * <li>for geographical redundancy, we want a location specific offset on the key</li>
 * <li>we want run time type information (RTTI) to be baked into the generated value</li>
 * <li>in order not to loose too many valid values, caching should extend more than just the current request, therefore the key generation should span across
 * all sessions</li>
 * <li>some change of the computing strategy (sequence, table, etc) should be centralized to a single class,</li>
 * </ul>
 * It is enforced that this class is used for all entities requiring an auto generated keys.
 */
@Singleton
public class SequenceBasedRefGenerator extends AbstractIdGenerator implements IRefGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceBasedRefGenerator.class);

    private static final String DEFAULT_STRATEGY         = "noop"; // fallback strategy - not for production
    private static final int DEFAULT_CACHE_SIZE          = 500; // how many sequences we generate per single DB-sequence fetch for scaled keys
    private static final int DEFAULT_CACHE_SIZE_UNSCALED =  10; // how many sequences we generate per single DB-sequence fetch for unscaled keys
    private static final int NUM_SEQUENCES               = 100; // how many sequences we use to obtain the IDs
    private static final int NUM_SEQUENCES_UNSCALED      =  10; // how many sequences we use to obtain unscaled IDs
    private final LongSupplier[] generatorTab            = new LongSupplier[NUM_SEQUENCES];
    private final LongSupplier[] generatorTab50xx        = new LongSupplier[NUM_SEQUENCES_UNSCALED];
    private final LongSupplier[] generatorTab60xx        = new LongSupplier[NUM_SEQUENCES_UNSCALED];
    private final LongSupplier[] generatorTab70xx        = new LongSupplier[NUM_SEQUENCES_UNSCALED];
    private final Map<String, LongSupplier> generatorMap = new ConcurrentHashMap<>(500);
    private final long scaledOffsetForLocation;
    private final boolean useSequencePerTable;

    private final T9tServerConfiguration configuration = Jdp.getRequired(T9tServerConfiguration.class);
    private final DatabaseBrandType dialect = configuration.getDatabaseConfiguration().getDatabaseBrand();
    private final int cacheSize;
    final ISingleRefGenerator refGeneratorFactory;

    public SequenceBasedRefGenerator() {
        final KeyPrefetchConfiguration keyConfig = configuration.getKeyPrefetchConfiguration();
        final int cacheSizeUnscaled;
        final String factoryQualifier;

        if (keyConfig != null) {
            scaledOffsetForLocation = (long) keyConfig.getLocationOffset() * OFFSET_BACKUP_LOCATION;
            cacheSize = keyConfig.getCacheSize() == null ? DEFAULT_CACHE_SIZE : keyConfig.getCacheSize().intValue();
            cacheSizeUnscaled = keyConfig.getCacheSizeUnscaled() == null ? DEFAULT_CACHE_SIZE_UNSCALED : keyConfig.getCacheSizeUnscaled().intValue();
            factoryQualifier = T9tUtil.isBlank(keyConfig.getStrategy()) ? DEFAULT_STRATEGY : keyConfig.getStrategy();
            useSequencePerTable = !Boolean.FALSE.equals(keyConfig.getUseSequencePerTable());
        } else {
            scaledOffsetForLocation = 0;
            cacheSize = DEFAULT_CACHE_SIZE;
            cacheSizeUnscaled = DEFAULT_CACHE_SIZE_UNSCALED;
            factoryQualifier = DEFAULT_STRATEGY;
            useSequencePerTable = true;
        }
        LOGGER.info("Creating object references via sequence per {} for database {} by generator {} with cache sizes {} / {}, locationOffset is {}",
            useSequencePerTable ? "table" : "RTTI",
            dialect.name(), factoryQualifier, cacheSize, cacheSizeUnscaled, scaledOffsetForLocation);
        refGeneratorFactory = Jdp.getRequired(ISingleRefGenerator.class, factoryQualifier);

        if (!useSequencePerTable) {
            for (int i = 0; i < NUM_SEQUENCES; ++i) {
                final String sequenceName = sequenceNameForIndex(i);
                final String key = refGeneratorFactory.needSelectStatement() ? selectStatementForSequence(dialect, sequenceName) : sequenceName;
                generatorTab[i] = new CachingRefSupplier(key, cacheSize, refGeneratorFactory);
            }
        }

        final IntFunction<LongSupplier> rttiBasedGeneratorUnscaled = i -> {
            final String sequenceName = sequenceNameForIndex(i);
            final String key = refGeneratorFactory.needSelectStatement() ? selectStatementForSequence(dialect, sequenceName) : sequenceName;
            return new CachingRefSupplier(key, cacheSizeUnscaled, refGeneratorFactory);
        };
        for (int i = 0; i < NUM_SEQUENCES_UNSCALED; ++i) {
            generatorTab50xx[i] = rttiBasedGeneratorUnscaled.apply(5000 + i);
            generatorTab60xx[i] = rttiBasedGeneratorUnscaled.apply(6000 + i);
            generatorTab70xx[i] = rttiBasedGeneratorUnscaled.apply(7000 + i);
        }
    }

    @Override
    public long generateRef(final String tablename, final int rttiOffset) {
        if ((rttiOffset < 0) || (rttiOffset >= OFFSET_BACKUP_LOCATION)) {
            throw new InvalidParameterException("Bad rtti offset: " + rttiOffset);
        }
        final LongSupplier supplier = !useSequencePerTable
            ? generatorTab[rttiOffset % NUM_SEQUENCES]
            : generatorMap.computeIfAbsent(tablename,
                tn -> {
                    final String sequenceName = sequenceNameForTable(tablename);
                    final String key = refGeneratorFactory.needSelectStatement() ? selectStatementForSequence(dialect, sequenceName) : sequenceName;
                    return new CachingRefSupplier(key, cacheSize, refGeneratorFactory);
                }
              );
        return supplier.getAsLong() * KEY_FACTOR + scaledOffsetForLocation + rttiOffset;
    }

    @Override
    public long generateUnscaledRef(final int rttiOffset) {
        final LongSupplier g;
        if ((rttiOffset >= 5000) && (rttiOffset < (5000 + NUM_SEQUENCES_UNSCALED))) {
            g = generatorTab50xx[rttiOffset - 5000];
        } else if ((rttiOffset >= 6000) && (rttiOffset < (6000 + NUM_SEQUENCES_UNSCALED))) {
            g = generatorTab60xx[rttiOffset - 6000];
        } else if ((rttiOffset >= 7000) && (rttiOffset < (7000 + NUM_SEQUENCES_UNSCALED))) {
            g = generatorTab70xx[rttiOffset - 7000];
        } else {
            throw new InvalidParameterException("Bad rtti offset: " + rttiOffset);
        }
        return (g.getAsLong() * 2L) + (scaledOffsetForLocation > 0 ? 1 : 0);
    }

    private static final class CachingRefSupplier implements LongSupplier {
        private volatile long lastProvidedValue;
        private volatile int remainingCachedIds;
        private final int cacheSize;
        private final String sqlCommandForNextValue;
        private final ISingleRefGenerator uncachedRefSupplier;

        private CachingRefSupplier(@Nonnull final String sqlCommandForNextValue, final int cacheSize, @Nonnull ISingleRefGenerator uncachedRefSupplier) {
            lastProvidedValue = -1L;
            remainingCachedIds = 0;
            this.sqlCommandForNextValue = sqlCommandForNextValue;
            this.cacheSize = cacheSize;
            this.uncachedRefSupplier = uncachedRefSupplier;
        }

        public synchronized long getAsLong() {
            if (remainingCachedIds > 0) {
                --remainingCachedIds;
                ++lastProvidedValue;
            } else {
                final long nextval = uncachedRefSupplier.getNextSequence(sqlCommandForNextValue);
                // store data for the next bunch of results
                lastProvidedValue = nextval * cacheSize;
                remainingCachedIds = cacheSize - 1;
            }
            return lastProvidedValue;
        }
    }
}
