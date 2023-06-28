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
package com.arvatosystems.t9t.base.services;

import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.event.EventData;
import com.arvatosystems.t9t.base.event.EventHeader;
import com.arvatosystems.t9t.base.event.EventParameters;

import jakarta.annotation.Nonnull;

/** The IAsyncRequestProcessor allows the asynchronous (in a separate transaction) processing of requests.
 * It is guaranteed that requests will be started in the order of submission.
 * They will not necessarily be completed in this order. If one request should be requests in a common transaction,
 * the BatchRequest is suitable for that. If they should be decoupled into separate transactions, use the AsyncBatchRequest,
 * which processes the second one after the first one has been completed, but in a separate transaction.
 *
 * The ServiceRequest has to include AuthenticationParameters (currently only JWT is supported).
 */
public interface IAsyncRequestProcessor {
    /**
     * Executes a task asynchronously. To be called from externally of any request context.
     * By default the task will be submitted to any node of this server.
     * Setting localNodeOnly will keep it on the same node.
     * Setting publish will use a publish instead of send (every node will receive and execute it).
     */
    void submitTask(@Nonnull ServiceRequest request, boolean localNodeOnly, boolean publish);

    /**
     * Wraps the event parameters into an event header, with data provided by the current request context.
     *
     * @param ctx    the current request context
     * @param params the target parameters
     * @return a wrapped EventData instance
     */
    default EventData toEventData(@Nonnull final RequestContext ctx, @Nonnull final EventParameters params) {
        final EventHeader header = new EventHeader();
        header.setTenantId(ctx.tenantId);
        header.setInvokingProcessRef(ctx.requestRef);
        header.setEncodedJwt(ctx.internalHeaderParameters.getEncodedJwt());
        return new EventData(header, params);
    }

    /** Sends event data to a single subscriber (node). */
    void send(@Nonnull EventData data);

    /** Publishes event data to all subscribers. */
    void publish(@Nonnull EventData data);

    /** Register an IEventHandler as subscriber for an eventID. */
    void registerSubscriber(@Nonnull String eventID, @Nonnull IEventHandler subscriber);

    /** Register an IEventHandler as subscriber for an eventID within a defined tenant */
    void registerSubscriber(@Nonnull String eventID, @Nonnull String tenantId, @Nonnull IEventHandler subscriber);
}
