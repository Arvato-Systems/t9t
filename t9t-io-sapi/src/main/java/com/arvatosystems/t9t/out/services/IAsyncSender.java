/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
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
    boolean send(@Nonnull AsyncChannelDTO channel, int timeout, @Nonnull InMemoryMessage msg, Consumer<AsyncHttpResponse> resultProcessor,
      long whenStarted) throws Exception;

    /**
     * Determines if a http response code is considered to be "OK".
     * The default implementation maps http codes 200 to 299 to "OK".
     */
    default boolean httpStatusIsOk(final int httpCode) {
        return httpCode / 100 == 2;
    }

    /**
     * Converts a http status code to a message status.
     * The default implementation only relies on the httpCode, customizations may want to use channel configuration in addition.
     *
     * @param httpCode   the HTTP status code
     * @param channel    the channel configuration
     *
     * @return a classification of the status
     */
    default ExportStatusEnum httpCodeToStatus(final int httpCode, final AsyncChannelDTO channel) {
        if (httpCode == 408 || httpCode == T9tConstants.HTTP_STATUS_INTERNAL_TIMEOUT) {
            return ExportStatusEnum.RESPONSE_TIMEOUT;  // timeout, please retry
        }
        switch (httpCode / 100) {
        case 2:
            return ExportStatusEnum.RESPONSE_OK;     // OK
        case 4:
            return ExportStatusEnum.RESPONSE_ABORT;  // error, please fix
        case 5:
            return ExportStatusEnum.RESPONSE_ERROR;  // error, please retry
        default:
            return ExportStatusEnum.RESPONSE_UNKNOWN;
        }
    }

    /** Called when the corresponding writer thread is shut down. */
    default void close() { }
}
