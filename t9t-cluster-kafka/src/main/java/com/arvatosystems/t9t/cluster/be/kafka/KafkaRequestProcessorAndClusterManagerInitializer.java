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
package com.arvatosystems.t9t.cluster.be.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;
import com.arvatosystems.t9t.kafka.service.IKafkaTopicReader;
import com.arvatosystems.t9t.kafka.service.impl.KafkaTopicReader;
import com.arvatosystems.t9t.metrics.IMetricsProvider;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupShutdown;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;

@Startup(95007)
@Singleton
public class KafkaRequestProcessorAndClusterManagerInitializer implements StartupShutdown {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRequestProcessorAndClusterManagerInitializer.class);
    public static final String KAFKA_CLUSTER_MANAGER_THREAD_NAME = "t9t-KafkaClusterManager";
    public static final String KAFKA_INPUT_SESSIONS_GROUP_ID = "clusterManager";
    public static final int DEFAULT_COMMIT_ASYNC_EVERY_N = 10; // send an async commit every 10th executed command
    public static final long DEFAULT_TIMEOUT = 9999L; // any request must terminate within 10 seconds

    private static final int DEFAULT_TIMEOUT_THREADPOOL_SHUTDOWN_MS = 10_000;

    @IsLogicallyFinal // set by open() method
    private ExecutorService executorKafkaIO;
    @IsLogicallyFinal // set by open() method
    private Future<Boolean> writerResult;
    @IsLogicallyFinal // set by open() method
    private KafkaConfiguration defaults;
    @IsLogicallyFinal // set by open() method
    private IKafkaTopicReader consumer;

    private final IMetricsProvider metricsProvider = Jdp.getOptional(IMetricsProvider.class);

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private long shutdownThreadpoolIntervalInMs;

    private static KafkaClusterRebalancer rebalancer = null;
    private static int numberOfPartitons = 1;

    public static KafkaClusterRebalancer getRebalancer() {
        return rebalancer;
    }

    public static int getNumberOfPartitons() {
        return numberOfPartitons;
    }

    public static boolean processOnThisNode(final String tenantId, final int hash) {
        if (numberOfPartitons <= 0) {
            return true; // no kafka available?
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

        final Map<String, Object> props = new HashMap<>(16);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, defaults.getMaxPollRecords());

        this.shutdownThreadpoolIntervalInMs = T9tUtil.nvl(defaults.getShutdownThreadpoolInterval(), DEFAULT_TIMEOUT_THREADPOOL_SHUTDOWN_MS).longValue();

        try {
            rebalancer = new KafkaClusterRebalancer(KAFKA_INPUT_SESSIONS_GROUP_ID, false);
            consumer = new KafkaTopicReader(defaults.getDefaultBootstrapServers(), topic, groupId, props, rebalancer);
            numberOfPartitons = consumer.getNumberOfPartitions();
        } catch (final Exception exc) {
            final String details = exc.getClass().getSimpleName() + ": " + exc.getMessage();
            LOGGER.error("Cannot create kafka listener: {}", details);
            throw new T9tException(T9tException.KAFKA_LISTENER_ERROR, details);
        }

        // select strategy
        final Callable<Boolean> processingStrategy;
        if (T9tUtil.isTrue(defaults.getClusterManagerOrdering())) {
            processingStrategy = new KafkaSimplePartitionOrderedRequestProcessor(defaults, consumer, shuttingDown);
        } else {
            processingStrategy = new KafkaRequestProcessor(defaults, consumer, shuttingDown);
        }

        // add metrics if enabled
        if (this.metricsProvider != null) {
            LOGGER.info("Adding metrics provider for kafka consumer");
            consumer.registerMetrics((kafkaConsumer) -> this.metricsProvider.addMeter(new KafkaClientMetrics(kafkaConsumer)));
            if (processingStrategy instanceof KafkaSimplePartitionOrderedRequestProcessor proc) {
                final KafkaClusterPartitionMetrics customMetrics = new KafkaClusterPartitionMetrics(proc.getPartitionStatusTable(), consumer.getKafkaTopic());
                this.metricsProvider.addMeter(customMetrics);
            }
        } else {
            LOGGER.warn("Metrics provider not available - cannot add meter for kafka");
        }

        rebalancer.setProcessingStrategy(processingStrategy);
        executorKafkaIO = Executors.newSingleThreadExecutor(call -> new Thread(call, KAFKA_CLUSTER_MANAGER_THREAD_NAME));
        writerResult = executorKafkaIO.submit(processingStrategy);
    }

    @Override
    public void onShutdown() {
        if (defaults == null) {
            // there was no configuration - nothing has been initialized, not possible to shut down anything either
            return;
        }
        shuttingDown.set(true);
        if (consumer != null) {
            // setting shuttingDown will als break polling loop, but if is currently WITHIN the poll methods, we need to call wakeUp()
            // close() is performed in processor
            consumer.wakeUp();
        }
        if (executorKafkaIO != null) {
            LOGGER.info("Shutting down {}...", this.getClass().getSimpleName());
            executorKafkaIO.shutdown();
            try {
                // wait 5 seconds longer than the underlying threadpool
                writerResult.get(shutdownThreadpoolIntervalInMs + 5000, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException | ExecutionException | TimeoutException exc) {
                LOGGER.warn(KAFKA_CLUSTER_MANAGER_THREAD_NAME + " thread failed to terminate properly: {}: {}", exc.getClass().getSimpleName(),
                        exc.getMessage());
            }
        }
    }
}
