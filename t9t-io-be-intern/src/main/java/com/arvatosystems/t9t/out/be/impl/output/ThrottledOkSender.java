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
package com.arvatosystems.t9t.out.be.impl.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncHttpResponse;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.out.services.IAsyncSender;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

/**
 * The ThrottledOkSender has been created for testing purposes. It always returns OK, but takes the maximum time allowed.
 */
@Dependent
@Named("OK")
public class ThrottledOkSender implements IAsyncSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThrottledOkSender.class);

    @Override
    public void init(AsyncQueueDTO queue) {
        LOGGER.info("Creating IAsyncSender OK for queue {}", queue.getAsyncQueueId());
    }

    @Override
    public AsyncHttpResponse send(AsyncChannelDTO channel, BonaPortable payload, int timeout, Long messageObjectRef) throws InterruptedException {
        if (timeout > 0)
            Thread.sleep(timeout);
        AsyncHttpResponse resp = new AsyncHttpResponse();
        resp.setHttpReturnCode(200);
        return resp;
    }
}
