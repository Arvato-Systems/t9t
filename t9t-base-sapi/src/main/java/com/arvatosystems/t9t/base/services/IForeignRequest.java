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

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;

/**
 * This interface provides access to remote instances of t9t based servers.
 */
public interface IForeignRequest {
    /**
     * Executes a remote request with the same credentials as the current request.
     */
    ServiceResponse execute(RequestContext ctx, RequestParameters rp);

    /**
     * Executes a remote request, using an API key as credentials.
     * The API key is passed as a string, because most key vaults will store it as that and we also will need it as a string, thus
     * avoiding duplicate conversions.
     */
    ServiceResponse execute(RequestParameters rp, String apiKey);

    /**
     * Executes a remote request with the same credentials as the current request.
     */
    <T extends ServiceResponse> T executeSynchronousAndCheckResult(RequestContext ctx, RequestParameters params, Class<T> requiredType);

    /**
     * Executes a remote request, using an API key as credentials.
     * The API key is passed as a string, because most key vaults will store it as that and we also will need it as a string, thus
     * avoiding duplicate conversions.
     */
    <T extends ServiceResponse> T executeSynchronousAndCheckResult(RequestParameters params, String apiKey, Class<T> requiredType);
}
