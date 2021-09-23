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
package com.arvatosystems.t9t.jetty.impl;

import java.util.List;
import java.util.function.Function;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.xml.GenericResult;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Singleton;

@Singleton
public class T9tRestProcessor implements IT9tRestProcessor {
    @Override
    public void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, RequestParameters requestParameters, String infoMsg) {
        GenericResultFactory.performAsyncBackendRequest(httpHeaders, resp, requestParameters, infoMsg);
    }

    @Override
    public <T extends ServiceResponse> void performAsyncBackendRequest(HttpHeaders httpHeaders,
            AsyncResponse resp, RequestParameters requestParameters, String infoMsg, Class<T> backendResponseClass,
            Function<T, BonaPortable> responseMapper) {
        GenericResultFactory.performAsyncBackendRequest(httpHeaders, resp, requestParameters, infoMsg, backendResponseClass, responseMapper);
    }

    @Override
    public <T> void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, String infoMsg,
            List<T> inputData, Function<T, RequestParameters> requestConverterSingle,
            Function<List<T>, RequestParameters> requestConverterBatch) {
        GenericResultFactory.performAsyncBackendRequest(httpHeaders, resp, infoMsg, inputData, requestConverterSingle, requestConverterBatch);
    }

    @Override
    public GenericResult createResultFromServiceResponse(ServiceResponse response) {
        return GenericResultFactory.createResultFromServiceResponse(response);
    }

    @Override
    public void returnAsyncResult(HttpHeaders httpHeaders, AsyncResponse resp, Response.Status status, Object result) {
        GenericResultFactory.returnAsyncResult(httpHeaders, resp, status, result);
    }
}
