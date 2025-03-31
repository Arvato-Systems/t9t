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
package com.arvatosystems.t9t.base.services;

import jakarta.annotation.Nonnull;

/**
 * Interface to be implemented by various methods which return new surrogate keys.
 *
 * Implementations are supposed to annotate the implementation with a @Named qualifier.
 * The value of the qualifier is determined by the server.xml configuration file,
 * in field <code>keyPrefetchConfiguration.strategy</code>.
 */
public interface ISingleRefGenerator {
    /** Returns a supplier of raw values for primary key creation, based on sequences. */
    long getNextSequence(@Nonnull String selectStatementOrGeneralParameter);

    /** Determines if the implementation wants a database specific full SELECT statement or just a key (for example sequence name). */
    default boolean needSelectStatement() {
        return true;   // most implementations are relational database based
    }
}
