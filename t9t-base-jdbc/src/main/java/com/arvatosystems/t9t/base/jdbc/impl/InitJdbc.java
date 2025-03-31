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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.jdbc.IJdbcDataSourceBuilder;
import com.arvatosystems.t9t.base.services.IJdbcDataSource;
import com.arvatosystems.t9t.base.services.IPersistenceProviderJdbc;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;

@Startup(12033)
public class InitJdbc implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitJdbc.class);

    private final IJdbcDataSourceBuilder jdbcDataSourceBuilder = Jdp.getRequired(IJdbcDataSourceBuilder.class);

    @Override
    public void onStartup() {
        final T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);
        final RelationalDatabaseConfiguration db2cfg = cfg.getSecondaryDatabaseConfig();
        if (db2cfg == null) {
            LOGGER.info("Not setting up JDBC - no secondary data source defined");
        } else {
            LOGGER.info("Setting up JDBC secondary data source");
            final IJdbcDataSource ds = jdbcDataSourceBuilder.initSecondaryDataSource(db2cfg);
            Jdp.bindInstanceTo(ds, IJdbcDataSource.class, T9tConstants.QUALIFIER_JDBC_SECONDARY);  // make it known to consumers such as BPMN2
            Jdp.registerWithCustomProvider(IPersistenceProviderJdbc.class, new PersistenceProviderJdbcProvider(ds));
        }
    }
}
