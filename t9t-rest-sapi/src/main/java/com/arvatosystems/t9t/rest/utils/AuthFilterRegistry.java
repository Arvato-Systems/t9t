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

    /** Predefined filter for common use case: only allow POST method. */
    public static final IExtraAuthFilter ONLY_POST = (httpMethod, context) -> !httpMethod.equalsIgnoreCase("POST");
    /** Predefined filter for common use case: only allow GET method. */
    public static final IExtraAuthFilter ONLY_GET = (httpMethod, context) -> !httpMethod.equalsIgnoreCase("GET");

    private static final ConcurrentHashMap<String, IExtraAuthFilter> EXTRA_AUTH_FILTERS_EXACT  = new ConcurrentHashMap<>();  // path names for exact match
    private static final ConcurrentHashMap<String, IExtraAuthFilter> EXTRA_AUTH_FILTERS_PREFIX = new ConcurrentHashMap<>();  // path names which have path parameters

    /**
     * Registers an extra auth filter for a specific path.
     * This method is typically called from @Startup classes to add custom authentication logic for specific endpoints.
     * If paths ends with a slash, it is regarded as a prefix, and the filter applies to all paths starting with that prefix. Otherwise, it is an exact match.
     */
    public static void registerExtraAuthFilter(@Nonnull final String path, @Nonnull final IExtraAuthFilter filter) {
        if (path.endsWith("/")) {
            // path is a prefix path, e.g. /myapi/items/ for /myapi/items/{id}
            if (EXTRA_AUTH_FILTERS_PREFIX.putIfAbsent(path, filter) == null) {
                return; // successfully registered as prefix filter
            }
        } else {
            // path is an exact match, e.g. /myapi/items for exactly that
            if (EXTRA_AUTH_FILTERS_EXACT.putIfAbsent(path, filter) == null) {
                return; // successfully registered as exact filter
            }
        }
        throw new IllegalStateException("FATAL: An auth filter is already registered for path: " + path);
    }

    /** Returns true if the request should be denied, false to continue processing, or null if there is no custom filter for the given path. */
    public static Boolean extraAuthFilter(@Nonnull final String path, @Nonnull final String httpMethod, @Nonnull final ContainerRequestContext requestContext) {
        // check for exact match first
        final IExtraAuthFilter filter = EXTRA_AUTH_FILTERS_EXACT.get(path);
        if (filter != null) {
            return filter.extraAuthFilter(httpMethod, requestContext);
        }
        // now try prefix match, e.g. for paths with path parameters
        for (final var entry : EXTRA_AUTH_FILTERS_PREFIX.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue().extraAuthFilter(httpMethod, requestContext);
            }
        }
        return null; // no filter registered for this path
    }
}
