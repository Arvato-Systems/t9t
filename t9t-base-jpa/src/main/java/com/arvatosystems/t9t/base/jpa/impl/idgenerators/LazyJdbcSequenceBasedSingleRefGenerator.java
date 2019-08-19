/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jpa.impl.idgenerators;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IJdbcConnectionProvider;
import com.arvatosystems.t9t.cfg.be.DatabaseBrandType;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ExceptionUtil;

class LazyJdbcSequenceBasedSingleRefGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LazyJdbcSequenceBasedSingleRefGenerator.class);

    private volatile long lastProvidedValue;
    private volatile int remainingCachedIds;
    private final int cacheSize;
    private final String sqlCommandForNextValue;

    private final IJdbcConnectionProvider jdbcProvider = Jdp.getRequired(IJdbcConnectionProvider.class, "independent");

    public LazyJdbcSequenceBasedSingleRefGenerator(int index, DatabaseBrandType dialect, int cacheSize) {

        lastProvidedValue = -1L;
        remainingCachedIds = 0;
        this.cacheSize = cacheSize;  // intentionally not tunable via xml config due to the risk of overlapping IDs if someone decreases values between restarts.
        switch (dialect) {
        case POSTGRES:
            sqlCommandForNextValue = String.format("SELECT nextval('cm_idgen_%04d_seq')", index);
            break;
        case ORACLE:
            sqlCommandForNextValue = String.format("SELECT cm_idgen_%04d_seq.NEXTVAL FROM DUAL", index);
            break;
        case HANA:
            sqlCommandForNextValue = String.format("SELECT cm_idgen_%04d_seq.NEXTVAL FROM DUMMY", index);
            break;
        case MS_SQL_SERVER: // requires MS SQL server 2012 or newer (sequences were introduces only then)
            sqlCommandForNextValue = String.format("SELECT NEXT VALUE FOR [dbo].[cm_idgen_%04d_seq]", index);
            break;
        default: // does not happen!
            LOGGER.error("undefined DatabaseDialect: {}", dialect.name());
            throw new T9tException(T9tException.JDBC_UNKNOWN_DIALECT, dialect.name()); // this would be a fatal error
        }
    }

    synchronized long getnextId() {
        if (remainingCachedIds > 0) {
            --remainingCachedIds;
            ++lastProvidedValue;
        } else {
            long nextval = 0L;
            // no data in cache, must obtain a new database sequence number
            // use the current thread's EntityManager to request a new value
            // from the database, because then we do not need to synchronize
            // different threads requesting different values at the same time.
            try (Connection conn = jdbcProvider.getJDBCConnection();
                 Statement stmt  = conn.createStatement();
                 ResultSet rs    = stmt.executeQuery(sqlCommandForNextValue)) {
                if (rs.next()) {
                    Object result = rs.getObject(1);
                    if (result instanceof Long) {
                        // default type
                        nextval = (Long) result;
                    } else if (result instanceof BigInteger) {
                        // Some DB returns BigInteger: must convert data type
                        nextval = ((BigInteger) result).longValue();
                    } else if (result instanceof BigDecimal) {
                        // Oracle: must convert data type
                        nextval = ((BigDecimal) result).longValue();
                    } else if (result instanceof Number) {
                        // approach to cover some of the above as well...
                        nextval = ((Number) result).longValue();
                    } else {
                        LOGGER.error("sequence query returned type {} which cannot be processed (yet)", result.getClass().getCanonicalName());
                        throw new T9tException(T9tException.JDBC_BAD_TYPE_RETURNED, result.getClass().getCanonicalName());
                    }
                } else {
                    LOGGER.error("No result returned from sequence query {}", sqlCommandForNextValue);
                    throw new T9tException(T9tException.JDBC_NO_RESULT_RETURNED, sqlCommandForNextValue);
                }
            } catch (SQLException e) {
                LOGGER.error("General SQL exception: Could not obtain next sequence value: {}", ExceptionUtil.causeChain(e));
                throw new T9tException(T9tException.JDBC_GENERAL_SQL, ExceptionUtil.causeChain(e));
            }
            // store data for the next bunch of results
            lastProvidedValue = nextval * cacheSize;
            remainingCachedIds = cacheSize - 1;
        }
        return lastProvidedValue;
    }
}
