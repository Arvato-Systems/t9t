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

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;

public interface IAuthFilterCustomization {

    /** Checks if the request came from a blocked IP address. */
    boolean isBlockedIpAddress(String remoteIp);

    /** Records a failed authentication event. */
    void registerBadAuthFromIp(String remoteIp);

    /** Returns setting if authentication via JWT is allowed (which should enable login). */
    boolean allowAuthJwt();

    /** Returns setting if authentication via "Http Basic" is allowed. */
    boolean allowAuthBasic();

    /** Returns setting if authentication via API key is allowed. */
    boolean allowAuthApiKey();

    /**
     * Check for allowed requests which do not need authentication.
     * Implementations should at least examine the HttpMethod and the Path.
     * Possible cases to allow such requests are:
     * - login
     * - simple ping GET requests for monitoring
     * - Swagger information on test and development systems
     * */
    void filterUnauthenticated(ContainerRequestContext requestContext);

    /** Check for allowed requests which come with authentication header. */
    void filterAuthenticated(String authHeader, ContainerRequestContext requestContext);

    /** Check for acceptable Basic authentication. */
    void filterBasic(String authHeader, ContainerRequestContext requestContext);

    /** Check for acceptable API key authentication. */
    void filterApiKey(String authHeader, ContainerRequestContext requestContext);

    /** Check for acceptable JWT authentication. Should throw an exception if this type is not desired. */
    void filterJwt(String authHeader, ContainerRequestContext requestContext);

    /** Invoked for POST requests: Check for supported type of payload. */
    void filterSupportedMediaType(MediaType mediaType);
}
