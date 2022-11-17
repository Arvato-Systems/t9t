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

import java.util.List;

import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncQueueDTO;

/** Interface to JPA layer which updates the async message.
 * This is a technical helper method. */
public interface IAsyncMessageUpdater {
    /**
     * Updates a message entry to its latest status.
     * The implementation truncates any passed contents for fields clientReference and errorDetails to the allowed length in the DB.
     * @param objectRef       the primary key of the status message in the database
     * @param newStatus       the status of the most recent send attempt
     * @param httpCode        the http status code received (useful to determine if and when to retry)
     * @param clientCode      any parsed client status code from the response
     * @param clientReference any reference provided by the client (client processing transaction ID etc)
     * @param errorDetails    any additional information describing a problem, from the client's response payload
     */
    void updateMessage(Long objectRef, ExportStatusEnum newStatus, Integer httpCode, Integer clientCode, String clientReference, String errorDetails);

    /** Reads all active queues (all tenants) from the database. */
    List<AsyncQueueDTO> getActiveQueues();

    /** Reads a channel configuration from the database. Throws an exception if the specified channel does not exist. */
    AsyncChannelDTO readChannelConfig(String channelId, String tenantId);
}
