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

public interface IConfigurationService {

    /**
     * Initializes the indexes for all entities that are annotated with @Indexed.
     * This method should be called at application startup to ensure that all
     * necessary indexes are created and up-to-date.
     * Note: This operation erases all existing indexes for the entity class!
     */
    <T> void createIndexesFromScratch(Class<T> entityClass) throws InterruptedException;

    <T> void updateIndexes(Class<T> entityClass) throws InterruptedException;

    <T> void checkIndexStatus(Class<T> entityClass) throws InterruptedException;
}
