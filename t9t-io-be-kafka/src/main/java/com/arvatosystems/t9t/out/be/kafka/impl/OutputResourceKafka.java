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
package com.arvatosystems.t9t.out.be.kafka.impl;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.out.services.IOutputResource;

import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import de.jpaw.util.ExceptionUtil;

@Dependent
@Named("KAFKA")
public class OutputResourceKafka implements IOutputResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutputResourceKafka.class);

    protected static Producer<String, byte[]> createKafkaProducer(DataSinkDTO config, Long sinkRef) {
        final Properties props = new Properties();
        final KafkaConfiguration defaults = ConfigProvider.getConfiguration().getKafkaConfiguration();
        final String defaultBootstrapServers = defaults == null ? null : defaults.getDefaultBootstrapServers();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, JsonUtil.getZString(config.getZ(), ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, defaultBootstrapServers));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, config.getDataSinkId() + ":" + sinkRef.toString());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        //props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());
        return new KafkaProducer<>(props);
    }

    protected String topic = "";
    protected int numberOfPartitions = 1;
    protected Producer<String, byte[]> producer;
    protected DataSinkDTO cfg;

    @Override
    public void open(DataSinkDTO config, OutputSessionParameters params, Long sinkRef, String targetName, MediaTypeDescriptor mediaType, Charset encoding) {
        cfg = config;
        topic = cfg.getFileOrQueueNamePattern();
        producer = createKafkaProducer(config, sinkRef);
        final List<PartitionInfo> partitions = producer.partitionsFor(topic);
        numberOfPartitions = partitions.size();
        LOGGER.debug("Topic {} has {} partitions: {}", topic, numberOfPartitions);
    }

    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(final String partitionKey, final String recordKey, byte[] buffer, int offset, int len, boolean isDataRecord) {
        final byte [] data = (offset == 0 && (len < 0 || len == buffer.length)) ? buffer : Arrays.copyOfRange(buffer, offset, offset+len-1);
        final int partition = (partitionKey.hashCode() & 0x7fffffff) % numberOfPartitions;
        producer.send(new ProducerRecord<String, byte[]>(topic, Integer.valueOf(partition), recordKey, data), (meta, e) -> {
            if (e != null) {
                LOGGER.error("Could not send record {} for partition {} in topic {}: {}: {}", recordKey, partitionKey, topic,
                        e.getClass().getSimpleName(), ExceptionUtil.causeChain(e));
            } else {
                LOGGER.debug("Sent record {} for partition {} in topic {} (made it into partition {} at offset {})", recordKey, partitionKey, topic,
                        meta.partition(), meta.offset());
            }
        });
    }

    @Override
    public void write(String partitionKey, String recordKey, String data) {
        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        write(partitionKey, recordKey, bytes, 0, bytes.length, true);
    }

    @Override
    public void close() {
        producer.flush();
        producer.close();
    }
}
