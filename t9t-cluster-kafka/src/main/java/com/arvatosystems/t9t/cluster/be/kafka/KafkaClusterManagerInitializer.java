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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;

import de.jpaw.dp.Singleton;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupShutdown;

@Startup(94001)
@Singleton
public class KafkaClusterManagerInitializer implements StartupShutdown {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTenantAgnosticClusterManager.class);
    public static final String KAFKA_CLUSTER_GROUP_ID = "ClusterManager";
    public static final String KAFKA_CLUSTER_TOPIC_NAME = "t9t.cluster";

    private static final Collection<TopicPartition> MY_SHARDS = new HashSet<>(100);
    protected static Collection<Integer> myIndexes = Collections.singletonList(Integer.valueOf(0));  // single node
    protected static int totalNumberOfPartitons = 1;  // single node

    @IsLogicallyFinal  // set by open() method
    private ExecutorService executorKafkaIO;
    @IsLogicallyFinal  // set by open() method
    private Future<Boolean> writerResult;
    @IsLogicallyFinal  // set by open() method
    private KafkaConfiguration defaults;
    @IsLogicallyFinal  // set by open() method
    private Consumer<String, byte[]> consumer;
    @IsLogicallyFinal  // set by open() method
    private String topicName;
    @IsLogicallyFinal
    private volatile boolean pleaseStop = false;

    private static String nvl(final String a, final String b) {
        if (a == null || a.isEmpty()) {
            return b;
        }
        return a;
    }

    private static void dumpPartitions(final String intro, final boolean recomputePartitions) {
        LOGGER.info("{} number of shards is {}", intro, MY_SHARDS.size());
        LOGGER.info("Partitions are: {}", MY_SHARDS.stream().map(tp -> Integer.toString(tp.partition())).collect(Collectors.joining(", ")));
        if (recomputePartitions) {
            final List<Integer> shards = new ArrayList<>(MY_SHARDS.size());
            for (final TopicPartition tp: MY_SHARDS) {
                shards.add(tp.partition());
            }
            myIndexes = Collections.unmodifiableCollection(shards);
        }
    }

    private static class MyRebalancer implements ConsumerRebalanceListener {
        @Override
        public void onPartitionsRevoked(final Collection<TopicPartition> partitions) {
            LOGGER.info("Rebalance! {} partitions revoked", partitions.size());
            dumpPartitions("BEFORE", false);
            MY_SHARDS.removeAll(partitions);
            dumpPartitions("AFTER", true);
        }

        @Override
        public void onPartitionsAssigned(final Collection<TopicPartition> partitions) {
            LOGGER.info("Rebalance! {} partitions assigned", partitions.size());
            dumpPartitions("BEFORE", false);
            MY_SHARDS.addAll(partitions);
            dumpPartitions("AFTER", true);
        }
    }

    private class WriterThread implements Callable<Boolean> {

        @Override
        public Boolean call() {
            consumer = createKafkaConsumer(defaults);
            totalNumberOfPartitons = consumer.partitionsFor(topicName).size();
            consumer.subscribe(Collections.singletonList(topicName), new MyRebalancer());
            final List<PartitionInfo> partitions = consumer.partitionsFor(topicName);
            LOGGER.info("Initially {} partitions have been assigned", partitions.size());
            for (final PartitionInfo pi: partitions) {
                MY_SHARDS.add(new TopicPartition(pi.topic(), pi.partition()));
            }
            dumpPartitions("INITIAL", true);

            while (!pleaseStop) {
                //final ConsumerRecords<String, byte []> records =
                consumer.poll(Duration.ofMillis(100));
            }
            LOGGER.info("Termination requested, waiting for all pending commands...");
            consumer.commitAsync();
            consumer.close(Duration.ofMillis(2000L));
            return Boolean.TRUE;
        }
    }

    protected static Consumer<String, byte[]> createKafkaConsumer(final KafkaConfiguration defaults) {
        final Map<String, Object> props = new HashMap<>(10);
        final String defaultBootstrapServers = defaults == null ? null : defaults.getDefaultBootstrapServers();
        final String clusterManagerGroupId = defaults == null ? null : defaults.getClusterManagerGroupId();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, defaultBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, nvl(clusterManagerGroupId, KAFKA_CLUSTER_GROUP_ID));
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);  // or "false" as found in examples?
        return new KafkaConsumer<>(props, new StringDeserializer(), new ByteArrayDeserializer());
    }

    @Override
    public void onStartup() {
        defaults = ConfigProvider.getConfiguration().getKafkaConfiguration();
        if (defaults == null || defaults.getDefaultBootstrapServers() == null) {
            LOGGER.info("No Kafka input initialization - missing or incomplete Kafka configuration in config XML: bootstrap servers or API key missing");
            return;
        }
        topicName = nvl(defaults.getClusterManagerTopicName(), KAFKA_CLUSTER_TOPIC_NAME);
        LOGGER.info("Setting up Kafka consumer for cluster node management, using topic {}", topicName);
        executorKafkaIO = Executors.newSingleThreadExecutor(call -> new Thread(call, "t9t-KafkaCluster"));
        writerResult = executorKafkaIO.submit(new WriterThread());
    }

    @Override
    public void onShutdown() {
        if (defaults == null) {
            // there was no configuration - nothing has been initialized, not possible to shut down anything either
            return;
        }
        pleaseStop = true;
        consumer.wakeup();   // tell kafka to abort any pending poll()
        executorKafkaIO.shutdown();
    }
}
