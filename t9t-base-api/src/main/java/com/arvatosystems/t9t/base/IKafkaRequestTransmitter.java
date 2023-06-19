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
package com.arvatosystems.t9t.base;

import com.arvatosystems.t9t.base.api.ServiceRequest;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Interface to use for writing request messages to t9t server nodes. (The payloads are of type <code>ServiceRequest</code>.)
 * For a low level API to transfer any BonaPortable, see <code>IKafkaTopicWriter</code>.
 */
public interface IKafkaRequestTransmitter {
    /**
     * Should normally return true, provided for callers which have an alternative way to transmit messages
     * in case kafka could not be initialized properly.
     */
    boolean initialized();

    /** Writes a message with a given partition key and record key. */
    void write(@Nonnull ServiceRequest srq, @Nonnull String partitionKey, @Nullable Object recordKey);
}
