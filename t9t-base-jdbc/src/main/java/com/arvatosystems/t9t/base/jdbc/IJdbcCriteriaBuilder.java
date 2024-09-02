/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jdbc;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.function.Consumer;

import com.arvatosystems.t9t.base.search.SearchCriteria;

import jakarta.annotation.Nonnull;

public interface IJdbcCriteriaBuilder {
    /**
     * Completes a partially initialized prepared statement with the WHERE clause and ORDER BY clause.
     *
     * @param sb   the partial SQL SELECT statement. It contains the SELECT and FROM clause, but not the WHERE or ORDER BY clause.
     * @param searchCriteria the search criteria to be used for WHERE and ORDER BY clauses, as well as pagination parameters limit and offset
     *
     * @return a list of lambdas which can be used to set the parameters on the prepared statement
     */
    List<Consumer<PreparedStatement>> createWhereClause(@Nonnull StringBuilder sb, @Nonnull SearchCriteria searchCriteria);
}
