package com.arvatosystems.t9t.kafka.service.impl;

import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IKafkaRequestTransmitter;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;

import de.jpaw.api.ConfigurationReader;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ConfigurationReaderFactory;
import de.jpaw.util.ExceptionUtil;

@Singleton
public class KafkaRequestTransmitter implements IKafkaRequestTransmitter {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRequestTransmitter.class);

    public static final ConfigurationReader CONFIG_READER = ConfigurationReaderFactory.getConfigReaderForName("t9t.kafka", null);

    private static final String DEFAULT_KAFKA_REQUEST_TOPIC = "t9tRequestTopic";
    private static final String KAFKA_DEFAULT_BOOTSTRAP_KEY = "t9t.kafka.bootstrap.servers";
    private static final String KAFKA_DEFAULT_TOPIC_KEY     = "t9t.kafka.request.topic";
    private final String kafkaBootstrapServers;   // determined via config
    private final String kafkaTopic;       // determined via config
    private final KafkaProducer<String, byte[]> producer;
    private final List<PartitionInfo> partitions;
    private final int numberOfPartitions;

    public KafkaRequestTransmitter() {
        kafkaBootstrapServers = CONFIG_READER.getProperty(KAFKA_DEFAULT_BOOTSTRAP_KEY, null);
        kafkaTopic = CONFIG_READER.getProperty(KAFKA_DEFAULT_TOPIC_KEY, DEFAULT_KAFKA_REQUEST_TOPIC);
        if (kafkaBootstrapServers == null) {
            throw new T9tException(T9tException.MISSING_KAFKA_BOOTSTRAP);
        }
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        // props.put(ProducerConfig.CLIENT_ID_CONFIG, config.getDataSinkId());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.LINGER_MS_CONFIG, 100);
        props.put(ProducerConfig.RETRIES_CONFIG, 2);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 8000);  // approx 5 orders
//        //props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());
//        if (config.getZ() != null) {
//            final Object extraKafkaConfig = config.getZ().get("kafka");
//            if (extraKafkaConfig instanceof Map) {
//                final Map<?, ?> extraKafkaConfigMap = (Map<?, ?>)extraKafkaConfig;
//                LOGGER.info("Found {} additional Producer configuration properties for kafka in data sink {}",
//                  extraKafkaConfigMap.size(), config.getDataSinkId());
//                for (Map.Entry<?, ?> entry: extraKafkaConfigMap.entrySet()) {
//                    props.put(entry.getKey(), entry.getValue());
//                }
//            }
//        }
        producer = new KafkaProducer<>(props);
        partitions = producer.partitionsFor(kafkaTopic);
        numberOfPartitions = partitions.size();
    }

    @Override
    public void write(final RequestParameters request, final String partitionKey) {
        final CompactByteArrayComposer cbac = new CompactByteArrayComposer(false);
        cbac.reset();
        cbac.writeRecord(request);
        final byte[] dataToWrite = cbac.getBytes();
        cbac.close();

        final int partition = (partitionKey.hashCode() & 0x7fffffff) % numberOfPartitions;
        producer.send(new ProducerRecord<String, byte[]>(kafkaTopic, Integer.valueOf(partition), partitionKey, dataToWrite), (meta, e) -> {
            if (e != null) {
                LOGGER.error("Could not send record for partition key {} in topic {}: {}: {}", partitionKey, kafkaTopic,
                        e.getClass().getSimpleName(), ExceptionUtil.causeChain(e));
            } else {
                LOGGER.debug("Sent record for partition key {} in topic {} (made it into partition {} at offset {}, {} bytes)", partitionKey, kafkaTopic,
                        meta.partition(), meta.offset(), dataToWrite.length);
            }
        });
    }
}
