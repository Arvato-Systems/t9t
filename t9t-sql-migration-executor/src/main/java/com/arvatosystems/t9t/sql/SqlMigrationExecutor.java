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
package com.arvatosystems.t9t.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.Flyway;
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

/** Runs an SQL migration, if requested by command line parameter (or system property). */
@Startup(67)
public class SqlMigrationExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlMigrationExecutor.class);

    public static Flyway configureFlywayForDatabase(String locations, String migrationTable, RelationalDatabaseConfiguration dbConfiguration) {
        Flyway flyway = new Flyway();
        flyway.setLocations(locations);
        flyway.setCleanDisabled(true);
        flyway.setCleanOnValidationError(false);
        flyway.setOutOfOrder(true);
        flyway.setEncoding("UTF-8");
        flyway.setTable(migrationTable);
        flyway.setInstalledBy("admin");
        flyway.setIgnoreMissingMigrations(true);
        flyway.setBaselineOnMigrate(true);
        flyway.setDataSource(dbConfiguration.getJdbcConnectString(), dbConfiguration.getUsername(), dbConfiguration.getPassword());

        return flyway;
    }

    public static void migrate() throws IOException {
        final T9tServerConfiguration serverConfiguration = ConfigProvider.getConfiguration();
        final RelationalDatabaseConfiguration dbConfiguration = serverConfiguration.getDatabaseConfiguration();
        final List<String> migrations = dbConfiguration.getMigrations();

        for (String migration : migrations) {
            String[] locationsAndMigrationTable = migration.split("=");

            // migrate
            Flyway flyway = configureFlywayForDatabase(locationsAndMigrationTable[0], locationsAndMigrationTable[1], dbConfiguration);

            LOGGER.info("Trying to migrate database based on following information: " + migration);
            flyway.migrate();
        }
    }

    // startup entry point - invoked during Jdp initialization - the system is not yet up
    public static void onStartup() {
        if (System.getProperty(T9tConstants.START_MIGRATION_PROPERTY) != null) {
            LOGGER.info("Database migration requested - staring flyway");
            try {
                migrate();
            } catch (IOException e) {
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
    public static void main(String[] args) {
        try {
            getServerConfigurationFromCommandLine(args);
            SqlMigrationExecutor.migrate();
        } catch (JSAPException e) {
            LOGGER.error("Could not parse command line - aborting");
            System.exit(1);
        } catch (IOException e) {
            LOGGER.error("SQL migration failed.", e);
            System.exit(1);
        }
    }

    /**
     * Retrieves T9tServerConfiguration from command line.
     * @param args Same to vertex server argument the T9tServerConfiguration file path needs to be provided with the --cfg option.
     */
    public static void getServerConfigurationFromCommandLine(String[] args) throws JSAPException {
        ArrayList<Parameter> options = new ArrayList<Parameter>();
        options.add(new FlaggedOption("cfg",      JSAP.STRING_PARSER,  null,                     JSAP.NOT_REQUIRED, 'c', "cfg",      "configuration filename"));

        Parameter[] optionsArray = new Parameter[options.size()];
        System.arraycopy(options.toArray(), 0, optionsArray, 0, optionsArray.length);

        SimpleJSAP commandLineOptions = new SimpleJSAP("t9t DB migrator", "Runs database schema upgrades", optionsArray);
        JSAPResult cmd = commandLineOptions.parse(args);
        if (commandLineOptions.messagePrinted()) {
            System.err.println("(use option --help for usage)");
            System.exit(1);
        }

        ConfigProvider.readConfiguration(cmd.getString("cfg"));
    }
}
