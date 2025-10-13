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
package com.arvatosystems.t9t.base.jpa.ormspecific;

import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManagerFactory;

public interface IEMFCustomizer {
    /**
     * Create a customized EntityManagerFactory for the given persistence unit name and settings.
     *
     * @param puName the name of the persistence unit as defined in persistence.xml
     * @param settings either the primary or the shadow database settings
     * @param configureTextSearch flag which decides if additional configuration for full text search (for example Hibernate Search) should be applied
     * @return a configured {@link EntityManagerFactory} instance for the specified persistence unit, customized according to the provided settings and text search configuration
     * @throws jakarta.persistence.PersistenceException if the EntityManagerFactory cannot be created due to persistence or database connection failures
     * @throws java.lang.IllegalArgumentException if the provided configuration is invalid
     * @throws Exception for other unexpected errors during factory creation or configuration
     */
    @Nonnull
    EntityManagerFactory getCustomizedEmf(@Nonnull String puName, @Nonnull RelationalDatabaseConfiguration settings, boolean configureTextSearch) throws Exception;
}
