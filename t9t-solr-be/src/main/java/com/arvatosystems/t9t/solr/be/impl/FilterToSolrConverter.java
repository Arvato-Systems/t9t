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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    protected String toSolrByUuidFilter(final UuidFilter it) {
        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream()
                    .map(v -> v.toString().replace("-", "\\-")));
        }

        return it.getEqualsValue().toString().replace("-", "\\-");
    }

    protected String toSolrByUnicodeFilter(final UnicodeFilter it) {
        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream());
        }

        return forSolr(it.getEqualsValue() != null ? it.getEqualsValue() : it.getLikeValue());
    }

    protected String toSolrByAsciiFilter(final AsciiFilter it) {
        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream());
        }

        return forSolr(it.getEqualsValue() != null ? it.getEqualsValue() : it.getLikeValue());
    }

    protected String toSolrByIntFilter(final IntFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue().toString();
        }

        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream());
        }
        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolrByLongFilter(final LongFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue().toString();
        }

        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream());
        }

        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolrByDecimalFilter(final DecimalFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue().toString();
        }

        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream());
        }

        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolrByTimeFilter(final TimeFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }

        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream());
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolrByInstantFilter(final InstantFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }

        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream());
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolrByTimestampFilter(final TimestampFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }

        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream());
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolrByDayFilter(final DayFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }

        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream());
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolrByDoubleFilter(final DoubleFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue().toString();
        }

        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream());
        }
        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolrByFloatFilter(final FloatFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue().toString();
        }

        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream());
        }
        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    public String toSolr(final FieldFilter filter) {
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

    protected String buildExpression(Object from, Object to) {
        return "[" + (from == null ? "*" : from) + " TO " + (to == null ? "*" : to) + "]";
    }

    protected String buildExpressionForSolr(Object from, Object to) {
        return "[" + (from == null ? "*" : forSolrByObject(from)) + " TO " + (to == null ? "*" : forSolrByObject(to)) + "]";
    }

    protected <C> String buildOr(Stream<C> valueList) {
        String joined = valueList
        .map(v -> v.toString())
        .collect(Collectors.joining(" OR "));
        return "(" + joined + ")";
    }

    protected <C> String buildOrForSolr(Stream<C> valueList) {
        String joined = valueList
        .map(v -> forSolrByObject(v))
        .collect(Collectors.joining(" OR "));
        return "(" + joined + ")";
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

    protected String toSolrByBooleanFilter(BooleanFilter it) {
        return Boolean.toString(it.getBooleanValue()); // '''«if (booleanValue) { 'true' } else { 'false' }»'''
    }

    protected String toSolrByEnumFilter(EnumFilter it) {
        if (it.getTokenList() != null) {
            return buildOr(it.getTokenList().stream());
        }

        if (it.getNameList() != null) {
            return buildOr(it.getNameList().stream()
                    .map(v -> enumResolver.getTokenByPqonAndInstance(it.getEnumPqon(), v).toString()));
        }

        final String what = it.getEqualsToken() == null ? (String) enumResolver.getTokenByPqonAndInstance(it.getEnumPqon(), it.getEqualsName())
                : it.getEqualsToken();
        if (what == null) {
            return "null";
        }
        return forSolr(what);
    }

    protected String toSolrByXenumFilter(XenumFilter it) {
        if (it.getTokenList() != null) {
            return buildOr(it.getTokenList().stream());
        }

        if (it.getNameList() != null) {
            return buildOr(it.getNameList().stream()
                    .map(v -> enumResolver.getTokenByXEnumPqonAndInstance(it.getXenumPqon(), v).toString()));
        }

        final String what = it.getEqualsToken() == null
                ? (String) enumResolver.getTokenByXEnumPqonAndInstance(it.getXenumPqon(), it.getEqualsName())
                : it.getEqualsToken();
        if (what == null) {
            return "null";
        }
        return forSolr(what);
    }

    @Override
    public String toSolrCondition(SearchFilter sc) {
        if (sc == null) {
            return null;
        }
        return toSolrConditionInternal(sc);
    }

    protected String toSolrConditionInternalByNotFilter(final NotFilter it) {
        final SearchFilter subFilter = it.getFilter();
        if (subFilter instanceof NullFilter) {
            // special case: use specific syntax
            final NullFilter f = (NullFilter) subFilter;
            return f.getFieldName() + ":[* TO *]";
        }
        if (subFilter instanceof NotFilter) {
            // special case: just drop double NOT
            return toSolrConditionInternal(((NotFilter)subFilter).getFilter());
        }
        return "NOT (" + toSolrConditionInternal(subFilter) + ")";
    }

    protected String toSolrConditionInternalByAndFilter(AndFilter it) {
        return "(" + toSolrConditionInternal(it.getFilter1()) + " AND " + toSolrConditionInternal(it.getFilter2()) + ")";
    }

    protected String toSolrConditionInternalByOrFilter(OrFilter it) {
        return "(" + toSolrConditionInternal(it.getFilter1()) + " OR " + toSolrConditionInternal(it.getFilter2()) + ")";
    }

    protected String toSolrConditionInternalByNullFilter(NullFilter it) {
        return "-" + it.getFieldName() + ":[* TO *]";
    }

    protected String toSolrConditionInternal(final SearchFilter it) {
        if (it instanceof NullFilter) {
            return toSolrConditionInternalByNullFilter((NullFilter) it);
        } else if (it instanceof AndFilter) {
            return toSolrConditionInternalByAndFilter((AndFilter) it);
        } else if (it instanceof NotFilter) {
            return toSolrConditionInternalByNotFilter((NotFilter) it);
        } else if (it instanceof OrFilter) {
            return toSolrConditionInternalByOrFilter((OrFilter) it);
        } else if (it instanceof FieldFilter) {
            return "+" + ((FieldFilter) it).getFieldName() + ":" + toSolr((FieldFilter) it);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + it.getClass().getSimpleName());
        }
    }
}
