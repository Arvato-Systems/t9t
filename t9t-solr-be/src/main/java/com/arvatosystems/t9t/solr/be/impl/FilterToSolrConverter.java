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
package com.arvatosystems.t9t.solr.be.impl;

import com.arvatosystems.t9t.base.search.EnumFilter;
import com.arvatosystems.t9t.base.search.XenumFilter;
import com.arvatosystems.t9t.base.services.IEnumResolver;
import com.arvatosystems.t9t.solr.be.IFilterToSolrConverter;
import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.BooleanFilter;
import de.jpaw.bonaparte.pojos.api.DayFilter;
import de.jpaw.bonaparte.pojos.api.DecimalFilter;
import de.jpaw.bonaparte.pojos.api.DoubleFilter;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.FloatFilter;
import de.jpaw.bonaparte.pojos.api.InstantFilter;
import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.NullFilter;
import de.jpaw.bonaparte.pojos.api.OrFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TimeFilter;
import de.jpaw.bonaparte.pojos.api.TimestampFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.UuidFilter;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Singleton
public class FilterToSolrConverter implements IFilterToSolrConverter {

    protected static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    protected static final DateTimeFormatter ISO_DAY = DateTimeFormatter.ISO_LOCAL_DATE;

    protected final IEnumResolver enumResolver = Jdp.getRequired(IEnumResolver.class);

    protected static String forSolr(final String s) {
        return s == null ? null : s.replace(":", "\\:").replace(" ", "\\ ").replace("-", "\\-").replace("%", "*");
    }

    protected static String forSolr(final LocalDate t) {
        return t == null ? "*" : ISO_DAY.format(t) + "T00\\\\:00\\\\:00Z";
    }

    protected static String forSolr(final LocalTime t) {
        return t == null ? "*" : ISO_FMT.format(t).replace(":", "\\:");
    }

    protected static String forSolr(final LocalDateTime t) {
        return t == null ? "*" : ISO_FMT.format(t).replace(":", "\\:") + "Z";
    }

    protected static String forSolr(final Instant t) {
        return t == null ? "*" : ISO_FMT.format(t).replace(":", "\\:") + "Z";
    }

    protected Object toSolrByUuidFilter(final UuidFilter filter) {
        return filter.getEqualsValue().toString().replace("-", "\\-");
    }

    protected Object toSolrByUnicodeFilter(final UnicodeFilter it) {
        String forSolr = forSolr(it.getEqualsValue());
        return forSolr == null ? forSolr(it.getLikeValue()) : forSolr;
    }

    protected Object toSolrByAsciiFilter(final AsciiFilter it) {
        String forSolr = forSolr(it.getEqualsValue());
        return forSolr == null ? forSolr(it.getLikeValue()) : forSolr;
    }

