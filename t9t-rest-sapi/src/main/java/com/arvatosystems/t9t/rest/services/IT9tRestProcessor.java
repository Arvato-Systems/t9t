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
package com.arvatosystems.t9t.rest.services;

import java.util.List;
import java.util.function.Function;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.rest.utils.RestUtils;
import com.arvatosystems.t9t.xml.GenericResult;

import de.jpaw.bonaparte.core.BonaPortable;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface IT9tRestProcessor {
    /** Performs the request asynchronously, using a generic response mapper. */
    void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, RequestParameters requestParameters, String infoMsg);

    /** Performs the request asynchronously, using a specific response mapper. */
    <T extends ServiceResponse> void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, RequestParameters requestParameters,
            String infoMsg, Class<T> backendResponseClass, Function<T, BonaPortable> responseMapper);

    /**
     * Performs the request asynchronously, with a specific request mapper.
     * If the provided list has a single element, the first converter is applied,
     * otherwise the second (which may be null, in which case a BAD_REQUEST will be returned).
     * Both request converters are executed in the I/O thread.
     **/
    <T extends BonaPortable, R extends RequestParameters> void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, String infoMsg,
        List<T> inputData, Function<T, R> requestConverterSingle, Function<List<T>, RequestParameters> requestConverterBatch);

    /**
     * Performs the request via kafka, if available.
     **/
    default <T extends BonaPortable, R extends RequestParameters> void performAsyncBackendRequestViaKafka(final HttpHeaders httpHeaders,
        final AsyncResponse resp, final String infoMsg, final List<T> inputData, final Function<T, R> requestConverterSingle,
        final Function<R, String> partitionKeyExtractor) {
        performAsyncBackendRequest(httpHeaders, resp, infoMsg, inputData, requestConverterSingle, (Function<List<T>, RequestParameters>)null);
    }

    default boolean kafkaAvailable() {
        return false;
    }

    /** Could be static, but declared in the interface to allow overriding. */
    default GenericResult createResultFromServiceResponse(final ServiceResponse response) {
        final GenericResult result = RestUtils.createErrorResult(response.getReturnCode(), response.getErrorDetails());
        result.setProcessRef(response.getProcessRef());
        return result;
    }

    /** Returns a response without using the worker thread. */
    default void returnAsyncResult(final String acceptHeader, final AsyncResponse resp, final Response.Status status, final Object result) {
        final Response.ResponseBuilder response = Response.status(status);
        response.type(acceptHeader == null || acceptHeader.length() == 0 ? MediaType.APPLICATION_JSON : acceptHeader);
        if (result != null) {
            response.entity(result);
        }
        final Response responseObj = response.build();
        resp.resume(responseObj);
    }

    /** Returns a response without using the worker thread. */
    default void returnAsyncResult(final String acceptHeader, final AsyncResponse resp, final Response.Status status,
      final int errorCode, final String message) {
        returnAsyncResult(acceptHeader, resp, status, RestUtils.createErrorResult(errorCode, message));
    }

    /** Performs the authentication request asynchronously, using a generic response mapper. */
    void performAsyncAuthBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, AuthenticationRequest requestParameters);

    /**
     * Determines which format to use for the response.
     * The type of the response is either the type requested by ACCEPT, or, in case that is null, matches the content type.
     *
     * @param httpHeaders   the HTTP headers provided to the REST request
     * @return              the type of response, or null
     */
    default String determineResponseType(final HttpHeaders httpHeaders) {
        return RestUtils.determineResponseType(httpHeaders);
    }
}
