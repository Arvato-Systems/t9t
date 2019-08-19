/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jpa.st.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jpa.ormspecific.IJpaJdbcConnectionProvider;
import com.arvatosystems.t9t.base.services.IJdbcConnectionProvider;

public class JDBCConnectionProvider implements IJpaJdbcConnectionProvider, IJdbcConnectionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCConnectionProvider.class);

    private final DataSource dataSource;

    public JDBCConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection get(EntityManager em) {
        LOGGER.trace("get(EntityManager): get jdbc connection based on entity manager {}", em);
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to get JDBC connection", e);
        }
    }

    @Override
    public Connection getJDBCConnection() {
        LOGGER.trace("getJDBCConnection(): get jdbc connection");
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to get JDBC connection", e);
        }
    }

    @Override
    public List<Integer> checkHealth() {
        // I don't think, this belongs here - seems to be some kind of monitoring...
        return Collections.emptyList();
    }
}
