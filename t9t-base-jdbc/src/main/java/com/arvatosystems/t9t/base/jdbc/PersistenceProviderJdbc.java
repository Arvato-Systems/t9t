package com.arvatosystems.t9t.base.jdbc;

import java.sql.Connection;

import de.jpaw.bonaparte.refs.PersistenceProvider;

public interface PersistenceProviderJdbc extends PersistenceProvider {
    Connection getConnection();
}
