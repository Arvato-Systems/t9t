/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IRefGenerator;
import com.arvatosystems.t9t.cfg.be.DatabaseBrandType;
import com.arvatosystems.t9t.cfg.be.KeyPrefetchConfiguration;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

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
@Named("lazySequenceJPA")  // only acquires an ID once the first request has been seen
@Singleton
public class LazyJpaSequenceBasedRefGenerator implements IRefGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LazyJpaSequenceBasedRefGenerator.class);

    private static final int DEFAULT_CACHE_SIZE          = 500; // how many sequences we generate per single DB-sequence fetch for scaled keys
    private static final int DEFAULT_CACHE_SIZE_UNSCALED =  10; // how many sequences we generate per single DB-sequence fetch for unscaled keys
    private static final int NUM_SEQUENCES               = 100; // how many sequences we use to obtain the IDs
    private static final int NUM_SEQUENCES_UNSCALED      =  10; // how many sequences we use to obtain unscaled IDs
    private final LazyJpaSequenceBasedSingleRefGenerator[] generatorTab = new LazyJpaSequenceBasedSingleRefGenerator[NUM_SEQUENCES];
    private final LazyJpaSequenceBasedSingleRefGenerator[] generatorTab50xx = new LazyJpaSequenceBasedSingleRefGenerator[NUM_SEQUENCES_UNSCALED];
    private final LazyJpaSequenceBasedSingleRefGenerator[] generatorTab60xx = new LazyJpaSequenceBasedSingleRefGenerator[NUM_SEQUENCES_UNSCALED];
    private final LazyJpaSequenceBasedSingleRefGenerator[] generatorTab70xx = new LazyJpaSequenceBasedSingleRefGenerator[NUM_SEQUENCES_UNSCALED];
    private final long scaledOffsetForLocation;

    // @Inject
    private final T9tServerConfiguration configuration = Jdp.getRequired(T9tServerConfiguration.class);

    public LazyJpaSequenceBasedRefGenerator() {
        final DatabaseBrandType dialect = configuration.getDatabaseConfiguration().getDatabaseBrand();
        LOGGER.info("Creating object references by SQL SEQUENCES via JPA for database {}", dialect.name());
        final KeyPrefetchConfiguration keyConfig = configuration.getKeyPrefetchConfiguration();
        final int cacheSize;
        final int cacheSizeUnscaled;

        if (keyConfig != null) {
            scaledOffsetForLocation = (long) keyConfig.getLocationOffset() * OFFSET_BACKUP_LOCATION;
            cacheSize = keyConfig.getCacheSize() == null ? DEFAULT_CACHE_SIZE : keyConfig.getCacheSize().intValue();
            cacheSizeUnscaled = keyConfig.getCacheSizeUnscaled() == null ? DEFAULT_CACHE_SIZE_UNSCALED : keyConfig.getCacheSizeUnscaled().intValue();
        } else {
            scaledOffsetForLocation = 0;
            cacheSize = DEFAULT_CACHE_SIZE;
            cacheSizeUnscaled = DEFAULT_CACHE_SIZE_UNSCALED;
        }
        for (int i = 0; i < NUM_SEQUENCES; ++i) {
            generatorTab[i] = new LazyJpaSequenceBasedSingleRefGenerator(i, dialect, cacheSize);
        }
        for (int i = 0; i < NUM_SEQUENCES_UNSCALED; ++i) {
            generatorTab50xx[i] = new LazyJpaSequenceBasedSingleRefGenerator(5000 + i, dialect, cacheSizeUnscaled);
            generatorTab60xx[i] = new LazyJpaSequenceBasedSingleRefGenerator(6000 + i, dialect, cacheSizeUnscaled);
            generatorTab70xx[i] = new LazyJpaSequenceBasedSingleRefGenerator(7000 + i, dialect, cacheSizeUnscaled);
        }
    }

    @Override
    public long generateRef(final int rttiOffset) {
        if ((rttiOffset < 0) || (rttiOffset >= OFFSET_BACKUP_LOCATION)) {
            throw new InvalidParameterException("Bad rtti offset: " + rttiOffset);
        }
        return (generatorTab[rttiOffset % NUM_SEQUENCES].getnextId() * KEY_FACTOR) + scaledOffsetForLocation + rttiOffset;
    }

    @Override
    public long generateUnscaledRef(final int rttiOffset) {
        LazyJpaSequenceBasedSingleRefGenerator g = null;
        if ((rttiOffset >= 5000) && (rttiOffset < (5000 + NUM_SEQUENCES_UNSCALED))) {
            g = generatorTab50xx[rttiOffset - 5000];
        } else if ((rttiOffset >= 6000) && (rttiOffset < (6000 + NUM_SEQUENCES_UNSCALED))) {
            g = generatorTab60xx[rttiOffset - 6000];
        } else if ((rttiOffset >= 7000) && (rttiOffset < (7000 + NUM_SEQUENCES_UNSCALED))) {
            g = generatorTab70xx[rttiOffset - 7000];
        } else {
            throw new InvalidParameterException("Bad rtti offset: " + rttiOffset);
        }
        return (g.getnextId() * 2L) + (scaledOffsetForLocation > 0 ? 1 : 0);
    }
}
