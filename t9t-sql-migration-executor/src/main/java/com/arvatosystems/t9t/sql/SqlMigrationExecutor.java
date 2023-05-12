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
package com.arvatosystems.t9t.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;

import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;

/** Runs an SQL migration, if requested by command line parameter (or system property). */
@Startup(67)
public class SqlMigrationExecutor implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlMigrationExecutor.class);

    public static Flyway configureFlywayForDatabase(final String locations, final String migrationTable,
      final RelationalDatabaseConfiguration dbConfiguration) {

        final FluentConfiguration config = Flyway.configure();
        config.locations(locations);
        config.cleanDisabled(true);
        config.cleanOnValidationError(false);
        config.outOfOrder(true);
        config.encoding("UTF-8");
        config.table(migrationTable);
        config.installedBy("admin");
        config.ignoreMigrationPatterns("*:Missing");
        config.baselineOnMigrate(true);
        config.dataSource(dbConfiguration.getJdbcConnectString(), dbConfiguration.getUsername(), dbConfiguration.getPassword());
        config.loggers("slf4j");

        final Flyway flyway = new Flyway(config);
        return flyway;
    }

    public static void migrate() throws IOException {
        final T9tServerConfiguration serverConfiguration = ConfigProvider.getConfiguration();

        final RelationalDatabaseConfiguration dbConfiguration = serverConfiguration.getDatabaseConfiguration();
        migrateDatabase(dbConfiguration);

        final RelationalDatabaseConfiguration secondaryDbConfiguration = serverConfiguration.getSecondaryDatabaseConfig();
        if (secondaryDbConfiguration != null)
            migrateDatabase(secondaryDbConfiguration);
    }

    private static void migrateDatabase(final RelationalDatabaseConfiguration dbConfiguration) {
        final List<String> migrations = dbConfiguration.getMigrations();

        if (migrations == null) {
            LOGGER.info("No migration files found for: " + dbConfiguration.getJdbcConnectString());
            return;
        }

        for (final String migration : migrations) {
            final String[] locationsAndMigrationTable = migration.split("=");

            // migrate
            final Flyway flyway = configureFlywayForDatabase(locationsAndMigrationTable[0], locationsAndMigrationTable[1], dbConfiguration);

            LOGGER.info("Trying to migrate database " +  dbConfiguration.getJdbcConnectString() + " based on following information: " + migration);
            flyway.migrate();
        }
    }

    // startup entry point - invoked during Jdp initialization - the system is not yet up
    @Override
    public void onStartup() {
        if (System.getProperty(T9tConstants.START_MIGRATION_PROPERTY) != null) {
            LOGGER.info("Database migration requested - staring flyway");
            try {
                migrate();
            } catch (final IOException e) {
                LOGGER.error("Flyway migration failed - FATAL, HARD STOP!", e);
                // Die! In case of such a hard error, do not continue
                System.exit(1);
            }
        }
    }

    /**
     * Main entry for executable jar
     * @param args
     */
    public static void main(final String[] args) {
        try {
            getServerConfigurationFromCommandLine(args);
            SqlMigrationExecutor.migrate();
        } catch (final JSAPException e) {
            LOGGER.error("Could not parse command line - aborting");
            System.exit(1);
        } catch (final IOException e) {
            LOGGER.error("SQL migration failed.", e);
            System.exit(1);
        }
    }

    /**
     * Retrieves T9tServerConfiguration from command line.
     * @param args Same to vertex server argument the T9tServerConfiguration file path needs to be provided with the --cfg option.
     */
    public static void getServerConfigurationFromCommandLine(final String[] args) throws JSAPException {
        final ArrayList<Parameter> options = new ArrayList<>();
        options.add(new FlaggedOption("cfg",      JSAP.STRING_PARSER,  null,                     JSAP.NOT_REQUIRED, 'c', "cfg",      "configuration filename"));

        final Parameter[] optionsArray = new Parameter[options.size()];
        System.arraycopy(options.toArray(), 0, optionsArray, 0, optionsArray.length);

        final SimpleJSAP commandLineOptions = new SimpleJSAP("t9t DB migrator", "Runs database schema upgrades", optionsArray);
        final JSAPResult cmd = commandLineOptions.parse(args);
        if (commandLineOptions.messagePrinted()) {
            System.err.println("(use option --help for usage)");
            System.exit(1);
        }

        ConfigProvider.readConfiguration(cmd.getString("cfg"));
    }
}
