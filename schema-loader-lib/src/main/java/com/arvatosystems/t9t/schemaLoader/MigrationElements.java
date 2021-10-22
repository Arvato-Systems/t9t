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
package com.arvatosystems.t9t.schemaLoader;

import static com.arvatosystems.t9t.schemaLoader.config.InstallConfiguration.BASELINE_VERSION_LATEST;
import static com.arvatosystems.t9t.schemaLoader.config.InstallConfiguration.BASELINE_VERSION_LATEST_MAJOR;
import static com.arvatosystems.t9t.schemaLoader.config.InstallConfiguration.BASELINE_VERSION_LATEST_MINOR;
import static java.util.Comparator.naturalOrder;
import static org.apache.commons.lang3.StringUtils.join;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.ExtendedFlyway;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.schemaLoader.config.DatabaseConfiguration;
import com.arvatosystems.t9t.schemaLoader.config.SchemaLoaderConfiguration;
import com.arvatosystems.t9t.schemaLoader.flyway.MigrationCallback;

public class MigrationElements {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationElements.class);

    private final SchemaElements schemaElements;
    private final SchemaLoaderConfiguration configuration;

    public MigrationElements(SchemaLoaderConfiguration configuration, SchemaElements schemaElements) {
        this.configuration = configuration;
        this.schemaElements = schemaElements;
    }

    public void info() {
        LOGGER.info("Show schema info");
        final Flyway flyway = createFlyway(null);

        final MigrationInfo[] migrations = flyway.info().all();

        if (migrations == null || migrations.length == 0) {
            LOGGER.info("No schema information available");
        } else {
            Arrays.sort(migrations);

            final String current = Optional.of(flyway.info())
                                           .map(MigrationInfoService::current)
                                           .map(MigrationInfo::getVersion)
                                           .map(MigrationVersion::toString)
                                           .orElse("unknown");
            LOGGER.info("Current version is {}", current);

            LOGGER.info("Migrations:");

            final SimpleDateFormat dataFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss''SSS");
            try (final Formatter formatter = new Formatter()) {
                formatter.format("  %-18s %-15s %-23s %-12s %-8s %5s %-11s %10s %s",
                                 "Version",
                                 "State",
                                 "Installed On",
                                 "Installed By",
                                 "Type",
                                 "Time",
                                 "Description",
                                 "Checksum",
                                 "Script");
                LOGGER.info(formatter.toString());
            }
            for (MigrationInfo migration : migrations) {
                try (final Formatter formatter = new Formatter()) {
                    formatter.format("  %-18s %-15s %-23s %-12s %-8s %5d %-11s %10d %s",
                                     migration.getVersion(),
                                     migration.getState(),
                                     migration.getInstalledOn() == null ? null : dataFormat.format(migration.getInstalledOn()),
                                     migration.getInstalledBy(),
                                     migration.getType(),
                                     migration.getExecutionTime(),
                                     migration.getDescription(),
                                     migration.getChecksum(),
                                     migration.getScript());
                    LOGGER.info(formatter.toString());
                }
            }
        }
    }

    public void repair() {
        LOGGER.info("Repair migrations");
        final Flyway flyway = createFlyway(null);

        flyway.repair();
    }

    public void migrate() {
        LOGGER.info("Perform migration");
        final Flyway flyway = createFlyway(null);

        final int migrationCount = flyway.migrate().migrations.size();
        LOGGER.info("{} migrations applied", migrationCount);
    }

    public void clearMigrationLog() {
        LOGGER.info("Clear existing migration log");

        final DatabaseConfiguration dbConfiguration = configuration.getSelectedDatabaseConfiguration();

        try (final Connection connection = DriverManager.getConnection(dbConfiguration.getUrl(), dbConfiguration.getUsername(), dbConfiguration.getPassword());
             final Statement stmt = connection.createStatement()) {

            stmt.execute(join("DROP TABLE ", configuration.getMigrationLogTable().toUpperCase()));

        } catch (SQLException e) {
            LOGGER.error("Could not remove migration log table {}: {}", configuration.getMigrationLogTable(), e.getMessage());
        }
    }

    public void baseline() {
        LOGGER.info("Perform schema baseline");
        ExtendedFlyway flyway = createFlyway(null);

        MigrationInfo[] pending = flyway.info().pending();

        final String maxMigration;
        if (pending == null) {
            LOGGER.debug("No migration for baseline version detection available - assuming version 0");
            maxMigration = "0";
        } else {
            maxMigration = Stream.of(pending)
                                 .map(MigrationInfo::getVersion)
                                 .filter(Objects::nonNull)
                                 .max(naturalOrder())
                                 .map(MigrationVersion::toString)
                                 .orElse("0");
            LOGGER.debug("Max available migration version is {}", maxMigration);
        }

        MigrationVersion baselineVersion;
        switch (configuration.getInstall().getBaselineVersion()) {
            case BASELINE_VERSION_LATEST: {
                baselineVersion = MigrationVersion.fromVersion(maxMigration);
                break;
            }
            case BASELINE_VERSION_LATEST_MAJOR: {
                baselineVersion = MigrationVersion.fromVersion(truncateVersion(maxMigration, 1));
                break;
            }
            case BASELINE_VERSION_LATEST_MINOR: {
                baselineVersion = MigrationVersion.fromVersion(truncateVersion(maxMigration, 2));
                break;
            }
            default: {
                baselineVersion = MigrationVersion.fromVersion(configuration.getInstall().getBaselineVersion());
                break;
            }
        }

        // Recreate new flyway instance with baseline version
        flyway = createFlyway(baselineVersion);

        flyway.baseline();
        flyway.skipPendingMigrations();
    }

    private String truncateVersion(String version, int length) {

        if (length <= 0) {
            return "0";
        }

        int currentLength = 0;
        int nextDelimiter = StringUtils.indexOf(version, '.');
        while (nextDelimiter >= 0) {
            currentLength++;

            if (currentLength == length) {
                return StringUtils.substring(version, 0, nextDelimiter);
            }

            nextDelimiter = StringUtils.indexOf(version, '.', nextDelimiter + 1);
        }

        return version;
    }

    private ExtendedFlyway createFlyway(MigrationVersion baselineVersion) {
        final DatabaseConfiguration dbConfig = configuration.getSelectedDatabaseConfiguration();
        FluentConfiguration config = Flyway.configure(configuration.getClassloader());

        config.dataSource(dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());
        config.table(configuration.getMigrationLogTable());
        config.callbacks(new MigrationCallback(configuration,  schemaElements));
        config.locations(configuration.getMigration()
                                      .getMigrationScriptPaths()
                                      .toArray(new String[configuration.getMigration()
                                                                       .getMigrationScriptPaths()
                                                                       .size()]));

        config.encoding(configuration.getScriptEncoding());
        config.baselineDescription("Baseline");

        config.ignoreIgnoredMigrations(true);
        config.cleanOnValidationError(false);
        config.cleanDisabled(true);
        config.outOfOrder(true);

        if (baselineVersion != null) {
            config.baselineVersion(baselineVersion);
        }

        final ExtendedFlyway flyway = new ExtendedFlyway(config);
        return flyway;
    }

}
