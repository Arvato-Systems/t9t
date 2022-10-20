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

import com.arvatosystems.t9t.kafka.service.IKafkaTopicWriter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.util.ExceptionUtil;

public class KafkaTopicWriter implements IKafkaTopicWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTopicWriter.class);

    private final String kafkaTopic;              // determined via config
    private final KafkaProducer<String, byte[]> producer;
    private final List<PartitionInfo> partitions;
    private final int numberOfPartitions;

    public KafkaTopicWriter(final String bootstrapServers, final String topic, final Properties props) {
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,      bootstrapServers);
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class.getName());     // pass the instance instead (no reflection)
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());  // pass the instance instead (no reflection)
        kafkaTopic = topic;
        producer = new KafkaProducer<>(props, new StringSerializer(), new ByteArraySerializer());
        partitions = producer.partitionsFor(kafkaTopic);
        numberOfPartitions = partitions.size();

        LOGGER.info("Created writer for kafka topic {}, which has {} partitions", topic, numberOfPartitions);
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    @Override
    public void write(final BonaPortable data, final int partitionIn, final String recordKey) {
        final CompactByteArrayComposer cbac = new CompactByteArrayComposer(false);
        cbac.reset();
        cbac.writeRecord(data);
        final byte[] dataToWrite = cbac.getBytes();
        cbac.close();
        write(dataToWrite, partitionIn, recordKey);
    }

    @Override
    public void write(final byte[] dataToWrite, final int partitionIn, final String recordKey) {
        final int partition = (partitionIn & 0x7fffffff) % numberOfPartitions;
        producer.send(new ProducerRecord<String, byte[]>(kafkaTopic, Integer.valueOf(partition), recordKey, dataToWrite), (meta, e) -> {
            if (e != null) {
                LOGGER.error("Could not send record for partition key {} in topic {}: {}: {}", recordKey, kafkaTopic,
                        e.getClass().getSimpleName(), ExceptionUtil.causeChain(e));
            } else {
                LOGGER.debug("Sent record for partition key {} in topic {} (made it into partition {} at offset {}, {} bytes)", recordKey, kafkaTopic,
                        meta.partition(), meta.offset(), dataToWrite.length);
            }
        });
    }

    @Override
    public void close() {
        producer.flush();
        producer.close();
    }
}
