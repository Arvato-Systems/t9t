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
package com.arvatosystems.t9t.dataloader.maven.util;

import com.arvatosystems.t9t.dataloader.maven.config.Database;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class DbSetupHelper {

    private Log log;
    private Database database;
    private String projectBuildDir;

    private File workDir;
    private String loadOnlyFromElements;

    private String projectFilterIncludingOrder;

    public DbSetupHelper(Log log, Database database, String projectBuildDir) {
        this.log = log;
        this.database = database;
        this.projectBuildDir = projectBuildDir;
    }

    public void createWorkDir() throws MojoExecutionException {
        File targetFolder = new File(projectBuildDir);
        File workDir = new File(targetFolder.getPath() + File.separator + "dataloader");
        workDir.mkdir();
        if (!workDir.exists()) {
            throw new MojoExecutionException("Could not create directory target/dataloader");
        }
        this.workDir = workDir;
    }

    public List<String> getArguments() throws MojoExecutionException {
        List<String> args = new LinkedList<>();
        addDbConfigArgument(args);
        addTargetDatabaseArgument(args);
        addDbTypeArgument(args);
        addWorkdirArgument(args);
        addLoadOnlyFromElementsArgument(args);
        addProjectFilterIncludingOrderArgument(args);
        return args;
    }

    /**
     * Adds the parameter for "loadOnlyFromElements"
     */
    protected void addLoadOnlyFromElementsArgument(List<String> args) {
        if (loadOnlyFromElements != null) {
            if (!StringUtils.isEmpty(loadOnlyFromElements)) {
                args.add("-loadOnlyFromElements");
                args.add(loadOnlyFromElements);
            }
        }
    }

    /**
     * Adds the parameter for "projectFilterIncludingOrder"
     */
    protected void addProjectFilterIncludingOrderArgument(List<String> args) {
        if (projectFilterIncludingOrder != null) {
            if (!StringUtils.isEmpty(projectFilterIncludingOrder)) {
                args.add("-projectFilterIncludingOrder");
                args.add(projectFilterIncludingOrder);
            }
        }
    }

    /**
     * Adds the parameter for DB config
     */
    protected void addDbConfigArgument(List<String> args) throws MojoExecutionException {
        args.add("-dbUrl");
        args.add(database.getJdbcUrl());

        args.add("-dbUser");
        args.add(database.getUser());

        args.add("-dbPassword");
        args.add(database.getPassword());
    }

    /**
     * Adds the parameter for "targetDatabase"
     */
    protected void addTargetDatabaseArgument(List<String> args) {
        args.add("-targetDatabase");
        args.add(database.getDatabase());
    }

    private static final String JDBC_URL_COMPONENT_POSTGRES = "jdbc:postgresql";
    private static final String JDBC_URL_COMPONENT_ORACLE = "jdbc:oracle";
    private static final String JDBC_URL_COMPONENT_MSSQL = "jdbc:jtds:sqlserver";
    private static final String LOADER_PARAM_POSTGRES = "POSTGRES";
    private static final String LOADER_PARAM_ORACLE = "ORACLE";
    private static final String LOADER_PARAM_MSSQL = "MSSQLSERVER";
    private static final String JDBC_DRIVER_POSTGRES = "org.postgresql.Driver";
    private static final String JDBC_DRIVER_ORACLE = "oracle.jdbc.OracleDriver";
    private static final String JDBC_DRIVER_MSSQL = "net.sourceforge.jtds.jdbc.Driver";

    /**
     * Adds the parameter for "dbType"
     */
    protected void addDbTypeArgument(List<String> args) throws MojoExecutionException {
        args.add("-dbType");
        String jdbcUrl = database.getJdbcUrl();
        if (jdbcUrl.contains(JDBC_URL_COMPONENT_POSTGRES)) {
            args.add(LOADER_PARAM_POSTGRES);
        } else if (jdbcUrl.contains(JDBC_URL_COMPONENT_ORACLE)) {
            args.add(LOADER_PARAM_ORACLE);
        } else if (jdbcUrl.contains(JDBC_URL_COMPONENT_MSSQL)) {
            args.add(LOADER_PARAM_MSSQL);
        } else {
            throw new MojoExecutionException("Unknown database type in jdbcUrl: " + jdbcUrl);
        }
    }

    /**
     * Adds the parameter for "workdir"
     */
    protected void addWorkdirArgument(List<String> args) throws MojoExecutionException {
        args.add("-workdir");
        args.add(workDir.getPath());
    }

    public String getLoadOnlyFromElements() {
        return loadOnlyFromElements;
    }

    public void setLoadOnlyFromElements(String loadOnlyFromElements) {
        this.loadOnlyFromElements = loadOnlyFromElements;
    }

    public String getProjectFilterIncludingOrder() {
        return projectFilterIncludingOrder;
    }

    public void setProjectFilterIncludingOrder(String projectFilterIncludingOrder) {
        this.projectFilterIncludingOrder = projectFilterIncludingOrder;
    }

}
