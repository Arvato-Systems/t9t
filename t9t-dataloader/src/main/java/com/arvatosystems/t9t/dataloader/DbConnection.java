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
package com.arvatosystems.t9t.dataloader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbConnection {

    private static Logger logger = LoggerFactory.getLogger(DbConnection.class);

    private String dbIdentifier;
    private Connection connection;

    public DbConnection(String dbIdentifier) {
        this.dbIdentifier = dbIdentifier;
    }

    /**
     * @return the connected
     * @throws SQLException exception
     */
    public final boolean isConnected() throws SQLException {
        return (connection != null && !connection.isClosed());
    }

    /**
     * Attempts to establish a connection.
     *
     * @throws SQLException exception
     */
    private void openConnection() throws SQLException {
        String drivername = Configurator.getValue(dbIdentifier + ".drivername", "no.driver.set");
        try {
            Class.forName(drivername);
        } catch (ClassNotFoundException cnfe) {
            String logString = "driver class [" + Configurator.getValue(dbIdentifier + ".drivername") + "] for datasource [" + dbIdentifier + "] not found! ";
            logger.error(logString, cnfe);
            throw new RuntimeException(logString + cnfe.getMessage());
        }
        String connectURI = Configurator.getValue(dbIdentifier + ".uri");
        Properties properties = Configurator.getAsProperties(dbIdentifier + ".connectionProperties", new Properties());
        Set<Entry<Object, Object>> set = properties.entrySet();

        // // Class.forName(driver);
        // DriverManager.registerDriver((Driver)
        // Class.forName(driver).newInstance());
        // connection = DriverManager.getConnection(url, properties);
        connection = DriverManager.getConnection(connectURI, properties);

        if (connectURI.contains("postgres")) {
            if (properties.containsKey("schema")) {
                logger.debug("Use schema: ".concat(properties.getProperty("schema")));
                connection.createStatement().execute("SET SCHEMA '"+ properties.getProperty("schema") + "'");
            }
        } else if (connectURI.contains("oracle")) {
            if (properties.containsKey("schema")) {
                logger.debug("Use schema: ".concat(properties.getProperty("schema")));
                connection.createStatement().execute("alter session set current_schema="+properties.getProperty("schema"));
            }
        }

    }

    /**
     * @return the openConnection
     * @throws SQLException exception
     */
    public final Connection getOpenConnection() throws SQLException {
        if (!isConnected()) {
            openConnection();
        }
        return connection;
    }

    /**
     * Makes all changes made since the previous commit/rollback permanent and releases any database locks currently held by this Connection object.
     *
     * @throws SQLException exception
     */
    public final void commitConnection() throws SQLException {
        if (isConnected() && !connection.getAutoCommit()) {
            connection.commit();
        }
    }

    /**
     * Undoes all changes made in the current transaction and releases any database locks currently held by this Connection object.
     *
     * @throws SQLException exception
     */
    public final void rollbackConnection() throws SQLException {
        if (isConnected() && !connection.getAutoCommit()) {
            connection.rollback();
        }
    }

    /**
     * Releases this Connection object's database.
     *
     * @throws SQLException exception
     */
    public final void closeConnection() throws SQLException {
        commitConnection();
        if (isConnected()) {
            connection.close();
        }
    }

    /**
     * @return the autoCommit
     * @throws SQLException exception
     */
    public final boolean isAutoCommit() throws SQLException {
        return (isConnected()) ? connection.getAutoCommit() : false;
    }

    /**
     * @param autoCommit the autoCommit to set
     * @throws SQLException exception
     */
    public final void setAutoCommit(final boolean autoCommit) throws SQLException {
        if (isConnected()) {
            connection.setAutoCommit(autoCommit);
        }
    }

}
