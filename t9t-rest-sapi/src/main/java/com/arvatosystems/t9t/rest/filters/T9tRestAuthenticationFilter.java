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

    private final IAuthFilterCustomization authFilterCustomization = Jdp.getRequired(IAuthFilterCustomization.class);

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            authFilterCustomization.filterUnauthenticated(requestContext);
            authFilterCustomization.filterSupportedMediaType(requestContext.getMediaType());
        } else {
            authFilterCustomization.filterAuthenticated(authHeader, requestContext);
            authFilterCustomization.filterSupportedMediaType(requestContext.getMediaType());
        }
    }
}
