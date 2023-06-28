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
package com.arvatosystems.t9t.base.jdbc.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IPersistenceProviderJdbc;
import com.arvatosystems.t9t.base.services.ISingleRefGenerator;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;
import jakarta.annotation.Nonnull;

@Singleton
@Named("JDBC2")
public class LazyJdbc2SequenceBasedSingleRefGenerator implements ISingleRefGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LazyJdbc2SequenceBasedSingleRefGenerator.class);

    private final Provider<IPersistenceProviderJdbc> jdbcProviderProvider = Jdp.getProvider(IPersistenceProviderJdbc.class);

    @Override
    public long getNextSequence(@Nonnull final String selectStatement) {
        // no data in cache, must obtain a new database sequence number
        // use the current thread's EntityManager to request a new value
        // from the database, because then we do not need to synchronize
        // different threads requesting different values at the same time.
        try (Connection conn = jdbcProviderProvider.get().getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(selectStatement)) {
            if (rs.next()) {
                final Object result = rs.getObject(1);
                if (result instanceof Number rNumber) {
                    // approach to cover all numeric values...
                    return rNumber.longValue();
                } else {
                    LOGGER.error("sequence query returned type {} which cannot be processed (yet)", result.getClass().getCanonicalName());
                    throw new T9tException(T9tException.JDBC_BAD_TYPE_RETURNED, result.getClass().getCanonicalName());
                }
            } else {
                LOGGER.error("No result returned from sequence query {}", selectStatement);
                throw new T9tException(T9tException.JDBC_NO_RESULT_RETURNED, selectStatement);
            }
        } catch (final SQLException e) {
            LOGGER.error("General SQL exception: Could not obtain next sequence value: {}", ExceptionUtil.causeChain(e));
            throw new T9tException(T9tException.JDBC_GENERAL_SQL, ExceptionUtil.causeChain(e));
        }
    }
}
