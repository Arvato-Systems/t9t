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
package com.arvatosystems.t9t.base.services;

import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.event.EventData;

/** The IAsyncRequestProcessor allows the asynchronous (in a separate transaction) processing of requests.
 * It is guaranteed that requests will be started in the order of submission.
 * They will not necessarily be completed in this order. If one request should be requests in a common transaction,
 * the BatchRequest is suitable for that. If they should be decoupled into separate transactions, use the AsyncBatchRequest,
 * which processes the second one after the first one has been completed, but in a separate transaction.
 *
 * The ServiceRequest has to include AuthenticationParameters (currently only JWT is supported).
 */
public interface IAsyncRequestProcessor {
    /** Executes a task asynchronously. To be called from externally of any request context. */
    void submitTask(ServiceRequest request);

    /** Sends event data to a single subscriber (node). */
    void send(EventData data);

    /** Publishes event data to all subscribers. */
    void publish(EventData data);

    /** Register an IEventHandler as subscriber for an eventID. */
    void registerSubscriber(String eventID, IEventHandler subscriber);
    /** Register an IEventHandler as subscriber for an eventID within a defined tenant */
    void registerSubscriber(String eventID, Long tenantRef, IEventHandler subscriber);
}
