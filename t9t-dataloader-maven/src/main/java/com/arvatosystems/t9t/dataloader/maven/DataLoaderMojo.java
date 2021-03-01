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
package com.arvatosystems.t9t.dataloader.maven;

import com.arvatosystems.t9t.dataloader.maven.config.Database;
import com.arvatosystems.t9t.dataloader.maven.config.SqlArtifact;
import com.arvatosystems.t9t.dataloader.maven.util.DbSetupHelper;
import com.arvatosystems.t9t.dataloader.maven.util.SqlDependencyHelper;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.arvatosystems.t9t.dataloader.DbSetup;

/**
 * Maven plugin that wraps the dataloader (@link {@link DbSetup}).
 *
 * @author Franz Becker
 *
 */
@Mojo(name = "setup-db")
public class DataLoaderMojo extends AbstractMojo {

    @Parameter(defaultValue = "false", property = "skipDbSetup")
    private boolean skipDbSetup;

    @Parameter(property = "loadOnlyFromElements")
    private String loadOnlyFromElements;

    @Parameter(property = "projectFilterIncludingOrder")
    private String projectFilterIncludingOrder;

    @Parameter(alias = "database", required = true)
    private Database database;

    @Parameter(required = true)
    private List<String> args;

    @Parameter(defaultValue = "${project.build.directory}")
    private String projectBuildDir;

    @Component
    private MavenProject project;

    @Parameter(defaultValue = "true")
    private boolean useAllSqlDependencies;

    @Parameter
    private List<SqlArtifact> sqlArtifacts;

    /**
     * Helps to abstract from the ulgyness of {@link DbSetup}.
     */
    private DbSetupHelper dbSetupHelper;

    private SqlDependencyHelper dependencyHelper;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Log log = getLog();

        // It should be possible to skip the execution by external configuration
        if (skipDbSetup) {
            log.info("Skipping execution of t9t-dataloader-maven plugin (skipDbSetup=true).");
            return;
        }

        // remove null args since they would later cause problems
        args.remove(null);

        log.info("Executing t9t-dataloader-maven plugin.");
        initHelpers();
        verifyParameters();

        // Create the workDir
        dbSetupHelper.createWorkDir();

        // Get the arguments from the parameters
        List<String> arguments = dbSetupHelper.getArguments();
        arguments.addAll(args);

        // Add the arguments for jar files
        if (useAllSqlDependencies) {
            log.info("Using all project dependencies with classifier " + SqlDependencyHelper.SQL_CLASSIFIER);
            arguments.addAll(dependencyHelper.getJarList(project));
        } else {
            log.info("Using project dependencies with classifier " + SqlDependencyHelper.SQL_CLASSIFIER + " specified in <sqlArtifacts>.");
            arguments.addAll(dependencyHelper.getJarList(project, sqlArtifacts));
        }

        log.info(buildConfigurationLog(arguments));

        // Execute dataloader
        try {
            log.info("Calling DbSetup...");
            log.info("... with arguments: " + Arrays.toString(arguments.toArray(new String[0])));
            DbSetup.main(arguments.toArray(new String[0]));
        } catch (Exception e) {
            String message = "Error during execution of DbSetup.";
            log.error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    protected void initHelpers() {
        dbSetupHelper = new DbSetupHelper(getLog(), database, projectBuildDir);
        dbSetupHelper.setLoadOnlyFromElements(loadOnlyFromElements);
        dbSetupHelper.setProjectFilterIncludingOrder(projectFilterIncludingOrder);
        dependencyHelper = new SqlDependencyHelper(getLog());
    }

    /**
     * Verifies the parameters... somehow Maven does not do this correctly?!
     *
     * @throws MojoExecutionException
     */
    protected void verifyParameters() throws MojoExecutionException {
        if (database == null) {
            throw new MojoExecutionException("<database> element is missing in configuration.");
        }
        database.verifyParameters();
        if (args == null) {
            throw new MojoExecutionException("<args> element is missing in configuration.");
        }
        if (args.isEmpty()) {
            throw new MojoExecutionException("<args> may not be empty.");
        }
        for (String arg : args) {
            if (arg.contains(" ")) {
                throw new MojoExecutionException("Argument '" + arg
                        + "' contained whitespace, not allowed! Please put every argument in a single <param> element.");
            }
        }
        if (useAllSqlDependencies) {
            if (sqlArtifacts != null && !sqlArtifacts.isEmpty()) {
                throw new MojoExecutionException("If useAllSqlDependencies=true, you may not specify elements in <sqlArtifacts>.");
            }
        } else {
            if (sqlArtifacts == null || sqlArtifacts.isEmpty()) {
                throw new MojoExecutionException("If useAllSqlDependencies=false, you must specify elements in <sqlArtifacts>.");
            }
        }
    }

    protected String buildConfigurationLog(List<String> arguments) {
        final String spaces = "    ";
        StringBuilder builder = new StringBuilder();
        builder.append("Configuration:\n");
        builder.append(spaces).append(database.toString()).append("\n");
        builder.append(spaces).append("args [").append("\n");
        for (String arg : arguments) {
            builder.append(spaces).append(spaces).append(arg).append("\n");
        }
        builder.append(spaces).append("]\n");
        builder.append(spaces).append("Load for elements: ").append(loadOnlyFromElements);
        builder.append(spaces).append("\n");
        builder.append(spaces).append("Project filter including order to load ").append(projectFilterIncludingOrder);
        return builder.toString();
    }

}
