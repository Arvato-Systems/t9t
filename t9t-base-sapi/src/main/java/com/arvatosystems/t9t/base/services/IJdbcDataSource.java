package com.arvatosystems.t9t.base.services;

import java.sql.Connection;
import java.sql.SQLException;

public interface IJdbcDataSource {
    Connection getConnection() throws SQLException;
}