    protected Object toSolrByIntFilter(final IntFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue();
        }
        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected Object toSolrByLongFilter(final LongFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue();
        }
        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected Object toSolrByDecimalFilter(final DecimalFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue();
        }
        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected Object toSolrByTimeFilter(final TimeFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound());
    }

    protected Object toSolrByInstantFilter(final InstantFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound());
    }

    protected Object toSolrByTimestampFilter(final TimestampFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound());
    }

    protected Object toSolrByDayFilter(final DayFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound());
    }

    protected Object toSolrByDoubleFilter(final DoubleFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue();
        }
        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected Object toSolrByFloatFilter(final FloatFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue();
        }
        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    public Object toSolr(final FieldFilter filter) {
        if (filter instanceof EnumFilter) {
            return toSolrByEnumFilter((EnumFilter) filter);
        } else if (filter instanceof XenumFilter) {
            return toSolrByXenumFilter((XenumFilter) filter);
        } else if (filter instanceof AsciiFilter) {
            return toSolrByAsciiFilter((AsciiFilter) filter);
        } else if (filter instanceof BooleanFilter) {
            return toSolrByBooleanFilter((BooleanFilter) filter);
        } else if (filter instanceof DayFilter) {
            return toSolrByDayFilter((DayFilter) filter);
        } else if (filter instanceof DecimalFilter) {
            return toSolrByDecimalFilter((DecimalFilter) filter);
        } else if (filter instanceof DoubleFilter) {
            return toSolrByDoubleFilter((DoubleFilter) filter);
        } else if (filter instanceof FloatFilter) {
            return toSolrByFloatFilter((FloatFilter) filter);
        } else if (filter instanceof InstantFilter) {
            return toSolrByInstantFilter((InstantFilter) filter);
        } else if (filter instanceof IntFilter) {
            return toSolrByIntFilter((IntFilter) filter);
        } else if (filter instanceof LongFilter) {
            return toSolrByLongFilter((LongFilter) filter);
        } else if (filter instanceof TimeFilter) {
            return toSolrByTimeFilter((TimeFilter) filter);
        } else if (filter instanceof TimestampFilter) {
            return toSolrByTimestampFilter((TimestampFilter) filter);
        } else if (filter instanceof UnicodeFilter) {
            return toSolrByUnicodeFilter((UnicodeFilter) filter);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + filter.getClass().getSimpleName());
        }
    }

    protected Object buildExpression(Object from, Object to) {
        return "[" + from == null ? "*" : from + " TO " + to == null ? "*" : to + "]";
    }

    protected Object buildExpressionForSolr(Object from, Object to) {
        return "[" + from == null ? "*" : forSolrByObject(from) + " TO " + to == null ? "*" : forSolrByObject(to) + "]";
    }

    protected String forSolrByObject(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return forSolr((String) obj);
        } else if (obj instanceof LocalDate) {
            return forSolr((LocalDate) obj);
        } else if (obj instanceof LocalTime) {
            return forSolr((LocalTime) obj);
        } else if (obj instanceof LocalDateTime) {
            return forSolr((LocalDateTime) obj);
        } else if (obj instanceof Instant) {
            return forSolr((Instant) obj);
        } else if (obj instanceof LocalDateTime) {
            return forSolr((LocalDateTime) obj);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + obj.getClass().getSimpleName());
        }
    }

    protected Object toSolrByBooleanFilter(BooleanFilter it) {
        return Boolean.toString(it.getBooleanValue()); // '''«if (booleanValue) { 'true' } else { 'false' }»'''
    }

    protected Object toSolrByEnumFilter(EnumFilter filter) {
        final String what = filter.getEqualsToken() == null ? (String) enumResolver.getTokenByPqonAndInstance(filter.getEnumPqon(), filter.getEqualsName())
                : filter.getEqualsToken();
        if (what == null) {
            return "null";
        }
        return forSolr(what.toString());
    }

    protected Object toSolrByXenumFilter(XenumFilter filter) {
        final String what = filter.getEqualsToken() == null
                ? (String) enumResolver.getTokenByXEnumPqonAndInstance(filter.getXenumPqon(), filter.getEqualsName())
                : filter.getEqualsToken();
        if (what == null) {
            return "null";
        }
        return forSolr(what.toString());
    }

    @Override
    public String toSolrCondition(SearchFilter sc) {
        if (sc == null) {
            return null;
        }
        return toSolrConditionInternal(sc).toString();
    }

    protected String toSolrConditionInternalByNotFilter(final NotFilter it) {
        if (it.getFilter() instanceof NullFilter) {
            // special case: use specific syntax
            final NullFilter f = (NullFilter) it.getFilter();
            return f.getFieldName() + ":[* TO *]";
        }
        return "NOT (" + toSolrConditionInternal(it.getFilter()) + ")";
    }

    protected String toSolrConditionInternalByAndFilter(AndFilter it) {
        return "(" + toSolrConditionInternal(it.getFilter1()) + " AND " + toSolrConditionInternal(it.getFilter2()) + ")";
    }

    protected String toSolrConditionInternalByOrFilter(OrFilter it) {
        return "(" + toSolrConditionInternal(it.getFilter1()) + " AND " + toSolrConditionInternal(it.getFilter2()) + ")";
    }

    protected String toSolrConditionInternalByNullFilter(NullFilter it) {
        return "-" + it.getFieldName() + ":[* TO *]";
    }

    protected String toSolrConditionInternalByFieldFilter(FieldFilter it) {
        throw new Error("Unresolved compilation problems:" + "\nType mismatch: cannot convert implicit first argument from FieldFilter to UuidFilter");
    }

    protected CharSequence toSolrConditionInternal(final SearchFilter it) {
        if (it instanceof NullFilter) {
            return toSolrConditionInternalByNullFilter((NullFilter) it);
        } else if (it instanceof AndFilter) {
            return toSolrConditionInternalByAndFilter((AndFilter) it);
        } else if (it instanceof FieldFilter) {
            return toSolrConditionInternalByFieldFilter((FieldFilter) it);
        } else if (it instanceof NotFilter) {
            return toSolrConditionInternalByNotFilter((NotFilter) it);
        } else if (it instanceof OrFilter) {
            return toSolrConditionInternalByOrFilter((OrFilter) it);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + it.getClass().getSimpleName());
        }
    }
}
