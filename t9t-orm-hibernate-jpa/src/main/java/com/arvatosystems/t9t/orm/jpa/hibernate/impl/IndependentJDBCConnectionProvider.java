/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.orm.jpa.hibernate.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IJdbcConnectionProvider;
import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("independent")
public class IndependentJDBCConnectionProvider implements IJdbcConnectionProvider {
    private   static final Logger LOGGER = LoggerFactory.getLogger(IndependentJDBCConnectionProvider.class);

    protected static final long   SECONDS_BETWEEN_INFO = 60L;
    public    static final String POOLED_DATASOURCE_NAME = "t9t-jdbc";

    protected final AtomicInteger counter = new AtomicInteger();

    protected static class LazyPoolWrapper {
        protected static final PooledDataSource POOLED_DATA_SOURCE = C3P0Registry.pooledDataSourceByName(POOLED_DATASOURCE_NAME);
        protected LazyPoolWrapper() {
            LOGGER.info("Obtaining pooled data source for JDBC connections {}", POOLED_DATA_SOURCE == null ? "FAILED" : " was successful");
        }
    }

    @Override
    public Connection getJDBCConnection() {
        if (LazyPoolWrapper.POOLED_DATA_SOURCE == null) {
            LOGGER.error("Could not find C3P0 pooled data source {}, cannot serve JDBC connections", POOLED_DATASOURCE_NAME);
            final Set<?> x = C3P0Registry.getPooledDataSources();
            LOGGER.info("PoolSet size is {} (using a different name?)", x.size());
            LOGGER.info("Second attempt would be {}", C3P0Registry.pooledDataSourceByName(POOLED_DATASOURCE_NAME) != null);
            return null;
        }
        final int count = counter.incrementAndGet();
        try {
            return LazyPoolWrapper.POOLED_DATA_SOURCE.getConnection();
        } catch (final SQLException e) {
            LOGGER.error("Cannot create Jdbc connection (request {}) - SQLException {}: {}", count, e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public List<Integer> checkHealth() {
        final PooledDataSource pds = LazyPoolWrapper.POOLED_DATA_SOURCE;
        final Integer count = counter.get();
        if (pds == null) {
            LOGGER.error("Could not find C3P0 pooled data source {}, cannot serve JDBC connections", POOLED_DATASOURCE_NAME);
            return Collections.singletonList(count);
        }
        try {
            final Integer cntAll      = pds.getNumConnectionsAllUsers();
            final Integer cntBusy     = pds.getNumBusyConnectionsAllUsers();
            final Integer cntIdle     = pds.getNumIdleConnectionsAllUsers();
            final Integer cntOrphaned = pds.getNumUnclosedOrphanedConnectionsAllUsers();
            LOGGER.debug("JDBC connection request count is {}, connection pool status: {} connections, {} busy, {} idle, {} unclosed orphans",
                count, cntAll, cntBusy, cntIdle, cntOrphaned);
            final List<Integer> result = new ArrayList<>(5);
            result.add(count);
            result.add(cntAll);
            result.add(cntBusy);
            result.add(cntIdle);
            result.add(cntOrphaned);
            return result;
        } catch (final SQLException e) {
            LOGGER.warn("Cannot provide status after {} requests - SQLException {}: {}", count, e.getClass().getSimpleName(), e.getMessage());
            return Collections.singletonList(count);  // return the count at least
        }
    }
}
