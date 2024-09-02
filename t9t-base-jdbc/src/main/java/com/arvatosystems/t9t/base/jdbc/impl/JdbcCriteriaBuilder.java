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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.jdbc.IJdbcCriteriaBuilder;
import com.arvatosystems.t9t.base.search.SearchCriteria;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.BooleanFilter;
import de.jpaw.bonaparte.pojos.api.DecimalFilter;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.InstantFilter;
import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.OrFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TimestampFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.UuidFilter;
import de.jpaw.dp.Singleton;
import de.jpaw.util.CharTestsASCII;
import jakarta.annotation.Nonnull;

@Singleton
public class JdbcCriteriaBuilder implements IJdbcCriteriaBuilder {

    @Override
    public List<Consumer<PreparedStatement>> createWhereClause(final StringBuilder sb, final SearchCriteria searchCriteria) {
        final List<Consumer<PreparedStatement>> setters = new ArrayList<>();
        if (searchCriteria.getSearchFilter() != null) {
            sb.append(" WHERE");
            walkFilterTree(sb, searchCriteria.getSearchFilter(), setters);
        }
        if (searchCriteria.getSortColumns() != null && !searchCriteria.getSortColumns().isEmpty()) {
            sb.append(" ORDER BY ");
            boolean first = true;
            for (final var sc : searchCriteria.getSortColumns()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(camelCaseToSnakeCase(sc.getFieldName()));
                if (sc.getDescending()) {
                    sb.append(" DESC");
                }
                first = false;
            }
        }
        return setters;
    }

