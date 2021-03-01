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
package org.flywaydb.core;

import static java.util.stream.Collectors.toList;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Stream;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendedFlyway extends Flyway {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedFlyway.class);

    /**
     * Skip all pending migrations by just adding them to the migration log. Thus those migrations are not executed and
     * will not be in the future.
     *
     * @return Count pending migrations skipped
     */
    public int skipPendingMigrations() throws FlywayException {
        LOGGER.info("Skip pending migrations");
        final MigrationInfo[] pendingMigrations = info().pending();

        if (pendingMigrations == null || pendingMigrations.length == 0) {
            LOGGER.debug("No pending migrations found");
            return 0;
        }

        final List<MigrationInfo> migrationsToSkip = Stream.of(pendingMigrations)
                                                           .filter(migration -> migration.getVersion() != null)
                                                           .sorted()
                                                           .collect(toList());

        if (migrationsToSkip.isEmpty()) {
            LOGGER.debug("No pending migrations found");
            return 0;
        }

        return execute(new Command<Integer>() {
            @Override
            public Integer execute(Connection connectionMetaDataTable,
                                   MigrationResolver migrationResolver,
                                   MetaDataTable metaDataTable,
                                   DbSupport dbSupport,
                                   Schema[] schemas,
                                   FlywayCallback[] flywayCallbacks) {

                final Connection connection = dbSupport.useSingleConnection() ? connectionMetaDataTable : JdbcUtils.openConnection(getDataSource());

                try {
                    for (MigrationInfo migration : migrationsToSkip) {
                        LOGGER.debug("Skip migration script {} ({}) of version {}", migration.getScript(), migration.getType(), migration.getVersion());
                        final AppliedMigration skipMigra = new AppliedMigration(migration.getVersion(), migration.getDescription(),
                                                                                migration.getType(), migration.getScript(), migration.getChecksum(),
                                                                                0, true);
                        metaDataTable.addAppliedMigration(skipMigra);
                    }

                    return migrationsToSkip.size();
                } finally {
                    if (!dbSupport.useSingleConnection()) {
                        JdbcUtils.closeConnection(connection);
                    }
                }
            }
        });
    }

}
