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
package com.arvatosystems.t9t.cluster.be.kafka;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;
import com.arvatosystems.t9t.kafka.service.IKafkaTopicReader;
import com.arvatosystems.t9t.kafka.service.impl.KafkaRebalancer;
import com.arvatosystems.t9t.kafka.service.impl.KafkaTopicReader;

import de.jpaw.dp.Singleton;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupShutdown;

@Startup(95007)
@Singleton
public class KafkaRequestProcessorAndClusterManagerInitializer implements StartupShutdown {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRequestProcessorAndClusterManagerInitializer.class);
    public static final String KAFKA_CLUSTER_MANAGER_THREAD_NAME = "t9t-KafkaClusterManager";
    public static final String KAFKA_INPUT_SESSIONS_GROUP_ID = "clusterManager";
    public static final int DEFAULT_COMMIT_ASYNC_EVERY_N = 10;  // send an async commit every 10th executed command
    public static final long DEFAULT_TIMEOUT = 9999L;  // any request must terminate within 10 seconds

    @IsLogicallyFinal  // set by open() method
    private ExecutorService executorKafkaIO;
    @IsLogicallyFinal  // set by open() method
    private Future<Boolean> writerResult;
    @IsLogicallyFinal  // set by open() method
    private KafkaConfiguration defaults;
    @IsLogicallyFinal  // set by open() method
    private IKafkaTopicReader consumer;

    private final AtomicBoolean pleaseStop = new AtomicBoolean(false);

    private static KafkaRebalancer rebalancer = null;
    private static int numberOfPartitons = 1;

    public static KafkaRebalancer getRebalancer() {
        return rebalancer;
    }

    public static int getNumberOfPartitons() {
        return numberOfPartitons;
    }
    public static boolean processOnThisNode(final String tenantId, final int hash) {
        if (numberOfPartitons <= 0) {
            return true;  // no kafka available?
        }
        final Integer partition = Integer.valueOf((hash & 0x7fffffff) % numberOfPartitons);
        return rebalancer.getCurrentPartitions().contains(partition);
    }

    @Override
    public void onStartup() {
        final T9tServerConfiguration serverConfig = ConfigProvider.getConfiguration();
        defaults = serverConfig.getKafkaConfiguration();
        if (defaults == null || defaults.getDefaultBootstrapServers() == null || defaults.getClusterManagerApiKey() == null) {
            LOGGER.info("No Kafka input initialization - missing or incomplete Kafka configuration in config XML: bootstrap servers or API key missing");
            return;
        }
        final String topic = T9tUtil.nvl(defaults.getClusterManagerTopicName(), T9tConstants.DEFAULT_KAFKA_TOPIC_SINGLE_TENANT_REQUESTS);
        final String groupId = T9tUtil.nvl(defaults.getClusterManagerGroupId(), T9tConstants.DEFAULT_KAFKA_REQUESTS_GROUP_ID);
        try {
            rebalancer = new KafkaRebalancer(KAFKA_INPUT_SESSIONS_GROUP_ID, false);
            consumer = new KafkaTopicReader(defaults.getDefaultBootstrapServers(), topic, groupId, null, rebalancer);
            numberOfPartitons = consumer.getNumberOfPartitions();
        } catch (Exception e) {
            LOGGER.error("Cannot create kafka listener: {}: {}", e.getClass().getSimpleName(), e.getMessage());
        }

        executorKafkaIO = Executors.newSingleThreadExecutor(call -> new Thread(call, KAFKA_CLUSTER_MANAGER_THREAD_NAME));
        writerResult = executorKafkaIO.submit(new KafkaRequestProcessor(defaults, consumer, pleaseStop));
    }

    @Override
    public void onShutdown() {
        if (defaults == null) {
            // there was no configuration - nothing has been initialized, not possible to shut down anything either
            return;
        }
        pleaseStop.set(true);
        if (consumer != null) {
            consumer.close();   // tell kafka to abort any pending poll() and unsubscribe from topics
        }
        if (executorKafkaIO != null) {
            executorKafkaIO.shutdown();
            try {
                writerResult.get(1000L, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.warn(KAFKA_CLUSTER_MANAGER_THREAD_NAME + " thread failed to terminate properly: {}: {}",
                  e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
