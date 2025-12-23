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
package com.arvatosystems.t9t.base.be.request;

import com.arvatosystems.t9t.base.request.ApiKeySessionInvalidationRequest;
import com.arvatosystems.t9t.base.services.SessionInvalidation;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class ApiKeySessionInvalidationRequestHandler extends AbstractSessionInvalidationRequestHandler<ApiKeySessionInvalidationRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeySessionInvalidationRequestHandler.class);

    @Override
    protected void performInvalidation(@Nonnull final ApiKeySessionInvalidationRequest request) {
        if (request.getRemoveInvalidation()) {
            LOGGER.debug("Removing JWT invalidation for apiKeyRef {}", request.getApiKeyRef());
            SessionInvalidation.removeApiKeyInvalidation(request.getApiKeyRef());
        } else {
            LOGGER.debug("Invalidating all JWTs for apiKey {}", request.getApiKeyRef());
            SessionInvalidation.invalidateApiKeySession(request.getApiKeyRef(), Instant.now());
        }
    }
}
