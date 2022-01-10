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
package com.arvatosystems.t9t.schemaLoader.mavenPlugin;

import com.arvatosystems.t9t.schemaLoader.SchemaLoader;
import com.arvatosystems.t9t.schemaLoader.config.SchemaLoaderConfiguration;
import com.arvatosystems.t9t.schemaLoader.config.SchemaLoaderConfiguration.SchemaLoaderAction;
import com.arvatosystems.t9t.schemaLoader.mavenPlugin.config.Database;
import com.arvatosystems.t9t.schemaLoader.mavenPlugin.config.Installation;
import com.arvatosystems.t9t.schemaLoader.mavenPlugin.config.Migration;
import com.arvatosystems.t9t.schemaLoader.mavenPlugin.config.SqlArtifact;
import com.arvatosystems.t9t.schemaLoader.mavenPlugin.helper.SqlDependencyHelper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

public abstract class AbstractSchemaLoaderMojo extends AbstractMojo {

    /** Skip plugin execution */
    @Parameter(defaultValue = "false", property = "skipDbSetup")
    private boolean skipDbSetup;

    /** Database connection to use */
    @Parameter(alias = "database", required = true)
    private Database database;

    /** Use all project dependencies with 'sql' classifier */
    @Parameter(defaultValue = "false")
    private boolean useAllSqlDependencies;

    /** List of artifacts for filtering project dependencies to use */
    @Parameter
    private List<SqlArtifact> sqlArtifacts;

    /** Name of the DB table to log migrations */
    @Parameter(alias = "migration-log-table", required = true)
    private String migrationLogTable;

    /**
     * Location to search SQL scripts. Using the prefix 'classpath:' the location is searched
     * within the project dependencies, using the prefix 'filesystem:' the location is searched within the provided filesystem path.
     */
    @Parameter(defaultValue = "classpath:", alias = "script-location", required = true)
    private String scriptLocation;

    /** Encoding to read SQL scripts */
    @Parameter(defaultValue = "UTF-8", alias = "script-encoding", required = true)
    private String scriptEncoding;

    /** Configuration of migration settings */
    @Parameter(alias = "migration")
    private Migration migration;

    /** Configuration of installation settings */
    @Parameter(alias = "installation")
    private Installation installation;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Log log = getLog();

        // It should be possible to skip the execution by external configuration
        if (skipDbSetup) {
            log.info("Skipping execution of schema-loader-maven plugin (skipDbSetup=true).");
            return;
        }

        verifyParameters();

        final SqlDependencyHelper dependencyHelper = new SqlDependencyHelper(getLog());

        // Add the arguments for jar files
        final List<URL> jarList;
        if (useAllSqlDependencies) {
            log.info("Using all project dependencies with classifier " + SqlDependencyHelper.SQL_CLASSIFIER);
            jarList = dependencyHelper.getJarList(project);
        } else {
            log.info("Using project dependencies with classifier " + SqlDependencyHelper.SQL_CLASSIFIER + " specified in <sqlArtifacts>.");
            jarList = dependencyHelper.getJarList(project, sqlArtifacts);
        }

        final URLClassLoader classLoader = new URLClassLoader(jarList.toArray(new URL[jarList.size()]), AbstractSchemaLoaderMojo.class.getClassLoader());

        final SchemaLoaderConfiguration configuration = new SchemaLoaderConfiguration();
        configuration.setClassloader(classLoader);
        configuration.setScriptLocation(scriptLocation);
        configuration.setScriptEncoding(scriptEncoding);

        configuration.setMigrationLogTable(migrationLogTable);

        configuration.setDatabase("default");
        configuration.getDbConfig()
                     .put("default", database.toDatabaseConfiguration(classLoader));

        if (migration != null) {
            configuration.getMigration()
                         .getPreDrop()
                         .addAll(migration.getPreDrops());
            configuration.getMigration()
                         .getPostCreate()
                         .addAll(migration.getPostCreates());
        }

        if (installation != null) {
            configuration.getInstall()
                         .setBaselineVersion(installation.getBaselineVersion());
            configuration.getInstall()
                         .getCreate()
                         .addAll(installation.getCreates());
            configuration.getInstall()
                         .getDrop()
                         .addAll(installation.getDrops());
        }

        configuration.setAction(getAction());

        final SchemaLoader schemaLoader = new SchemaLoader(configuration);

        try {
            schemaLoader.executeAction();
        } catch (RuntimeException e) {
            final String message = "Error during execution of SchemaLoader.";
            log.error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    protected abstract SchemaLoaderAction getAction();

    protected void verifyParameters() throws MojoExecutionException {
        if (database == null) {
            throw new MojoExecutionException("<database> element is missing in configuration.");
        }

        database.verifyParameters();

        final boolean scriptFromClasspath = startsWithIgnoreCase(scriptLocation, "classpath:");

        if (scriptFromClasspath) {
            if (useAllSqlDependencies) {
                if (sqlArtifacts != null && !sqlArtifacts.isEmpty()) {
                    throw new MojoExecutionException("If useAllSqlDependencies=true, you may not specify elements in <sqlArtifacts>.");
                }
            } else {
                if (sqlArtifacts == null || sqlArtifacts.isEmpty()) {
                    throw new MojoExecutionException("If scriptLocation is from classpath and useAllSqlDependencies=false, you must specify elements in <sqlArtifacts>.");
                }
            }
        } else {
            if (useAllSqlDependencies) {
                throw new MojoExecutionException("If scriptLocation is not from classpath, you may not specify useAllSqlDependencies=true");
            }
            if (sqlArtifacts != null && !sqlArtifacts.isEmpty()) {
                throw new MojoExecutionException("If scriptLocation is not from classpath, you may not specify elements in <sqlArtifacts>.");
            }
        }
    }
}
