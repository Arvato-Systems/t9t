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
package com.arvatosystems.t9t.dataloader.maven.config;


import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatabaseTest {

    private Database database;

    @BeforeEach
    public void init() {
        database = new Database();
        database.jdbcUrl = "jdbc:something/myDatabase";
        database.user = "user";
        database.password = "password";
    }

    @Test
    public void emptyJdbcUrl() throws MojoExecutionException {
        Assertions.assertThrows(MojoExecutionException.class, () -> {
            database.jdbcUrl = null;
            database.verifyParameters();
        });
    }

    @Test
    public void incorrectJdbcUrl() throws MojoExecutionException {
        Assertions.assertThrows(MojoExecutionException.class, () -> {
            database.jdbcUrl = "incorrect";
            database.verifyParameters();
        });
    }

    @Test
    public void correctJdbcUrl() throws MojoExecutionException {
        database.verifyParameters();
        Assertions.assertEquals("myDatabase", database.getDatabase());
    }

    @Test
    public void emptyUser() throws MojoExecutionException {
        Assertions.assertThrows(MojoExecutionException.class, () -> {
            database.user = null;
            database.verifyParameters();
        });
    }

    @Test
    public void emptyPassword() throws MojoExecutionException {
        Assertions.assertThrows(MojoExecutionException.class, () -> {
            database.password = null;
            database.verifyParameters();
        });
    }

}
