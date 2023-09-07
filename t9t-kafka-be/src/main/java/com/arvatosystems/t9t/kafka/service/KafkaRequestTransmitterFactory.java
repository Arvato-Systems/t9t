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
package com.arvatosystems.t9t.kafka.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.arvatosystems.t9t.base.IKafkaRequestTransmitter;
import com.arvatosystems.t9t.kafka.service.impl.KafkaRequestTransmitter;

import jakarta.annotation.Nonnull;

public final class KafkaRequestTransmitterFactory {
    private KafkaRequestTransmitterFactory() {
    }

    /** Keeps track of created instances to ensure we build only one per target. */
    private static final Map<String, IKafkaRequestTransmitter> TRANSMITTER_MAP = new ConcurrentHashMap<>();

    /** Obtains an instance of a request transmitter to a certain topic (usually there is one topic per server type). */
    public static IKafkaRequestTransmitter getKafkaRequestTransmitterForTopic(@Nonnull final String topicName) {
        return TRANSMITTER_MAP.computeIfAbsent(topicName, t -> new KafkaRequestTransmitter(t));
    }
}
