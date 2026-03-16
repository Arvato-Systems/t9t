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
package com.arvatosystems.t9t.rest.utils;

import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.Nonnull;
import jakarta.ws.rs.container.ContainerRequestContext;

public final class AuthFilterRegistry {
    private AuthFilterRegistry() {
        // prevent instantiation
    }

    @FunctionalInterface
    public interface IExtraAuthFilter {
        /** Returns true if the request should be denied, false to continue processing. */
        boolean extraAuthFilter(@Nonnull String httpMethod, @Nonnull ContainerRequestContext requestContext);
    }

    private static final ConcurrentHashMap<String, IExtraAuthFilter> EXTRA_AUTH_FILTERS = new ConcurrentHashMap<>();

    /**
     * Registers an extra auth filter for a specific path.
     * This method is typically called from @Startup classes to add custom authentication logic for specific endpoints.
     */
    public static void registerExtraAuthFilter(@Nonnull final String path, @Nonnull final IExtraAuthFilter filter) {
        if (EXTRA_AUTH_FILTERS.putIfAbsent(path, filter) != null) {
            throw new IllegalStateException("FATAL: An auth filter is already registered for path: " + path);
        }
    }

    /** Returns true if the request should be denied, false to continue processing, or null if there is no custom filter for the given path. */
    public static Boolean extraAuthFilter(@Nonnull final String path, @Nonnull final String httpMethod, @Nonnull final ContainerRequestContext requestContext) {
        final IExtraAuthFilter filter = EXTRA_AUTH_FILTERS.get(path);
        if (filter == null) {
            return null; // no custom filter for this path
        }
        return filter.extraAuthFilter(httpMethod, requestContext);
    }
}
