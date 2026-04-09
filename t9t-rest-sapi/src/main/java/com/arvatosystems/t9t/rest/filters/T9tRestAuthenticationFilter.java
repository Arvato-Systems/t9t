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
package com.arvatosystems.t9t.rest.filters;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.ipblocker.services.impl.IPAddressBlocker;
import com.arvatosystems.t9t.rest.services.IAuthFilterCustomization;

@Provider
@PreMatching
public class T9tRestAuthenticationFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRestAuthenticationFilter.class);

    private final IAuthFilterCustomization authFilterCustomization = Jdp.getRequired(IAuthFilterCustomization.class);
    private final IPAddressBlocker ipBlockerService = Jdp.getRequired(IPAddressBlocker.class);

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        LOGGER.trace("Starting filter");
        if (authFilterCustomization.filterHttpMethod(requestContext)) {
            LOGGER.debug("aborting due to not enabled OPTIONS method");
            return;  // aborted
        }
        final String remoteIpHeader = requestContext.getHeaderString(T9tConstants.HTTP_HEADER_FORWARDED_FOR);
        if (authFilterCustomization.filterBlockedIpAddress(requestContext, remoteIpHeader)) {
            LOGGER.debug("aborting due to blocked IP address {}", remoteIpHeader);
            return;  // aborted
        }
        final String idempotencyHeader = requestContext.getHeaderString(T9tConstants.HTTP_HEADER_IDEMPOTENCY_KEY);
        if (authFilterCustomization.filterCorrectIdempotencyPattern(requestContext, idempotencyHeader)) {
            LOGGER.debug("aborting due to invalid format of idempotency header");
            return;  // aborted
        }
        final String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            LOGGER.trace("Starting filter - unauthed");
            if (authFilterCustomization.filterUnauthenticated(requestContext)) {
                LOGGER.debug("aborting due to forbidden unauthed access");
                return;  // aborted
            }
            if (authFilterCustomization.filterSupportedMediaType(requestContext)) {
                LOGGER.debug("aborting due to forbidden media type");
                return;  // aborted
            }
        } else {
            LOGGER.trace("Starting filter - authed");
            // swap headers if required, in case session token and API key have been provided
            final MultivaluedMap<String, String> headers = requestContext.getHeaders();
            final String xApiKeyHeader = headers.getFirst(T9tConstants.HTTP_HEADER_X_API_KEY);
            String authHeaderNew = authHeader;
            if (xApiKeyHeader != null) {
                if (authHeader.startsWith(T9tConstants.HTTP_AUTH_PREFIX_JWT) && !headers.containsKey(T9tConstants.HTTP_HEADER_X_SESSION_TOKEN)) {
                    // can be swapped
                    LOGGER.debug("Swapping API key and session token headers");
                    authHeaderNew = T9tConstants.HTTP_AUTH_PREFIX_API_KEY + xApiKeyHeader;
                    headers.putSingle(HttpHeaders.AUTHORIZATION, authHeaderNew);
                    headers.putSingle(T9tConstants.HTTP_HEADER_X_SESSION_TOKEN, authHeader.substring(T9tConstants.HTTP_AUTH_PREFIX_JWT.length()));
                    headers.remove(T9tConstants.HTTP_HEADER_X_API_KEY);
                } else {
                    LOGGER.warn("Received both API key and session token, but session token is not a JWT or x-session-token header is already present - cannot swap headers");
                }
            }
            if (authFilterCustomization.filterAuthenticated(requestContext, authHeaderNew)) {
                // any bad auth should record the IP address as "bad"
                ipBlockerService.registerBadAuthFromIp(remoteIpHeader);
                LOGGER.debug("aborting due failed authentication from {}", remoteIpHeader);
                return;  // aborted
            }
            if (authFilterCustomization.filterSupportedMediaType(requestContext)) {
                LOGGER.debug("aborting due to forbidden media type");
                return;  // aborted
            }
        }
        LOGGER.trace("Filter fully passed");
    }
}
