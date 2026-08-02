package com.arvatosystems.t9t.base.jpa.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.annotation.Nonnull;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import de.jpaw.bonaparte.core.ObjectValidationException;
import de.jpaw.bonaparte.pojos.api.BooleanFilter;
import de.jpaw.bonaparte.pojos.api.ByteArrayFilter;
import de.jpaw.bonaparte.pojos.api.ByteFilter;
import de.jpaw.bonaparte.pojos.api.BytesFilter;
import de.jpaw.bonaparte.pojos.api.DayFilter;
import de.jpaw.bonaparte.pojos.api.DecimalFilter;
import de.jpaw.bonaparte.pojos.api.DoubleFilter;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.FloatFilter;
import de.jpaw.bonaparte.pojos.api.InstantFilter;
import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.NullFilter;
import de.jpaw.bonaparte.pojos.api.ShortFilter;
import de.jpaw.bonaparte.pojos.api.StringFilter;
import de.jpaw.bonaparte.pojos.api.TimeFilter;
import de.jpaw.bonaparte.pojos.api.TimestampFilter;
import de.jpaw.bonaparte.pojos.api.UuidFilter;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

import com.arvatosystems.t9t.base.jpa.IJpaFilter;

@Singleton
public class JpaFilterImpl implements IJpaFilter {

    private List<String> toLowerCase(@Nonnull final List<String> list, @Nonnull final Locale locale) {
        final List<String> lowerList = new ArrayList<>(list.size());
        for (final String s : list) {
            lowerList.add(s.toLowerCase(locale));
        }
        return lowerList;
    }

