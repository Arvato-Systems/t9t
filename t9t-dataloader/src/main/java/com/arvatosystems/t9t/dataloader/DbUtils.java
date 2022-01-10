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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbUtils {

    static final Logger logger = LoggerFactory.getLogger(DbUtils.class);

    private static final String JDBC_URL_COMPONENT_POSTGRES = "jdbc:postgresql";
    private static final String JDBC_URL_COMPONENT_ORACLE = "jdbc:oracle";
    private static final String JDBC_URL_COMPONENT_MSSQL = "jdbc:jtds:sqlserver";
    private static final String JDBC_DRIVER_POSTGRES = "org.postgresql.Driver";
    private static final String JDBC_DRIVER_ORACLE = "oracle.jdbc.OracleDriver";
    private static final String JDBC_DRIVER_MSSQL = "net.sourceforge.jtds.jdbc.Driver";

    public static String getSqlInsert(String tableName, Set<String> realColumns, String[] columnNames, Properties defaultColumns) throws Exception {
        StringBuilder query = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder values = new StringBuilder(") VALUES (");

        // set the default columns
        for (Enumeration<?> en = defaultColumns.propertyNames(); en.hasMoreElements();) {
            String key = (String) en.nextElement();
            if (!realColumns.contains(key)) {
                logger.warn("Config: Configured column {} not found in DB table", key);
                continue;
            }

            query.append(key).append(",");
            String value = defaultColumns.getProperty(key);
            if (value.startsWith("@RAW")) {
                // remove @RAM and leaf it as it is
                value = value.replaceFirst("@RAW:", "");
            } else {
                // declare as a bind variable
                value = "?";
            }
            values.append(value).append(",");
        }

        // set columns and bind variables
        for (String column : columnNames) {
            if (realColumns.toString().contains(column)) {
                query.append(column).append(",");
                values.append("?,");
            } else {
                logger.error("Config: Configured column {} not found in DB table", column);
                throw new Exception("Config: Configured column not found in DB table");
            }
        }

        query.deleteCharAt(query.length() - 1); // cut last ,
        values.deleteCharAt(values.length() - 1); // cut last ,

        return query.append(values).append(")").toString(); // query + values
    }

    public static Map<String, Integer> getDbTypesForTable(DbConnection dbConnection, String tableName) throws SQLException {
        HashMap<String, Integer> types = new LinkedHashMap<String, Integer>();

        try (Statement st = dbConnection.getOpenConnection().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM " + tableName);

            ResultSetMetaData rsMetaData = rs.getMetaData();

            int numberOfColumns = rsMetaData.getColumnCount();

            for (int i = 1; i <= numberOfColumns; i++) {
                String columnName = Util.isBlank(rsMetaData.getColumnName(i)) ? rsMetaData.getColumnLabel(i) : rsMetaData.getColumnName(i);
                types.put(columnName.toLowerCase(), rsMetaData.getColumnType(i));
            }
        }
        dbConnection.closeConnection();

        return types;
    }

    public static Set<String> getSelectColumnsWithoutExclued(DbConnection dbConnection, String tableName, Set<?> excludedDefaultColumns) throws SQLException {
        Map<String, Integer> types = getDbTypesForTable(dbConnection, tableName);
        Set<String> allColumns = types.keySet();
        return Util.minus(allColumns, Util.setFromIterator(excludedDefaultColumns.iterator()));
    }

    public static String getSqlSelect(DbConnection dbConnection, String tableName, String where, String orderBy, Set<String> selectColumns) throws SQLException {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ").append(Util.setToString(selectColumns)).append(" FROM ").append(tableName);
        if (where != null)
            sql.append(" WHERE ").append(where);
        if (orderBy != null)
            sql.append(" ORDER BY ").append(orderBy);

        // logger.debug("SELECT: Using statement: {}", sql);

        return sql.toString();
    }

    public static void deleteOldData(DbConnection dbConnection, String tableName, String where) throws SQLException {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM ").append(tableName);
        if (where != null)
            sql.append(" WHERE ").append(where);

        logger.debug("DELETE: Using statement: {}", sql);

        try (Statement statement = dbConnection.getOpenConnection().createStatement()) {
            int updateQuery = statement.executeUpdate(sql.toString());

            if (updateQuery != 0) {
                logger.debug("DELETE: " + updateQuery + " rows are deleted.");
            } else {
                logger.debug("DELETE: NO rows are deleted.");
            }
        }
    }

    public static void executeBatch(DbConnection dbConnection, PreparedStatement preparedStatement) throws SQLException {
        try {
            preparedStatement.addBatch();
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            logger.error("SQLException.getNextException: {}", e.getNextException() != null ? e.getNextException().getMessage() : null);
            throw e;
        }
        if (!dbConnection.isAutoCommit() /* && (r % ncommit.intValue() == 0) */) {
            dbConnection.commitConnection();
        }
    }

    public static String getDbDriver(String jdbcUrl) {
        if (jdbcUrl.contains(JDBC_URL_COMPONENT_POSTGRES)) {
            return JDBC_DRIVER_POSTGRES;
        } else if (jdbcUrl.contains(JDBC_URL_COMPONENT_ORACLE)) {
            return JDBC_DRIVER_ORACLE;
        } else if (jdbcUrl.contains(JDBC_URL_COMPONENT_MSSQL)) {
            return JDBC_DRIVER_MSSQL;
        } else {
            throw new IllegalArgumentException("Unknown database type in jdbcUrl: " + jdbcUrl);
        }
    }
}
