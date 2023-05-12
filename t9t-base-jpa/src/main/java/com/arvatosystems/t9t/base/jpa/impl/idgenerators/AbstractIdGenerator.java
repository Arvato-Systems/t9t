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
        switch (dialect) {
        case POSTGRES:
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
