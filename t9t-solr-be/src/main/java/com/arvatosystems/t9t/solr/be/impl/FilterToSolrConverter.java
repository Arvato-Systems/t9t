/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
import java.util.function.Function;
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

    protected String toSolr(final UuidFilter it) {
        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream()
                    .map(v -> v.toString().replace("-", "\\-")));
        }

        return it.getEqualsValue().toString().replace("-", "\\-");
    }

    protected String toSolr(final UnicodeFilter it) {
        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream(), (String s) -> s);
        }

        return forSolr(it.getEqualsValue() != null ? it.getEqualsValue() : it.getLikeValue());
    }

    protected String toSolr(final AsciiFilter it) {
        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream(), (String s) -> s);
        }

        return forSolr(it.getEqualsValue() != null ? it.getEqualsValue() : it.getLikeValue());
    }

    protected String toSolr(final IntFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue().toString();
        }

        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream());
        }
        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolr(final LongFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue().toString();
        }

        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream());
        }

        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolr(final DecimalFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue().toString();
        }

        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream());
        }

        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolr(final TimeFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }

        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream(), (LocalTime ldt) -> forSolr(ldt));
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound(), (LocalTime lt) -> forSolr(lt));
    }

    protected String toSolr(final InstantFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }

        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream(), (Instant ldt) -> forSolr(ldt));
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound(), (Instant i) -> forSolr(i));
    }

    protected String toSolr(final TimestampFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }

        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream(), (LocalDateTime ldt) -> forSolr(ldt));
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound(), (LocalDateTime ldt) -> forSolr(ldt));
    }

    protected String toSolr(final DayFilter it) {
        if (it.getEqualsValue() != null) {
            return forSolr(it.getEqualsValue());
        }

        if (it.getValueList() != null) {
            return buildOrForSolr(it.getValueList().stream(), (LocalDate ldt) -> forSolr(ldt));
        }
        return buildExpressionForSolr(it.getLowerBound(), it.getUpperBound(), (LocalDate ld) -> forSolr(ld));
    }

    protected String toSolr(final DoubleFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue().toString();
        }

        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream());
        }
        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    protected String toSolr(final FloatFilter it) {
        if (it.getEqualsValue() != null) {
            return it.getEqualsValue().toString();
        }

        if (it.getValueList() != null) {
            return buildOr(it.getValueList().stream());
        }
        return buildExpression(it.getLowerBound(), it.getUpperBound());
    }

    public String toSolr(final FieldFilter filter) {
// JEP 406 is preview only, Java will need a few more years to allow this :-(
//        return switch(filter) {
//        case EnumFilter      enumFilter    -> toSolr(enumFilter);
//        case XenumFilter     xenumFilter   -> toSolr(xenumFilter);
//        case AsciiFilter     asciiFilter   -> toSolr(asciiFilter);
//        case UnicodeFilter   unicodeFilter -> toSolr(unicodeFilter);
//        case LongFilter      longFilter    -> toSolr(longFilter);
//        case IntFilter       intFilter     -> toSolr(intFilter);
//        case BooleanFilter   booleanFilter -> toSolr(booleanFilter);
//        case DayFilter       dayFilter     -> toSolr(dayFilter);
//        case DecimalFilter   decimalFilter -> toSolr(decimalFilter);
//        case DoubleFilter    doubleFilter  -> toSolr(doubleFilter);
//        case FloatFilter     floatFilter   -> toSolr(floatFilter);
//        case InstantFilter   instantFilter -> toSolr(instantFilter);
//        case TimeFilter      timeFilter    -> toSolr(timeFilter);
//        case TimestampFilter timestampFilter -> toSolr(timestampFilter);
//        default -> throw new IllegalArgumentException("Unhandled parameter types: " + filter.getClass().getSimpleName());
//        };
        if (filter instanceof EnumFilter f) {
            return toSolr(f);
        } else if (filter instanceof XenumFilter f) {
            return toSolr(f);
        } else if (filter instanceof AsciiFilter f) {
            return toSolr(f);
        } else if (filter instanceof BooleanFilter f) {
            return toSolr(f);
        } else if (filter instanceof DayFilter f) {
            return toSolr(f);
        } else if (filter instanceof DecimalFilter f) {
            return toSolr(f);
        } else if (filter instanceof DoubleFilter f) {
            return toSolr(f);
        } else if (filter instanceof FloatFilter f) {
            return toSolr(f);
        } else if (filter instanceof InstantFilter f) {
            return toSolr(f);
        } else if (filter instanceof IntFilter f) {
            return toSolr(f);
        } else if (filter instanceof LongFilter f) {
            return toSolr(f);
        } else if (filter instanceof TimeFilter f) {
            return toSolr(f);
        } else if (filter instanceof TimestampFilter f) {
            return toSolr(f);
        } else if (filter instanceof UnicodeFilter f) {
            return toSolr(f);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + filter.getClass().getSimpleName());
        }
    }

    protected String buildExpression(Object from, Object to) {
        return "[" + (from == null ? "*" : from) + " TO " + (to == null ? "*" : to) + "]";
    }

    protected <T> String buildExpressionForSolr(T from, T to, Function<T, String> converter) {
        return "[" + (from == null ? "*" : converter.apply(from)) + " TO " + (to == null ? "*" : converter.apply(to)) + "]";
    }

    protected <C> String buildOr(final Stream<C> valueList) {
        String joined = valueList.map(v -> v.toString()).collect(Collectors.joining(" OR "));
        return "(" + joined + ")";
    }

    protected <T> String buildOrForSolr(final Stream<T> valueList, Function<T, String> converter) {
        String joined = valueList.map(v -> converter.apply(v)).collect(Collectors.joining(" OR "));
        return "(" + joined + ")";
    }

    protected String toSolr(final BooleanFilter it) {
        return Boolean.toString(it.getBooleanValue());
    }

    protected String toSolr(EnumFilter it) {
        if (it.getTokenList() != null) {
            return buildOr(it.getTokenList().stream());
        }

        if (it.getNameList() != null) {
            return buildOr(it.getNameList().stream()
                    .map(v -> enumResolver.getTokenByPqonAndInstance(it.getEnumPqon(), v).toString()));
        }

        final String what = it.getEqualsToken() == null
            ? (String) enumResolver.getTokenByPqonAndInstance(it.getEnumPqon(), it.getEqualsName())
            : it.getEqualsToken();
        if (what == null) {
            return "null";
        }
        return forSolr(what);
    }

    protected String toSolr(XenumFilter it) {
        if (it.getTokenList() != null) {
            return buildOr(it.getTokenList().stream());
        }

        if (it.getNameList() != null) {
            return buildOr(it.getNameList().stream().map(v -> enumResolver.getTokenByXEnumPqonAndInstance(it.getXenumPqon(), v).toString()));
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
        if (sc instanceof NullFilter nullFilter) {
            // NullFilter is subclass of FieldFilter and must go first before that
            return toSolrCondition(nullFilter);
        } else if (sc instanceof FieldFilter fieldFilter) {
            return ("+" + fieldFilter.getFieldName() + ":" + toSolr(fieldFilter));
        } else if (sc instanceof NotFilter notFilter) {
            return toSolrCondition(notFilter);
        } else if (sc instanceof AndFilter andFilter) {
            return toSolrCondition(andFilter);
        } else if (sc instanceof OrFilter orFilter) {
            return toSolrCondition(orFilter);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + sc.getClass().getSimpleName());
        }
    }

    protected String toSolrCondition(final NotFilter it) {
        final SearchFilter subFilter = it.getFilter();
        if (subFilter instanceof NullFilter nullFilter) {
            // special case: use specific syntax
            return nullFilter.getFieldName() + ":[* TO *]";
        }
        if (subFilter instanceof NotFilter notFilter) {
            // special case: just drop double NOT
            return toSolrCondition(notFilter.getFilter());
        }
        return "NOT (" + toSolrCondition(subFilter) + ")";
    }

    protected String toSolrCondition(final AndFilter it) {
        return "(" + toSolrCondition(it.getFilter1()) + " AND " + toSolrCondition(it.getFilter2()) + ")";
    }

    protected String toSolrCondition(final OrFilter it) {
        return "(" + toSolrCondition(it.getFilter1()) + " OR " + toSolrCondition(it.getFilter2()) + ")";
    }

    protected String toSolrCondition(final NullFilter it) {
        return "-" + it.getFieldName() + ":[* TO *]";
    }
}
