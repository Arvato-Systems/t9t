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
package com.arvatosystems.t9t.dataloader.maven.config;

import static org.codehaus.plexus.util.StringUtils.isEmpty;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

public class Database {

    @Parameter(alias = "jdbc-url", required = true)
    protected String jdbcUrl;

    @Parameter(required = true)
    protected String user;

    @Parameter(required = true)
    protected String password;

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

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getDatabase() {
        String[] split = jdbcUrl.split("/");
        return split[split.length - 1];
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Database [jdbcUrl=" + jdbcUrl + ", database=" + getDatabase() + ", user=" + user + "]";
    }

}
