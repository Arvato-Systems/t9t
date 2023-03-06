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
package com.arvatosystems.t9t.rest.filters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.ipblocker.services.impl.IPAddressBlocker;
import com.arvatosystems.t9t.rest.services.IAuthFilterCustomization;

import de.jpaw.dp.Jdp;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

@Provider
@PreMatching
public class T9tRestAuthenticationFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRestAuthenticationFilter.class);

    private final IAuthFilterCustomization authFilterCustomization = Jdp.getRequired(IAuthFilterCustomization.class);
    private final IPAddressBlocker ipBlockerService = Jdp.getRequired(IPAddressBlocker.class);

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        LOGGER.debug("Starting filter");
        final String remoteIpHeader = requestContext.getHeaderString(T9tConstants.HTTP_HEADER_FORWARDED_FOR);
        if (authFilterCustomization.filterBlockedIpAddress(requestContext, remoteIpHeader)) {
            LOGGER.debug("aborting due to blocked IP address");
            return;  // aborted
        }
        final String idempotencyHeader = requestContext.getHeaderString(T9tConstants.HTTP_HEADER_IDEMPOTENCY_KEY);
        if (authFilterCustomization.filterCorrectIdempotencyPattern(requestContext, idempotencyHeader)) {
            LOGGER.debug("aborting due to invalid format of idempotency header");
            return;  // aborted
        }
        final String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            LOGGER.debug("Starting filter - unauthed");
            if (authFilterCustomization.filterUnauthenticated(requestContext)) {
                LOGGER.debug("aborting due to forbidden unauthed access");
                return;  // aborted
            }
            if (authFilterCustomization.filterSupportedMediaType(requestContext)) {
                LOGGER.debug("aborting due to forbidden media type");
                return;  // aborted
            }
        } else {
            LOGGER.debug("Starting filter - authed");
            if (authFilterCustomization.filterAuthenticated(requestContext, authHeader)) {
                // any bad auth should record the IP address as "bad"
                ipBlockerService.registerBadAuthFromIp(remoteIpHeader);
                LOGGER.debug("aborting due failed authentication");
                return;  // aborted
            }
            if (authFilterCustomization.filterSupportedMediaType(requestContext)) {
                LOGGER.debug("aborting due to forbidden media type");
                return;  // aborted
            }
        }
        LOGGER.debug("Filter fully passed");
    }
}
