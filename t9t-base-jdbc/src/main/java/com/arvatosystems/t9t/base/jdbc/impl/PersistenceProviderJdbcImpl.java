/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jdbc.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IJdbcDataSource;
import com.arvatosystems.t9t.base.services.IPersistenceProviderJdbc;

import de.jpaw.bonaparte.pojos.api.PersistenceProviders;

class PersistenceProviderJdbcImpl implements IPersistenceProviderJdbc {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceProviderJdbcImpl.class);
    private Connection connection;

    /** The constructor of the provider is usually invoked by some application specific producer. */
    PersistenceProviderJdbcImpl(final IJdbcDataSource ds) {
        LOGGER.trace("new(): creating Connection");
        try {
            connection = ds.getConnection();
            connection.setAutoCommit(false);
        } catch (final SQLException e) {
            LOGGER.error("{} on JDBC new connection: {}", e.getClass().getSimpleName(), e.getMessage());
            throw new T9tException(T9tException.JDBC_GENERAL_SQL, e.getClass().getSimpleName());
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public String getId() {
        return "JDBC";
    }

    @Override
    public int getPriority() {
        return PersistenceProviders.UNUSED.ordinal();  // there is no JDBC entry, grab the "unused" slot
    }

    @Override
    public void open() {
        LOGGER.trace("open(): starting transaction");
    }

    @Override
    public void rollback() {
        LOGGER.trace("rollback(): terminating transaction");
        try {
            connection.rollback();
        } catch (final SQLException e) {
            // cannot do anything because we are rolling back already anyway
            LOGGER.error("{} on JDBC rollback: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void commit() throws Exception {
        LOGGER.trace("commit(): transaction end");
        try {
            connection.commit();
        } catch (final SQLException e) {
            // cannot do anything because we are rolling back already anyway
            LOGGER.error("{} on JDBC commit: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void close() {
        LOGGER.trace("close(): destroying connection");
        // allow multiple closes...
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException e) {
                // cannot do anything except reporting it
                LOGGER.error("{} on JDBC close: {}", e.getClass().getSimpleName(), e.getMessage());
            }
            connection = null;
        }
    }
}
