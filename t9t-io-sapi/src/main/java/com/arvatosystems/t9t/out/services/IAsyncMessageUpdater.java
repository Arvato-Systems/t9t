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

import java.util.List;

import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncQueueDTO;

/** Interface to JPA layer which updates the async message.
 * This is a technical helper method. */
public interface IAsyncMessageUpdater {
    /** Updates a message entry to its latest status. */
    void updateMessage(Long objectRef, ExportStatusEnum newStatus, Integer httpCode, Integer clientCode, String clientReference);

    /** Reads all active queues (all tenants) from the database. */
    List<AsyncQueueDTO> getActiveQueues();

    /** Reads a channel configuration from the database. Throws an exception if the specified channel does not exist. */
    AsyncChannelDTO readChannelConfig(String channelId, Long tenantRef);
}
