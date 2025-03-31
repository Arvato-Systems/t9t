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
package com.arvatosystems.t9t.vdb.service;

import java.util.List;

import com.arvatosystems.t9t.base.types.FloatVector;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Generic API to perform low level operations on vector databases.
 *
 * Vector stores are considered to consists of different tables, each of them supporting multiple namespaces (used to distinguish threads / conversations).
 */
public interface IVectorIO {
    /**
     * Inserts vectors into the database. In case of existing keys, these will be overwritten (UPSERT).
     * Any provided distance field will be ignored.
     */
    void insertVectors(@Nonnull String tableName, @Nullable String namespace, @Nonnull List<FloatVector> vectors);

    /**
     * Retrieves the k ANN vectors from the database. Returns an empty list if no data is in the namespace.
     * The applied distance metric is determined by the table (and its index type).
     *
     * @param tableName the table name
     * @param namespace the namespace
     * @param queryVector the query vector
     * @param k the number of nearest neighbors to retrieve
     * @return the k approximate nearest neighbors
     */
    @Nonnull List<FloatVector> getApproximateNearestNeighbors(@Nonnull String tableName, @Nullable String namespace, @Nonnull FloatVector queryVector, int k);
}
