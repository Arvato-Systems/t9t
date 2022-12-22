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
package com.arvatosystems.t9t.out.be.kafka.impl;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.kafka.service.impl.KafkaTopicWriter;
import com.arvatosystems.t9t.out.services.IOutputResource;

import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

@Dependent
@Named("KAFKA")
public class OutputResourceKafka implements IOutputResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutputResourceKafka.class);

    protected static KafkaTopicWriter createKafkaProducer(final DataSinkDTO config) {
        final Properties props = new Properties();
        final KafkaConfiguration defaults = ConfigProvider.getConfiguration().getKafkaConfiguration();
        if (defaults == null) {
            throw new T9tException(T9tIOException.MISSING_KAFKA_CONFIGURAION, "No kafkaConfiguration entry in config.xml");
        }

        final String defaultBootstrapServers = defaults == null ? null : defaults.getDefaultBootstrapServers();
        final String bootstrapServers = config.getBootstrapServers() != null ? config.getBootstrapServers() : defaultBootstrapServers;
        if (bootstrapServers == null) {
            throw new T9tException(T9tIOException.MISSING_KAFKA_CONFIGURAION, "No bootstrap servers defined in DataSink nor config.xml");
        }
//        props.put(ProducerConfig.CLIENT_ID_CONFIG, config.getDataSinkId() + ":" + sinkRef.toString());
        props.put(ProducerConfig.LINGER_MS_CONFIG, 100);
        props.put(ProducerConfig.RETRIES_CONFIG, 2);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 8000);  // approx 5 orders
        //props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());
        if (config.getZ() != null) {
            final Object extraKafkaConfig = config.getZ().get("kafka");
            if (extraKafkaConfig instanceof Map<?, ?> extraKafkaConfigMap) {
                LOGGER.info("Found {} additional Producer configuration properties for kafka in data sink {}",
                  extraKafkaConfigMap.size(), config.getDataSinkId());
                for (final Map.Entry<?, ?> entry: extraKafkaConfigMap.entrySet()) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return new KafkaTopicWriter(bootstrapServers, config.getFileOrQueueNamePattern(), props);
    }

    protected KafkaTopicWriter writer;
    protected DataSinkDTO cfg;

    @Override
    public void open(final DataSinkDTO config, final OutputSessionParameters params, final Long sinkRef, final String targetName,
      final MediaTypeDescriptor mediaType, final Charset encoding) {
        cfg = config;
        writer = createKafkaProducer(config);
    }

    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(final String partitionKey, final String recordKey, final byte[] buffer, final int offset, final int len, final boolean isDataRecord) {
        final byte[] data = (offset == 0 && (len < 0 || len == buffer.length))
          ? buffer : Arrays.copyOfRange(buffer, offset, offset + len);
        final int partition = partitionKey.hashCode();
        writer.write(data, partition, recordKey);
    }

    @Override
    public void write(final String partitionKey, final String recordKey, final String data) {
        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        write(partitionKey, recordKey, bytes, 0, bytes.length, true);
    }

    @Override
    public void close() {
        writer.close();
    }
}
