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
package com.arvatosystems.t9t.out.services;

import java.util.function.Consumer;

import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncHttpResponse;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.InMemoryMessage;

import jakarta.annotation.Nonnull;

/**
 * Describes an implementation of a REST POST or SOAP WS endpoint.
 * The implementations are @Dependent and are constructed once per queue.
 */
public interface IAsyncSender {

    /** Called once after construction. */
    void init(@Nonnull AsyncQueueDTO queue);

    /**
     * Sends the provided payload of the message, synchronously or asynchronously.
     * Returns false if the process has encountered a transmission error (for synchronous messaging).
     */
    boolean send(@Nonnull AsyncChannelDTO channel, int timeout, @Nonnull InMemoryMessage msg, Consumer<AsyncHttpResponse> resultProcessor) throws Exception;

    /** Called when the corresponding writer thread is shut down. */
    default void close() { }
}
