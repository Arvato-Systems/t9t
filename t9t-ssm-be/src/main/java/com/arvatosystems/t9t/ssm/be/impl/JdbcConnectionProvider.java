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
package com.arvatosystems.t9t.ssm.be.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.quartz.utils.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.services.IJdbcDataSource;

import de.jpaw.dp.Jdp;

public class JdbcConnectionProvider implements ConnectionProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcConnectionProvider.class);
    private final IJdbcDataSource provider = Jdp.getRequired(IJdbcDataSource.class, T9tConstants.QUALIFIER_JDBC_SECONDARY);

    @Override
    public Connection getConnection() throws SQLException {
        LOGGER.trace("Connection requested by Quartz");
        return provider.getConnection();
    }

    @Override
    public void initialize() throws SQLException {
        LOGGER.info("Quartz connection provider initializing");
    }

    @Override
    public void shutdown() throws SQLException {
        LOGGER.info("Quartz connection provider shutting down");
    }
}