    @Override
    public Predicate applyFilter(CriteriaBuilder cb, Path<?> path, FieldFilter filter) {
        return switch (filter) {
            case NullFilter f -> cb.isNull(path);
            case BooleanFilter f -> cb.equal(path, Boolean.valueOf(f.getBooleanValue()));
            case StringFilter f -> {
                if (!Boolean.TRUE.equals(f.getCaseInsensitive())) {
                    final Path<String> stringPath = (Path<String>) path;
                    if (f.getValueList() != null)
                        yield path.in(f.getValueList());
                    else if (f.getEqualsValue() != null)
                        yield cb.equal(path, f.getEqualsValue());
                    else if (f.getLikeValue() != null)
                        yield cb.like(stringPath, f.getLikeValue());
                    else if (f.getUpperBound() != null) {
                        if (f.getLowerBound() != null) {
                            yield cb.between(stringPath, f.getLowerBound(), f.getUpperBound());
                        } else {
                            yield cb.lessThanOrEqualTo(stringPath, f.getUpperBound());
                        }
                    } else {
                        if (f.getLowerBound() != null) {
                            yield cb.greaterThanOrEqualTo(stringPath, f.getLowerBound());
                        } else {
                            throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
                        }
                    }
                } else {
                    final Expression<String> stringExpr = cb.lower((Path<String>) path);
                    if (f.getValueList() != null)
                        yield stringExpr.in(toLowerCase(f.getValueList(), Locale.ROOT));
                    else if (f.getEqualsValue() != null)
                        yield cb.equal(stringExpr, f.getEqualsValue().toLowerCase(Locale.ROOT));
                    else if (f.getLikeValue() != null)
                        yield cb.like(stringExpr, f.getLikeValue().toLowerCase(Locale.ROOT));
                    else if (f.getUpperBound() != null) {
                        if (f.getLowerBound() != null) {
                            yield cb.between(stringExpr, f.getLowerBound().toLowerCase(Locale.ROOT), f.getUpperBound().toLowerCase(Locale.ROOT));
                        } else {
                            yield cb.lessThanOrEqualTo(stringExpr, f.getUpperBound().toLowerCase(Locale.ROOT));
                        }
                    } else {
                        if (f.getLowerBound() != null) {
                            yield cb.greaterThanOrEqualTo(stringExpr, f.getLowerBound().toLowerCase(Locale.ROOT));
                        } else {
                            throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
                        }
                    }
                }
            }
            case IntFilter f -> {
                final Path<Integer> intPath = (Path<Integer>) path;
                if (f.getValueList() != null)
                    yield intPath.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(intPath, f.getEqualsValue());
                else if (f.getLowerBound() != null && f.getUpperBound() != null)
                    yield cb.between(intPath, f.getLowerBound(), f.getUpperBound());
                else if (f.getLowerBound() != null)
                    yield cb.greaterThanOrEqualTo(intPath, f.getLowerBound());
                else if (f.getUpperBound() != null)
                    yield cb.lessThanOrEqualTo(intPath, f.getUpperBound());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case LongFilter f -> {
                final Path<Long> longPath = (Path<Long>) path;
                if (f.getValueList() != null)
                    yield longPath.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(longPath, f.getEqualsValue());
                else if (f.getLowerBound() != null && f.getUpperBound() != null)
                    yield cb.between(longPath, f.getLowerBound(), f.getUpperBound());
                else if (f.getLowerBound() != null)
                    yield cb.greaterThanOrEqualTo(longPath, f.getLowerBound());
                else if (f.getUpperBound() != null)
                    yield cb.lessThanOrEqualTo(longPath, f.getUpperBound());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case DecimalFilter f -> {
                final Path<BigDecimal> decimalPath = (Path<BigDecimal>) path;
                if (f.getValueList() != null)
                    yield decimalPath.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(decimalPath, f.getEqualsValue());
                else if (f.getLowerBound() != null && f.getUpperBound() != null)
                    yield cb.between(decimalPath, f.getLowerBound(), f.getUpperBound());
                else if (f.getLowerBound() != null)
                    yield cb.greaterThanOrEqualTo(decimalPath, f.getLowerBound());
                else if (f.getUpperBound() != null)
                    yield cb.lessThanOrEqualTo(decimalPath, f.getUpperBound());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case DayFilter f -> {
                final Path<LocalDate> dayPath = (Path<LocalDate>) path;
                if (f.getValueList() != null)
                    yield dayPath.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(dayPath, f.getEqualsValue());
                else if (f.getLowerBound() != null && f.getUpperBound() != null)
                    yield cb.between(dayPath, f.getLowerBound(), f.getUpperBound());
                else if (f.getLowerBound() != null)
                    yield cb.greaterThanOrEqualTo(dayPath, f.getLowerBound());
                else if (f.getUpperBound() != null)
                    yield cb.lessThanOrEqualTo(dayPath, f.getUpperBound());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case TimestampFilter f -> {
                final Path<LocalDateTime> timestampPath = (Path<LocalDateTime>) path;
                if (f.getValueList() != null)
                    yield timestampPath.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(timestampPath, f.getEqualsValue());
                else if (f.getLowerBound() != null && f.getUpperBound() != null)
                    yield cb.between(timestampPath, f.getLowerBound(), f.getUpperBound());
                else if (f.getLowerBound() != null)
                    yield cb.greaterThanOrEqualTo(timestampPath, f.getLowerBound());
                else if (f.getUpperBound() != null)
                    yield cb.lessThanOrEqualTo(timestampPath, f.getUpperBound());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case InstantFilter f -> {
                final Path<Instant> instantPath = (Path<Instant>) path;
                if (f.getValueList() != null)
                    yield instantPath.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(instantPath, f.getEqualsValue());
                else if (f.getLowerBound() != null && f.getUpperBound() != null)
                    yield cb.between(instantPath, f.getLowerBound(), f.getUpperBound());
                else if (f.getLowerBound() != null)
                    yield cb.greaterThanOrEqualTo(instantPath, f.getLowerBound());
                else if (f.getUpperBound() != null)
                    yield cb.lessThanOrEqualTo(instantPath, f.getUpperBound());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case TimeFilter f -> {
                final Path<LocalTime> timePath = (Path<LocalTime>) path;
                if (f.getValueList() != null)
                    yield timePath.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(timePath, f.getEqualsValue());
                else if (f.getLowerBound() != null && f.getUpperBound() != null)
                    yield cb.between(timePath, f.getLowerBound(), f.getUpperBound());
                else if (f.getLowerBound() != null)
                    yield cb.greaterThanOrEqualTo(timePath, f.getLowerBound());
                else if (f.getUpperBound() != null)
                    yield cb.lessThanOrEqualTo(timePath, f.getUpperBound());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case ByteFilter f -> {
                final Path<Byte> bytePath = (Path<Byte>) path;
                if (f.getValueList() != null)
                    yield bytePath.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(bytePath, f.getEqualsValue());
                else if (f.getLowerBound() != null && f.getUpperBound() != null)
                    yield cb.between(bytePath, f.getLowerBound(), f.getUpperBound());
                else if (f.getLowerBound() != null)
                    yield cb.greaterThanOrEqualTo(bytePath, f.getLowerBound());
                else if (f.getUpperBound() != null)
                    yield cb.lessThanOrEqualTo(bytePath, f.getUpperBound());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case ShortFilter f -> {
                final Path<Short> shortPath = (Path<Short>) path;
                if (f.getValueList() != null)
                    yield shortPath.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(shortPath, f.getEqualsValue());
                else if (f.getLowerBound() != null && f.getUpperBound() != null)
                    yield cb.between(shortPath, f.getLowerBound(), f.getUpperBound());
                else if (f.getLowerBound() != null)
                    yield cb.greaterThanOrEqualTo(shortPath, f.getLowerBound());
                else if (f.getUpperBound() != null)
                    yield cb.lessThanOrEqualTo(shortPath, f.getUpperBound());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case DoubleFilter f -> {
                final Path<Double> doublePath = (Path<Double>) path;
                if (f.getValueList() != null)
                    yield doublePath.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(doublePath, f.getEqualsValue());
                else if (f.getLowerBound() != null && f.getUpperBound() != null)
                    yield cb.between(doublePath, f.getLowerBound(), f.getUpperBound());
                else if (f.getLowerBound() != null)
                    yield cb.greaterThanOrEqualTo(doublePath, f.getLowerBound());
                else if (f.getUpperBound() != null)
                    yield cb.lessThanOrEqualTo(doublePath, f.getUpperBound());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case FloatFilter f -> {
                final Path<Float> floatPath = (Path<Float>) path;
                if (f.getValueList() != null)
                    yield floatPath.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(floatPath, f.getEqualsValue());
                else if (f.getLowerBound() != null && f.getUpperBound() != null)
                    yield cb.between(floatPath, f.getLowerBound(), f.getUpperBound());
                else if (f.getLowerBound() != null)
                    yield cb.greaterThanOrEqualTo(floatPath, f.getLowerBound());
                else if (f.getUpperBound() != null)
                    yield cb.lessThanOrEqualTo(floatPath, f.getUpperBound());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case UuidFilter f -> {
                if (f.getValueList() != null)
                    yield path.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(path, f.getEqualsValue());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case ByteArrayFilter f -> {
                if (f.getValueList() != null)
                    yield path.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(path, f.getEqualsValue());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            case BytesFilter f -> {
                if (f.getValueList() != null)
                    yield path.in(f.getValueList());
                else if (f.getEqualsValue() != null)
                    yield cb.equal(path, f.getEqualsValue());
                else
                    throw new ApplicationException(ObjectValidationException.FILTER_WITHOUT_NONNULL_FIELDS, filter.getFieldName());
            }
            default -> throw new RuntimeException("Unrecognized field filter type: " + filter.ret$PQON());
        };
    }
}
