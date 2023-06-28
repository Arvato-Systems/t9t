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
package com.arvatosystems.t9t.out.services;

import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.InMemoryMessage;

/**
 * This interface provides the link between the logical async queue implementation (for example LTQ or KAFKA)
 * and the execution units such as senders and database APIs.
 */
public interface IAsyncTools {
    /** Retrieves a channel configuration from cache. */
    AsyncChannelDTO getCachedAsyncChannelDTO(String tenantId, String asyncChanneId);

    /** Sends the message via async sender, and updates the database status accordingly. */
    boolean tryToSend(IAsyncSender sender, InMemoryMessage m, int timeout);
}
