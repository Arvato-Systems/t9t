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
package com.arvatosystems.t9t.mediaresolver;

import java.io.InputStream;

import jakarta.annotation.Nonnull;

public interface IMediaDataSource {
    default String getAbsolutePathForTenant(@Nonnull final String relativePath, @Nonnull final String tenantId) {
        // The default implementation does not depend on a tenant.
        return relativePath;
    }

    /** Open a stream for reading, returning the data as it is stored. */
    InputStream open(String path) throws Exception;

    /** Return true if the source was not yet read completely - optional, not supported by most sources. */
    default boolean hasMore(@Nonnull final InputStream is) throws Exception {
        return false;
    }
}
