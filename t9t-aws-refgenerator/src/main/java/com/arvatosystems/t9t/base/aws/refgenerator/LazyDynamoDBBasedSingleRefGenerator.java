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
package com.arvatosystems.t9t.base.aws.refgenerator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.ISingleRefGenerator;
import com.arvatosystems.t9t.cfg.be.AWSConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;
import jakarta.annotation.Nonnull;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughputDescription;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.utils.ImmutableMap;

@Singleton
@Named("lazyDynamoDB")
public class LazyDynamoDBBasedSingleRefGenerator implements ISingleRefGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LazyDynamoDBBasedSingleRefGenerator.class);

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

    private final DynamoDbClient client;

    private static final String TABLE_NAME = "counters";
    private static final String COLUMN_NAME_KEY = "rtti";
    private static final String COLUMN_NAME_DATA = "counter";

    public LazyDynamoDBBasedSingleRefGenerator() {
        client = initDynamoDBAccess();
        describeTable(TABLE_NAME);
    }

//        counterMap = ImmutableMap.of(COLUMN_NAME_DATA, AttributeValue.builder().n("1").build());
//
//        final AttributeValue fulfilled = new AttributeValue().withBOOL(true);
//
//        final UpdateItemRequest updateItemRequest = new UpdateItemRequest()
//             .withTableName(tableName)
//             .withKey(keyAttributes)
//             .withUpdateExpression("SET count = count + :increment")
//             .withExpressionAttributeValues(ImmutableMap.of(":increment", 1));
//
//        final UpdateItemResponse result = dynamoClient.updateItem(updateItemRequest);
//
//    protected AttributeValue makeKey(Integer rtti) {
//        return AttributeValue.builder().s(rtti.toString()).build();
//    }
//
//    protected Map<String,AttributeValue> makeKeyMap(Integer rtti) {
//        return Collections.singletonMap(COLUMN_NAME_KEY, makeKey(rtti));
//    }

    @Override
    public long getNextSequence(@Nonnull final String selectStatement) {
        final String counterKey = selectStatement; // TODO: improve me
        final Map<String, AttributeValue> keyMap = ImmutableMap.of(COLUMN_NAME_KEY, AttributeValue.builder().s(counterKey).build());

        // no data in cache, must obtain a new database sequence number
        // use the current thread's EntityManager to request a new value
        // from the database, because then we do not need to synchronize
        // different threads requesting different values at the same time.
        final Map<String, AttributeValueUpdate> updates = Collections.singletonMap(COLUMN_NAME_DATA, AttributeValueUpdate.builder()
                .value(AttributeValue.builder().n("1").build())
                .action(AttributeAction.ADD)  // https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_UpdateItem.html
                .build());

        final UpdateItemRequest putReq = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(keyMap)
                .attributeUpdates(updates)
                .returnValues(ReturnValue.UPDATED_NEW)
                .build();

        try {
            final UpdateItemResponse result = client.updateItem(putReq);
            final Map<String, AttributeValue> attributes = result.attributes();
            if (attributes == null || attributes.isEmpty()) {
                LOGGER.error("Did not receive any attributes");
                throw new T9tException(T9tException.DYNAMODB_EXCEPTION, "No attributes returned");
            }
            AttributeValue nextCounter = attributes.get(COLUMN_NAME_DATA);
            if (nextCounter == null) {
                LOGGER.error("Did not receive attribute vaue for counter");
                throw new T9tException(T9tException.DYNAMODB_EXCEPTION, "Did not receive attribute vaue for counter");
            }
            LOGGER.debug("Obtained next counter value for {} with value <{}>", counterKey, nextCounter.n());
            return Long.parseLong(nextCounter.n());
        } catch (DynamoDbException e) {
            LOGGER.error("SEQUENCE exception on key {}: {}", counterKey, ExceptionUtil.causeChain(e));
            throw new T9tException(T9tException.DYNAMODB_EXCEPTION, "Counters table(rtti = " + counterKey + "): " + ExceptionUtil.causeChain(e));
        }
    }

    @Override
    public boolean needSelectStatement() {
        return false;  // we use the parameter just as key field
    }
}
