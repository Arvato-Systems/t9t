/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.xml.GenericResult;

import de.jpaw.bonaparte.core.BonaPortable;

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
    <T> void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, String infoMsg,
        List<T> inputData, Function<T, RequestParameters> requestConverterSingle, Function<List<T>, RequestParameters> requestConverterBatch);

    /** Could be static, but declared in the interface to allow overriding. */
    GenericResult createResultFromServiceResponse(ServiceResponse response);

    /** Returns a response without using the worker thread. */
    void returnAsyncResult(String acceptHeader, AsyncResponse resp, Response.Status status, Object result);

    /** Performs the authentication request asynchronously, using a generic response mapper. */
    void performAsyncAuthBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, AuthenticationRequest requestParameters, Consumer<String> cacheUpdater);


    /**
     * Determines which format to use for the response.
     * The type of the response is either the type requested by ACCEPT, or, in case that is null, matches the content type.
     *
     * @param httpHeaders   the HTTP headers provided to the REST request
     * @return              the type of response, or null
     */
    default String determineResponseType(final HttpHeaders httpHeaders) {
        final String accept = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);
        return accept != null ? accept : httpHeaders.getHeaderString(HttpHeaders.CONTENT_TYPE);
    }
}
