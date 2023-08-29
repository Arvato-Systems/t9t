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
package com.arvatosystems.t9t.rest.services;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;

public interface IAuthFilterCustomization {

    /** Returns setting if authentication via JWT is allowed (which should enable login). */
    boolean allowAuthJwt();

    /** Returns setting if authentication via "Http Basic" is allowed. */
    boolean allowAuthBasic();

    /** Returns setting if authentication via API key is allowed. */
    boolean allowAuthApiKey();

    /**
     * Constructs a new Response object for the current request and
     * either throws a WebApplicationException of the given errorStatus, or aborts the filter with that response.
     */
    void abortFilter(@Nonnull ContainerRequestContext requestContext, @Nonnull Response errorStatus);

    /**
     * Checks if the provided userId is allowed to be used for authentication.
     * The default implementation provides a pattern check and a list check.
     *
     * @return false if processing can continue (no problem), true if the filter decides the attempt should be rejected.
     */
    boolean filterByUserId(@Nullable String userId);

    /**
     * Checks if the provided API key is allowed to be used for authentication.
     * The default implementation provides a pattern check.
     *
     * @return false if processing can continue (no problem), true if the filter decides the attempt should be rejected.
     */
    boolean filterByApiKey(@Nonnull String apiKey);

    /**
     * Checks if the request came from a blocked IP address.
     *
     * @return false if processing can continue (no problem), true if the filter has aborted with an error code
     */
    boolean filterBlockedIpAddress(@Nonnull ContainerRequestContext requestContext, @Nullable String remoteIp);

    /**
     * Checks for allowed requests which do not need authentication.
     * Implementations should at least examine the HttpMethod and the Path.
     * Possible cases to allow such requests are:
     * - login
     * - simple ping GET requests for monitoring
     * - Swagger information on test and development systems
     *
     * @return false if processing can continue (no problem), true if the filter has aborted with an error code
     */
    boolean filterUnauthenticated(@Nonnull ContainerRequestContext requestContext);

    /**
     * Checks for allowed requests which come with authentication header.
     *
     * @return false if processing can continue (no problem), true if the filter has aborted with an error code
     */
    boolean filterAuthenticated(@Nonnull ContainerRequestContext requestContext, @Nonnull String authHeader);

    /**
     * Invoked for POST requests: Checks for supported type of payload.
     *
     * @return false if processing can continue (no problem), true if the filter has aborted with an error code
     */
    boolean filterSupportedMediaType(@Nonnull ContainerRequestContext requestContext);

    /**
     * Checks for correct UUID in case an idempotency header has been provided.
     *
     * @return false if processing can continue (no problem), true if the filter has aborted with an error code
     */
    boolean filterCorrectIdempotencyPattern(@Nonnull ContainerRequestContext requestContext, @Nullable String idempotencyHeader);
}
