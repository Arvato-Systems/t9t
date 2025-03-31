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
package com.arvatosystems.t9t.base.jpa.impl.idgenerators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.cfg.be.DatabaseBrandType;

import jakarta.annotation.Nonnull;

public abstract class AbstractIdGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIdGenerator.class);

    /** Returns the name of the sequence used for RTTI based primary key generation. */
    @Nonnull
    protected String sequenceNameForIndex(final int index) {
        return String.format("cm_idgen_%04d_seq", index);
    }

    /** Returns the name of the sequence used for primary key generation with per-table sequences. */
    @Nonnull
    protected String sequenceNameForTable(@Nonnull final String tablename) {
        return tablename + "_s";
    }

    @Nonnull
    protected String selectStatementForSequence(@Nonnull final DatabaseBrandType dialect, @Nonnull final String sequenceName) {
        return selectStatementForSequence(dialect, sequenceName, 0);
    }

    @Nonnull
    protected String selectStatementForSequence(@Nonnull final DatabaseBrandType dialect, @Nonnull final String sequenceName, final int eagerCacheSize) {
        switch (dialect) {
        case POSTGRES:
            if (eagerCacheSize > 1) {
                return "SELECT setval('" + sequenceName + "', nextval('" + sequenceName + "') + " + eagerCacheSize + ")";
            }
            return "SELECT nextval('" + sequenceName + "')";
        case H2:
            return "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
        case ORACLE:
            return "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
        case HANA:
            return "SELECT " + sequenceName + ".NEXTVAL FROM DUMMY";
        case MS_SQL_SERVER:
            return "SELECT NEXT VALUE FOR [dbo].[" + sequenceName + "]";
        default: // does not happen!
            LOGGER.error("undefined DatabaseDialect: {}", dialect.name());
            throw new T9tException(T9tException.JDBC_UNKNOWN_DIALECT, dialect.name()); // this would be a fatal error
        }
    }
}
