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
package com.arvatosystems.t9t.kafka.service;

import de.jpaw.bonaparte.core.BonaPortable;

/**
 * Low level API to transfer any BonaPortable object via kafka.
 * For a specific one for writing request messages to t9t server nodes (payloads of type <code>ServiceRequest</code>),
 * see <code>IKafkaRequestTransmitter</code>.
 */
public interface IKafkaTopicWriter {
    /** Serializes the data object using the compact bonaparte serializer and writes it to the topic. */
    void write(BonaPortable data, int partition, String partitionKey);

    /** Writes binary data to the topic. */
    void write(byte[] dataToWrite, int partitionIn, String recordKey);

    /** Flushes and closes the topic writer. */
    void close();
}
