/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

import java.util.concurrent.ExecutorService;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;

/** Implementations are used to launch requests as autonomous transactions. */
public interface IAutonomousExecutor {
    /** Launch a request in an autonomous transaction - backwards compatible version. */
    default ServiceResponse execute(RequestContext ctx, RequestParameters rp) {
        return execute(ctx, rp, true);
    }

    /** Launch a request in an autonomous transaction. */
    ServiceResponse execute(RequestContext ctx, RequestParameters rp, boolean skipPermissionCheck);

    /** Launch a request in an autonomous transaction and expect a specific response type. */
    <T extends ServiceResponse> T executeAndCheckResult(RequestContext ctx, RequestParameters rp, Class<T> responseClass);

    /** Obtain the executor pool (only to be used for metering). */
    ExecutorService getExecutorServiceForMetering();
}
