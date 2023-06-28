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
package com.arvatosystems.t9t.schemaLoader.mavenPlugin.config;

import com.arvatosystems.t9t.schemaLoader.config.DatabaseConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class Database {

    /** JDBC url for database connection */
    @Parameter(alias = "jdbc-url", required = true)
    protected String jdbcUrl;

    /** Username for connection */
    @Parameter(required = true)
    protected String user;

    /** Password for connection */
    @Parameter(required = true)
    protected String password;

    /** JDBC driver class to use (driver must be included as dependency for plugin execution) */
    @Parameter(alias = "driver-class", required = true)
    protected String driverClass;

    /** Database type */
    @Parameter(alias = "db-type", required = true)
    protected String dbType;

    public void verifyParameters() throws MojoExecutionException {
        if (isEmpty(jdbcUrl)) {
            mayNotBeEmpty("jdbcUrl");
        } else {
            if (jdbcUrl.split("/").length <= 1) {
                throw new MojoExecutionException("Incorrect jdbcUrl! Need last fragment to determine database.");
            }
        }
        if (isEmpty(user)) {
            mayNotBeEmpty("user");
        }
        if (isEmpty(password)) {
            mayNotBeEmpty("password");
        }
    }

    private void mayNotBeEmpty(String attribute) throws MojoExecutionException {
        throw new MojoExecutionException("The <database> attribute " + attribute + " must be set!");
    }


    public DatabaseConfiguration toDatabaseConfiguration(ClassLoader classLoader) throws MojoExecutionException {
        verifyParameters();

        try {
            final DatabaseConfiguration cfg = new DatabaseConfiguration();

            cfg.setUrl(jdbcUrl);
            cfg.setUsername(user);
            cfg.setPassword(password);
            cfg.setDriverClass((Class)classLoader.loadClass(driverClass));
            cfg.setType(dbType);

            return cfg;
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Driver class not found: " + driverClass);
        }
    }

    @Override
    public String toString() {
        return "Database [jdbcUrl=" + jdbcUrl + ", dbType=" + dbType + ", user=" + user + ", driverClass=" + driverClass + "]";
    }

}
