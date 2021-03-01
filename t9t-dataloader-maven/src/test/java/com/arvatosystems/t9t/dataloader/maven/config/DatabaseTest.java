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
package com.arvatosystems.t9t.dataloader.maven.config;


import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DatabaseTest {

    private Database database;

    @Before
    public void init() {
        database = new Database();
        database.jdbcUrl = "jdbc:something/myDatabase";
        database.user = "user";
        database.password = "password";
    }

    @Test(expected = MojoExecutionException.class)
    public void emptyJdbcUrl() throws MojoExecutionException {
        database.jdbcUrl = null;
        database.verifyParameters();
    }

    @Test(expected = MojoExecutionException.class)
    public void incorrectJdbcUrl() throws MojoExecutionException {
        database.jdbcUrl = "incorrect";
        database.verifyParameters();
    }

    @Test
    public void correctJdbcUrl() throws MojoExecutionException {
        database.verifyParameters();
        Assert.assertEquals("myDatabase", database.getDatabase());
    }

    @Test(expected = MojoExecutionException.class)
    public void emptyUser() throws MojoExecutionException {
        database.user = null;
        database.verifyParameters();
    }

    @Test(expected = MojoExecutionException.class)
    public void emptyPassword() throws MojoExecutionException {
        database.password = null;
        database.verifyParameters();
    }

}
