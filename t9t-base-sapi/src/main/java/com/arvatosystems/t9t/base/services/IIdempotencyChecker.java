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

import java.util.UUID;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.RetryAdviceType;
import com.arvatosystems.t9t.base.api.ServiceResponse;

/**
 * Implementations of this interface are responsible to perform a check if the same request has been fired already,
 * and has either finished or is still processing.
 *
 * If the request is still processing, a timeout error should be returned.
 * If the request has finished, the previous result should be returned.
 */
public interface IIdempotencyChecker {
    /** Checks for a prior request and marks the request as "in progress". */
    ServiceResponse runIdempotencyCheck(String tenantId, UUID messageId, RetryAdviceType idempotencyBehaviour, RequestParameters rp);

    /** Stores the result of a finished request. */
    void storeIdempotencyResult(String tenantId, UUID messageId, RetryAdviceType idempotencyBehaviour, RequestParameters rp, ServiceResponse resp);
}
