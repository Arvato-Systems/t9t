/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.out.services;

import de.jpaw.bonaparte.core.BonaPortable;

/**
 * Interface to send a message asynchronously.
 * The message will be sent after a successful commit. It will be persisted (survives a downtime).
 * The call returns a key into the async message table (or null if the implementation uses a queue).
 * The transmitted payload usually is the final external object (for example as defined in API EXT projects, and only needs to be serialized into JSON or XML).
 *
 * This is the first interface used in data flow, the one injected into the business logic methods.
 * */
public interface IAsyncTransmitter {
    Long transmitMessage(String asyncChannelId, BonaPortable payload, Long ref, String category, String identifier);
}
