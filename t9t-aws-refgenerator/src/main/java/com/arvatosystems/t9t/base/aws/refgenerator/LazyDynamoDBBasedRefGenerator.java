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
package com.arvatosystems.t9t.base.aws.refgenerator;

import java.security.InvalidParameterException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IRefGenerator;
import com.arvatosystems.t9t.cfg.be.AWSConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.KeyPrefetchConfiguration;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughputDescription;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

/**
 */
@Named("lazyDynamoDB")  // only acquires an ID once the first request has been seen
@Singleton
public class LazyDynamoDBBasedRefGenerator implements IRefGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LazyDynamoDBBasedRefGenerator.class);

    private static final int DEFAULT_CACHE_SIZE          = 1000; // how many sequences we generate per single DB-sequence fetch for scaled keys
    private static final int DEFAULT_CACHE_SIZE_UNSCALED =  100; // how many sequences we generate per single DB-sequence fetch for unscaled keys
    private static final int NUM_SEQUENCES               =  100; // how many sequences we use to obtain the IDs
    private static final int NUM_SEQUENCES_UNSCALED      =   10; // how many sequences we use to obtain unscaled IDs
    private final LazyDynamoDBBasedSingleRefGenerator[] generatorTab = new LazyDynamoDBBasedSingleRefGenerator[NUM_SEQUENCES];
    private final LazyDynamoDBBasedSingleRefGenerator[] generatorTab50xx = new LazyDynamoDBBasedSingleRefGenerator[NUM_SEQUENCES_UNSCALED];
    private final LazyDynamoDBBasedSingleRefGenerator[] generatorTab60xx = new LazyDynamoDBBasedSingleRefGenerator[NUM_SEQUENCES_UNSCALED];
    private final LazyDynamoDBBasedSingleRefGenerator[] generatorTab70xx = new LazyDynamoDBBasedSingleRefGenerator[NUM_SEQUENCES_UNSCALED];
    private final long scaledOffsetForLocation;
    protected final DynamoDbClient client;

    private static final String TABLE_NAME = "counters";

    // @Inject
    private final T9tServerConfiguration configuration = Jdp.getRequired(T9tServerConfiguration.class);


    private DynamoDbClient initDynamoDBAccess() {
        final AWSConfiguration awsCfg = ConfigProvider.getConfiguration().getAwsConfiguration();
        Region region = Region.EU_CENTRAL_1;

        if (awsCfg != null && awsCfg.getRegion() != null) {
            LOGGER.info("Setting region to {} by configuration", awsCfg.getRegion());
            region = Region.of(awsCfg.getRegion());
        } else {
            LOGGER.info("Connection to DynamoDB using default region {}", region);
        }
        final DynamoDbClient xclient = DynamoDbClient.builder()
                .region(region)
                .build();
        LOGGER.info("DynamoDBPersistenceAccess instantiated, for region {}", region);
        return xclient;
    }

    private void describeTable(String tableName) {

        try {
            final DescribeTableRequest request = DescribeTableRequest.builder()
                .tableName(tableName)
                .build();

            final TableDescription tableInfo = client.describeTable(request).table();

            if (tableInfo != null) {
                LOGGER.info("Table {} has ARN {}, status {}", tableInfo.tableName(), tableInfo.tableArn(), tableInfo.tableStatus());
                LOGGER.info("    item count {}, {}", tableInfo.itemCount(), tableInfo.tableSizeBytes());

                final ProvisionedThroughputDescription throughputInfo = tableInfo.provisionedThroughput();
                LOGGER.info("    Read Capacity {}, write capacity {}", throughputInfo.readCapacityUnits(), throughputInfo.writeCapacityUnits());

                final List<AttributeDefinition> attributes = tableInfo.attributeDefinitions();
                for (AttributeDefinition a : attributes) {
                    LOGGER.info("    Attribute {}, type {}", a.attributeName(), a.attributeType());
                }
            }
        } catch (DynamoDbException e) {
            LOGGER.error("Cannot find table {}: {}", tableName, ExceptionUtil.causeChain(e));
        }
    }


    public LazyDynamoDBBasedRefGenerator() {
        LOGGER.info("Creating object references by DynamoDB AtomicCounters");
        client = initDynamoDBAccess();

        describeTable(TABLE_NAME);

        final KeyPrefetchConfiguration keyConfig = configuration.getKeyPrefetchConfiguration();
        final int cacheSize;
        final int cacheSizeUnscaled;

        if (keyConfig != null) {
            scaledOffsetForLocation = (long) keyConfig.getLocationOffset() * LazyDynamoDBBasedRefGenerator.OFFSET_BACKUP_LOCATION;
            cacheSize = keyConfig.getCacheSize() == null ? DEFAULT_CACHE_SIZE : keyConfig.getCacheSize().intValue();
            cacheSizeUnscaled = keyConfig.getCacheSizeUnscaled() == null ? DEFAULT_CACHE_SIZE_UNSCALED : keyConfig.getCacheSizeUnscaled().intValue();
        } else {
            scaledOffsetForLocation = 0;
            cacheSize = DEFAULT_CACHE_SIZE;
            cacheSizeUnscaled = DEFAULT_CACHE_SIZE_UNSCALED;
        }
        for (int i = 0; i < NUM_SEQUENCES; ++i) {
            generatorTab[i] = new LazyDynamoDBBasedSingleRefGenerator(i, client, cacheSize);
        }
        for (int i = 0; i < NUM_SEQUENCES_UNSCALED; ++i) {
            generatorTab50xx[i] = new LazyDynamoDBBasedSingleRefGenerator(5000 + i, client, cacheSizeUnscaled);
            generatorTab60xx[i] = new LazyDynamoDBBasedSingleRefGenerator(6000 + i, client, cacheSizeUnscaled);
            generatorTab70xx[i] = new LazyDynamoDBBasedSingleRefGenerator(7000 + i, client, cacheSizeUnscaled);
        }
    }

    @Override
    public long generateRef(final int rttiOffset) {
        if ((rttiOffset < 0) || (rttiOffset >= OFFSET_BACKUP_LOCATION)) {
            throw new InvalidParameterException("Bad rtti offset: " + rttiOffset);
        }
        return (generatorTab[rttiOffset % NUM_SEQUENCES].getnextId() * LazyDynamoDBBasedRefGenerator.KEY_FACTOR) + scaledOffsetForLocation + rttiOffset;
    }

    @Override
    public long generateUnscaledRef(final int rttiOffset) {
        LazyDynamoDBBasedSingleRefGenerator g = null;
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
