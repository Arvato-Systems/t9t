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
package com.arvatosystems.t9t.jetty.kafka;

import java.util.List;
import java.util.function.Function;

import com.arvatosystems.t9t.base.api.RequestParameters;

import de.jpaw.bonaparte.core.BonaPortable;
import jakarta.ws.rs.core.Response;

/**
 * Defines the implementation from gateway to kafka.
 */
public interface IGateWayToServerByKafka {
    /**
     * Sends a request to the correct backend server node via kafka.
     *
     * @param authentication
     *          the contents of REST request's http <code>Authorization</code> parameter.
     *          Will be used as authentication in the backend. The permission is checked upfront (cached)
     *          in order to be able to provide immediate feedback to the caller about correct permissions.
     *          Also required in order to avoid DoS (unauthenticated callers filling the kafka topic).
     * @param partition
     *          a numeric value which will be used to determine the correct partition to use (will be taken
     *          modulus the actual number of partitions of the topic). Usually the hashCode of the customerId,
     *          orderId, or a productId or skuId.
     * @param request
     *          the request which should be processed.
     * @return the http response status code
     */
    Response.Status sendToServer(String authentication, String partitionKey, RequestParameters request);

    /**
     * Sends a request, where the input data must be wrapped in a list of one element.
     *
     * @param authentication
     *          the contents of REST request's http <code>Authorization</code> parameter.
     *          Will be used as authentication in the backend. The permission is checked upfront (cached)
     *          in order to be able to provide immediate feedback to the caller about correct permissions.
     *          Also required in order to avoid DoS (unauthenticated callers filling the kafka topic).
     * @param partition
     *          a numeric value which will be used to determine the correct partition to use (will be taken
     *          modulus the actual number of partitions of the topic). Usually the hashCode of the customerId,
     *          orderId, or a productId or skuId.
     * @param request
     *          the request which should be processed.
     * @return the http response
     */
    <T extends BonaPortable, R extends RequestParameters> Response
        sendToServer(String authentication, String pathInfo, List<T> inputData,
            Function<T, R> requestParameterConverter,
            Function<R, String> partitionKeyExtractor);
}
