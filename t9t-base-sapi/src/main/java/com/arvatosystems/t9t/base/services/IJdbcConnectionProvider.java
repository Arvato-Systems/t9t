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
package com.arvatosystems.t9t.base.services;

import java.sql.Connection;
import java.util.List;

/** Defines a way to obtain a JDBC connection from some persistence layer built on top of it, for example JPA.
 * Inject with @Named("independent") to get connections which are not tied to the current session. */
public interface IJdbcConnectionProvider {
    /** Retrieves the JDBC connection of the current session. */
    Connection getJDBCConnection();
    List<Integer> checkHealth();
}