    protected String camelCaseToSnakeCase(@Nonnull final String s) {
        // return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, s);  // guava solution, but looks slower than below due to generic approach
        final int len = s.length();
        final StringBuilder sb = new StringBuilder(len + 4);
        for (int i = 0; i < len; ++i) {
            final char c = s.charAt(i);
            if (CharTestsASCII.isAsciiUpperCase(c)) {
                sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    protected void walkFilterTree(@Nonnull final StringBuilder sb, @Nonnull final SearchFilter sf, @Nonnull List<Consumer<PreparedStatement>> setters) {
        if (sf instanceof NotFilter nf) {
            sb.append(" NOT (");
            walkFilterTree(sb, nf.getFilter(), setters);
            sb.append(")");
        } else if (sf instanceof AndFilter af) {
            walkFilterTree(sb, af.getFilter1(), setters);
            sb.append(" AND");
            walkFilterTree(sb, af.getFilter2(), setters);
        } else if (sf instanceof OrFilter of) {
            walkFilterTree(sb, of.getFilter1(), setters);
            sb.append(" OR");
            walkFilterTree(sb, of.getFilter2(), setters);
        } else if (sf instanceof FieldFilter ff) {
            final String sqlName = camelCaseToSnakeCase(ff.getFieldName());
            sb.append(" ");
            sb.append(sqlName);
            applyFieldFilter(sb, ff, setters);
        } else {
            throw new IllegalArgumentException("Unknown filter type " + sf.getClass().getSimpleName());
        }
    }

    protected void applyFieldFilter(@Nonnull final StringBuilder sb, @Nonnull final FieldFilter ff, @Nonnull final List<Consumer<PreparedStatement>> setters) {
        final int nextIndex = setters.size() + 1;
        if (ff instanceof IntFilter intFilter) {
            applyFieldFilter(sb, setters, nextIndex, intFilter);
        } else if (ff instanceof LongFilter longFilter) {
            applyFieldFilter(sb, setters, nextIndex, longFilter);
        } else if (ff instanceof DecimalFilter decimalFilter) {
            applyFieldFilter(sb, setters, nextIndex, decimalFilter);
        } else if (ff instanceof TimestampFilter timestampFilter) {
            applyFieldFilter(sb, setters, nextIndex, timestampFilter);
        } else if (ff instanceof InstantFilter instantFilter) {
            applyFieldFilter(sb, setters, nextIndex, instantFilter);
        } else if (ff instanceof AsciiFilter asciiFilter) {
            applyFieldFilter(sb, setters, nextIndex, asciiFilter);
        } else if (ff instanceof UnicodeFilter unicodeFilter) {
            applyFieldFilter(sb, setters, nextIndex, unicodeFilter);
        } else if (ff instanceof BooleanFilter booleanFilter) {
            applyFieldFilter(sb, setters, nextIndex, booleanFilter);
        } else if (ff instanceof UuidFilter uuidFilter) {
            applyFieldFilter(sb, setters, nextIndex, uuidFilter);
        } else {
            throw new IllegalArgumentException("Unknown filter type " + ff.getClass().getSimpleName());
        }
    }

    /** Below just Java boilerplate. It can't get any worse! */

    protected void applyFieldFilter(@Nonnull final StringBuilder sb, @Nonnull final List<Consumer<PreparedStatement>> setters, final int nextIndex,
      @Nonnull final IntFilter ff) {
        if (ff.getEqualsValue() != null) {
            sb.append(" = ?");
            setters.add(ps -> wrappedSetInt(ps, nextIndex, ff.getEqualsValue()));
        } else if (ff.getLowerBound() != null) {
            setters.add(ps -> wrappedSetInt(ps, nextIndex, ff.getLowerBound()));
            if (ff.getUpperBound() != null) {
                // range provided
                sb.append(" BETWEEN ? AND ? ");
                setters.add(ps -> wrappedSetInt(ps, nextIndex + 1, ff.getUpperBound()));
            } else {
                // only lower bound provided
                sb.append(" >= ?");
            }
        } else if (ff.getUpperBound() != null) {
            sb.append(" <= ?");
            setters.add(ps -> wrappedSetInt(ps, nextIndex, ff.getUpperBound()));
        } else {
            throw new IllegalArgumentException("IntFilter must have at least one of equalsValue, lowerBound, upperBound");
        }
    }

    protected void wrappedSetInt(@Nonnull final PreparedStatement ps, final int index, final int value) {
        try {
            ps.setInt(index, value);
        } catch (final SQLException e) {
            throw new T9tException(T9tException.JDBC_GENERAL_SQL, e);
        }
    }

    protected void applyFieldFilter(@Nonnull final StringBuilder sb, @Nonnull final List<Consumer<PreparedStatement>> setters, final int nextIndex,
              @Nonnull final LongFilter ff) {
        if (ff.getEqualsValue() != null) {
            sb.append(" = ?");
            setters.add(ps -> wrappedSetLong(ps, nextIndex, ff.getEqualsValue()));
        } else if (ff.getLowerBound() != null) {
            setters.add(ps -> wrappedSetLong(ps, nextIndex, ff.getLowerBound()));
            if (ff.getUpperBound() != null) {
                // range provided
                sb.append(" BETWEEN ? AND ? ");
                setters.add(ps -> wrappedSetLong(ps, nextIndex + 1, ff.getUpperBound()));
            } else {
                // only lower bound provided
                sb.append(" >= ?");
            }
        } else if (ff.getUpperBound() != null) {
            sb.append(" <= ?");
            setters.add(ps -> wrappedSetLong(ps, nextIndex, ff.getUpperBound()));
        } else {
            throw new IllegalArgumentException("LongFilter must have at least one of equalsValue, lowerBound, upperBound");
        }
    }

    protected void wrappedSetLong(@Nonnull final PreparedStatement ps, final int index, final long value) {
        try {
            ps.setLong(index, value);
        } catch (final SQLException e) {
            throw new T9tException(T9tException.JDBC_GENERAL_SQL, e);
        }
    }

    protected void applyFieldFilter(@Nonnull final StringBuilder sb, @Nonnull final List<Consumer<PreparedStatement>> setters, final int nextIndex,
            @Nonnull final DecimalFilter ff) {
        if (ff.getEqualsValue() != null) {
            sb.append(" = ?");
            setters.add(ps -> wrappedSetDecimal(ps, nextIndex, ff.getEqualsValue()));
        } else if (ff.getLowerBound() != null) {
            setters.add(ps -> wrappedSetDecimal(ps, nextIndex, ff.getLowerBound()));
            if (ff.getUpperBound() != null) {
                // range provided
                sb.append(" BETWEEN ? AND ? ");
                setters.add(ps -> wrappedSetDecimal(ps, nextIndex + 1, ff.getUpperBound()));
            } else {
                // only lower bound provided
                sb.append(" >= ?");
            }
        } else if (ff.getUpperBound() != null) {
            sb.append(" <= ?");
            setters.add(ps -> wrappedSetDecimal(ps, nextIndex, ff.getUpperBound()));
        } else {
            throw new IllegalArgumentException("DecimalFilter must have at least one of equalsValue, lowerBound, upperBound");
        }
    }

    protected void wrappedSetDecimal(@Nonnull final PreparedStatement ps, final int index, final BigDecimal value) {
        try {
            ps.setBigDecimal(index, value);
        } catch (final SQLException e) {
            throw new T9tException(T9tException.JDBC_GENERAL_SQL, e);
        }
    }

    protected void applyFieldFilter(@Nonnull final StringBuilder sb, @Nonnull final List<Consumer<PreparedStatement>> setters, final int nextIndex,
            @Nonnull final TimestampFilter ff) {
        if (ff.getEqualsValue() != null) {
            sb.append(" = ?");
            setters.add(ps -> wrappedSetTimestamp(ps, nextIndex, ff.getEqualsValue()));
        } else if (ff.getLowerBound() != null) {
            setters.add(ps -> wrappedSetTimestamp(ps, nextIndex, ff.getLowerBound()));
            if (ff.getUpperBound() != null) {
                // range provided
                sb.append(" BETWEEN ? AND ? ");
                setters.add(ps -> wrappedSetTimestamp(ps, nextIndex + 1, ff.getUpperBound()));
            } else {
                // only lower bound provided
                sb.append(" >= ?");
            }
        } else if (ff.getUpperBound() != null) {
            sb.append(" <= ?");
            setters.add(ps -> wrappedSetTimestamp(ps, nextIndex, ff.getUpperBound()));
        } else {
            throw new IllegalArgumentException("TimestampFilter must have at least one of equalsValue, lowerBound, upperBound");
        }
    }

    protected void wrappedSetTimestamp(@Nonnull final PreparedStatement ps, final int index, final LocalDateTime value) {
        try {
            // convert LocalDateTime to SQL Timestamp. Unfortunately, the only way to do it is to use a deprecated type
            final Timestamp ts = Timestamp.valueOf(value);
            ps.setTimestamp(index, ts);
        } catch (final SQLException e) {
            throw new T9tException(T9tException.JDBC_GENERAL_SQL, e);
        }
    }

    protected void applyFieldFilter(@Nonnull final StringBuilder sb, @Nonnull final List<Consumer<PreparedStatement>> setters, final int nextIndex,
            @Nonnull final InstantFilter ff) {
        if (ff.getEqualsValue() != null) {
            sb.append(" = ?");
            setters.add(ps -> wrappedSetInstant(ps, nextIndex, ff.getEqualsValue()));
        } else if (ff.getLowerBound() != null) {
            setters.add(ps -> wrappedSetInstant(ps, nextIndex, ff.getLowerBound()));
            if (ff.getUpperBound() != null) {
                // range provided
                sb.append(" BETWEEN ? AND ? ");
                setters.add(ps -> wrappedSetInstant(ps, nextIndex + 1, ff.getUpperBound()));
            } else {
                // only lower bound provided
                sb.append(" >= ?");
            }
        } else if (ff.getUpperBound() != null) {
            sb.append(" <= ?");
            setters.add(ps -> wrappedSetInstant(ps, nextIndex, ff.getUpperBound()));
        } else {
            throw new IllegalArgumentException("InstantFilter must have at least one of equalsValue, lowerBound, upperBound");
        }
    }

    protected void wrappedSetInstant(@Nonnull final PreparedStatement ps, final int index, final Instant value) {
        try {
            ps.setTimestamp(index, Timestamp.from(value));
        } catch (final SQLException e) {
            throw new T9tException(T9tException.JDBC_GENERAL_SQL, e);
        }
    }

    protected void applyFieldFilter(@Nonnull final StringBuilder sb, @Nonnull final List<Consumer<PreparedStatement>> setters, final int nextIndex,
            @Nonnull final AsciiFilter ff) {
        if (ff.getEqualsValue() != null) {
            sb.append(" = ?");
            setters.add(ps -> wrappedSetString(ps, nextIndex, ff.getEqualsValue()));
        } else if (ff.getLikeValue() != null) {
            sb.append(" LIKE ?");
            setters.add(ps -> wrappedSetString(ps, nextIndex, ff.getLikeValue()));
        } else if (ff.getLowerBound() != null) {
            setters.add(ps -> wrappedSetString(ps, nextIndex, ff.getLowerBound()));
            if (ff.getUpperBound() != null) {
                // range provided
                sb.append(" BETWEEN ? AND ? ");
                setters.add(ps -> wrappedSetString(ps, nextIndex + 1, ff.getUpperBound()));
            } else {
                // only lower bound provided
                sb.append(" >= ?");
            }
        } else if (ff.getUpperBound() != null) {
            sb.append(" <= ?");
            setters.add(ps -> wrappedSetString(ps, nextIndex, ff.getUpperBound()));
        } else {
            throw new IllegalArgumentException("AsciiFilter must have at least one of equalsValue, lowerBound, upperBound");
        }
    }

    protected void wrappedSetString(@Nonnull final PreparedStatement ps, final int index, final String value) {
        try {
            ps.setString(index, value);
        } catch (final SQLException e) {
            throw new T9tException(T9tException.JDBC_GENERAL_SQL, e);
        }
    }

    protected void applyFieldFilter(@Nonnull final StringBuilder sb, @Nonnull final List<Consumer<PreparedStatement>> setters, final int nextIndex,
            @Nonnull final UnicodeFilter ff) {
        if (ff.getEqualsValue() != null) {
            sb.append(" = ?");
            setters.add(ps -> wrappedSetString(ps, nextIndex, ff.getEqualsValue()));
        } else if (ff.getLikeValue() != null) {
            sb.append(" LIKE ?");
            setters.add(ps -> wrappedSetString(ps, nextIndex, ff.getLikeValue()));
        } else if (ff.getLowerBound() != null) {
            setters.add(ps -> wrappedSetString(ps, nextIndex, ff.getLowerBound()));
            if (ff.getUpperBound() != null) {
                // range provided
                sb.append(" BETWEEN ? AND ? ");
                setters.add(ps -> wrappedSetString(ps, nextIndex + 1, ff.getUpperBound()));
            } else {
                // only lower bound provided
                sb.append(" >= ?");
            }
        } else if (ff.getUpperBound() != null) {
            sb.append(" <= ?");
            setters.add(ps -> wrappedSetString(ps, nextIndex, ff.getUpperBound()));
        } else {
            throw new IllegalArgumentException("UnicodeFilter must have at least one of equalsValue, lowerBound, upperBound");
        }
    }

    protected void applyFieldFilter(@Nonnull final StringBuilder sb, @Nonnull final List<Consumer<PreparedStatement>> setters, final int nextIndex,
            @Nonnull final BooleanFilter ff) {
        sb.append(ff.getBooleanValue() ? " = TRUE" : " = FALSE");
    }

    protected void applyFieldFilter(@Nonnull final StringBuilder sb, @Nonnull final List<Consumer<PreparedStatement>> setters, final int nextIndex,
            @Nonnull final UuidFilter ff) {
        if (ff.getEqualsValue() != null) {
            sb.append(" = ?");
            setters.add(ps -> wrappedSetUUID(ps, nextIndex, ff.getEqualsValue()));
        } else {
            throw new IllegalArgumentException("UnicodeFilter must have at least one of equalsValue, lowerBound, upperBound");
        }
    }

    protected void wrappedSetUUID(@Nonnull final PreparedStatement ps, final int index, final UUID value) {
        try {
            ps.setObject(index, value, Types.OTHER);
        } catch (final SQLException e) {
            throw new T9tException(T9tException.JDBC_GENERAL_SQL, e);
        }
    }
}
