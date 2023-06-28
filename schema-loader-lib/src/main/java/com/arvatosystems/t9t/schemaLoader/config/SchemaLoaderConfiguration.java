/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.schemaLoader.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration root for the schema loader.
 */
public class SchemaLoaderConfiguration {

    /**
     * Location to search SQL scripts. Using the prefix 'classpath:' the location is searched
     * within the classpath, using the prefix 'file:' the location is searched within the provided filesystem path.
     */
    private String scriptLocation = "classpath:";

    /** Encoding to read SQL scripts */
    private String scriptEncoding = "UTF-8";

    /** Classloader to use for JDBC driver and script lookup */
    private ClassLoader classloader = SchemaLoaderConfiguration.class.getClassLoader();

    /** Name of the DB table to log migrations */
    private String migrationLogTable = "fw_migration";

    /** Name of database configuration to use */
    private String database = "default";

    /** Map of database name to available database configuration */
    private final Map<String, DatabaseConfiguration> dbConfig = new HashMap<>();

    private final InstallConfiguration install = new InstallConfiguration();
    private final MigrationConfiguration migration = new MigrationConfiguration();

    /** Action to perform */
    private SchemaLoaderAction action = SchemaLoaderAction.INFO;

    public static enum SchemaLoaderAction {
        INSTALL, MIGRATE, REPAIR, INFO, BASELINE;
    }

    // Convenience Getter/Setter

    public DatabaseConfiguration getSelectedDatabaseConfiguration() {
        final DatabaseConfiguration cfg = dbConfig.get(database);
        if (cfg == null) {
            throw new IllegalArgumentException("No database configuration available for " + database);
        }
        return cfg;
    }

    // Regular Getter/Setter

    public String getScriptLocation() {
        return scriptLocation;
    }

    public void setScriptLocation(String scriptLocation) {
        this.scriptLocation = scriptLocation;
    }

    public String getScriptEncoding() {
        return scriptEncoding;
    }

    public void setScriptEncoding(String scriptEncoding) {
        this.scriptEncoding = scriptEncoding;
    }

    public ClassLoader getClassloader() {
        return classloader;
    }

    public void setClassloader(ClassLoader classloader) {
        this.classloader = classloader;
    }

    public String getMigrationLogTable() {
        return migrationLogTable;
    }

    public void setMigrationLogTable(String migrationLogTable) {
        this.migrationLogTable = migrationLogTable;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public SchemaLoaderAction getAction() {
        return action;
    }

    public void setAction(SchemaLoaderAction action) {
        this.action = action;
    }

    public Map<String, DatabaseConfiguration> getDbConfig() {
        return dbConfig;
    }

    public InstallConfiguration getInstall() {
        return install;
    }

    public MigrationConfiguration getMigration() {
        return migration;
    }

    @Override
    public String toString() {
        return "SchemaLoaderConfiguration [scriptLocation=" + scriptLocation + ", scriptEncoding=" + scriptEncoding + ", classloader=" + classloader
               + ", migrationLogTable=" + migrationLogTable + ", database=" + database + ", dbConfig=" + dbConfig + ", install=" + install + ", migration="
               + migration + ", action=" + action + "]";
    }

}
