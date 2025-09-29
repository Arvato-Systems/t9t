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
package com.arvatosystems.t9t.hs.configurate.be.core.service;

import com.arvatosystems.t9t.base.services.RequestContext;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface IConfigurationService {

    /**
     * Create the indexes for given entity.
     * Note: This operation erases all existing indexes for the entity class!
     *
     * @param ctx {@link RequestContext}
     * @param entityClass the entity class to create indexes for
     * @throws InterruptedException if the thread is interrupted
     */
    <T> void createIndexesFromScratch(@Nonnull RequestContext ctx, @Nonnull Class<T> entityClass) throws InterruptedException;

    /**
     * Update the indexes for given entity.
     *
     * @param entityClass the entity class to update indexes for
     * @throws InterruptedException if the thread is interrupted
     */
    <T> void updateIndexes(@Nonnull Class<T> entityClass) throws InterruptedException;

    /**
     * Check the index status for given entity.
     *
     * @param entityClass the entity class to check index status for
     * @return null if index is valid, error message otherwise
     */
    @Nullable
    <T> String checkIndexStatus(@Nonnull Class<T> entityClass);
}
