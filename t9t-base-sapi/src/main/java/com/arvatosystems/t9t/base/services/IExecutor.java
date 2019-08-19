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

import java.util.Set;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.event.EventParameters;

import de.jpaw.bonaparte.core.BonaPortable;

/**
 * Implementations serve as key entry points for intra-module communication.
 */
public interface IExecutor {

    /**
     * Method handles incoming synchronous service request. Does this locally if the service sits in the same JVM, otherwise, serializes the request, transmits
     * it and deserializes the response. The remote location is obtained via ????? (tbd). (Future expansion: Takes care, if the service executor is in a bundle
     * which is not active.)
     *
     * @param params
     *            The received request parameters
     * @return The response object related to the given service request
     */
    ServiceResponse executeSynchronous(RequestParameters params);
    // preferred entry! context passed, but has been established already
    ServiceResponse executeSynchronous(RequestContext ctx, RequestParameters params);

    /**
     * Invokes executeSynchronous() and checks the result for correctness and the response type.
     *
     */
    <T extends ServiceResponse> T executeSynchronousAndCheckResult(RequestParameters params, Class<T> requiredType);
    <T extends ServiceResponse> T executeSynchronousAndCheckResult(RequestContext ctx, RequestParameters params, Class<T> requiredType);

    /** Schedules the provided request asynchronously. It will be executed with the same userId / tenant as the current request, and only if the current
     * request is technically successful (i.e. does no rollback, i.e. a returncode is 0 <= r <= 199999999).
     *
     * The response of this request is the response of the primary request.
     */
    void executeAsynchronous(RequestParameters params);
    void executeAsynchronous(RequestContext ctx, RequestParameters params);
    void executeAsynchronous(RequestContext ctx, RequestParameters params, boolean priority);

    /** Send an event. */
    void sendEvent(EventParameters data);
    void sendEvent(RequestContext ctx, EventParameters data);

    /** Publish an event. */
    void publishEvent(EventParameters data);
    void publishEvent(RequestContext ctx, EventParameters data);

    /** Writes to multiple buckets. */
    void writeToBuckets(Set<String> bucketIds, Long ref, Integer mode);

    /** Publishes an event to clear caches.
     * cacheId usually is the DTO simple name or JPA entity base class name (or PQON),
     * key can be null, or specifies an element.
     */
    void clearCache(String cacheId, BonaPortable key);
}
