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
package com.arvatosystems.t9t.base.jdbc.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.jdbc.IJdbcDataSourceBuilder;
import com.arvatosystems.t9t.base.services.IJdbcDataSource;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import de.jpaw.dp.Singleton;

@Singleton
public class JdbcDataSourceBuilder implements IJdbcDataSourceBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcDataSourceBuilder.class);

    private static final int DEFAULT_MAX_POOL_SIZE = 20;
    private static final int DEFAULT_MIN_IDLE = 5;

    @Override
    public IJdbcDataSource initSecondaryDataSource(final RelationalDatabaseConfiguration db2cfg) {
        final HikariConfig hcfg = new HikariConfig();
        if (db2cfg.getJdbcDriverClass() != null) {
            hcfg.setDriverClassName(db2cfg.getJdbcDriverClass());
        }
        hcfg.setUsername(db2cfg.getUsername());
        hcfg.setPassword(db2cfg.getPassword());
        hcfg.setJdbcUrl(db2cfg.getJdbcConnectString());
        hcfg.setAutoCommit(false);

        hcfg.setMaximumPoolSize(DEFAULT_MAX_POOL_SIZE);
        if (db2cfg.getHikariMaximumPoolSize() != null) {
            hcfg.setMaximumPoolSize(db2cfg.getHikariMaximumPoolSize().intValue());
        }
        hcfg.setMinimumIdle(DEFAULT_MIN_IDLE);
        if (db2cfg.getHikariMinimumIdle() != null) {
            hcfg.setMinimumIdle(db2cfg.getHikariMinimumIdle().intValue());
        }
        if (T9tUtil.isNotBlank(db2cfg.getHikariExceptionOverrideClassName())) {
            hcfg.setExceptionOverrideClassName(db2cfg.getHikariExceptionOverrideClassName());
        }
        if (db2cfg.getHikariMaxLifetime() != null) {
            hcfg.setMaxLifetime(db2cfg.getHikariMaxLifetime().longValue());
        }

        additionalInitialization(hcfg, db2cfg.getZ());

        final HikariDataSource ds = new HikariDataSource(hcfg);
        if (LOGGER.isTraceEnabled()) {
            // wrap it into some logger
            return new LoggingJdbcDataSource(ds);
        } else {
            // return it as is
            return new NonLoggingJdbcDataSource(ds);
        }
    }

    /** Initialize z fields (if any), can also be used to install specific other settings (for example for AWS JDC wrapper). */
    protected void additionalInitialization(final HikariConfig hcfg, final List<String> z) {
        if (z != null) {
            // construct some additional data source properties
            for (final String s: z) {
                final String[] kvp = s.split("=");
                if (kvp.length == 2) {
                    final String key = kvp[0].trim();
                    final String value = kvp[1].trim();
                    LOGGER.info("Setting additional datasource property {} = {}", key, value);
                    hcfg.addDataSourceProperty(key, value);
                }
            }
        }
    }

    private static final class LoggingJdbcDataSource implements IJdbcDataSource {
        final HikariDataSource ds;

        private LoggingJdbcDataSource(HikariDataSource ds) {
            this.ds = ds;
        }

        @Override
        public Connection getConnection() throws SQLException {
            LOGGER.trace("New JDBC connection requested");
            return ds.getConnection();
        }
    }

    private static final class NonLoggingJdbcDataSource implements IJdbcDataSource {
        final HikariDataSource ds;

        private NonLoggingJdbcDataSource(HikariDataSource ds) {
            this.ds = ds;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return ds.getConnection();
        }
    }
}
