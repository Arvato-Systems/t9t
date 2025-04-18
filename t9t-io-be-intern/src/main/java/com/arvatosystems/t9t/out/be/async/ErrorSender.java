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
package com.arvatosystems.t9t.out.be.async;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncHttpResponse;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.InMemoryMessage;
import com.arvatosystems.t9t.out.services.IAsyncSender;

import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

/**
 * The ErrorSender has been created for testing purposes. It always returns a "server error".
 */
@Dependent
@Named("ERROR")
public class ErrorSender implements IAsyncSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorSender.class);

    @Override
    public void init(final AsyncQueueDTO queue) {
        LOGGER.info("Creating IAsyncSender ERROR for queue {}", queue.getAsyncQueueId());
    }

    @Override
    public boolean send(final AsyncChannelDTO channel, final int timeout, final InMemoryMessage msg,
      final Consumer<AsyncHttpResponse> resultProcessor, final long whenStarted) {
        LOGGER.debug("Returning error for channel {}, object {}", channel.getAsyncChannelId(), msg.getPayload().ret$PQON());
        final AsyncHttpResponse resp = new AsyncHttpResponse();
        resp.setHttpReturnCode(500);
        resp.setHttpStatusMessage("Error");
        resultProcessor.accept(resp);
        return false;
    }
}
