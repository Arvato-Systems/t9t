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
package com.arvatosystems.t9t.base;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;

import com.arvatosystems.t9t.base.request.AggregationGranularityType;

import de.jpaw.enums.TokenizableEnum;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
/**
 * Utility class for common checks and operations.
 */
public final class T9tUtil {

    /**
     * Dividing line for console outputs
     */
    public static final String CON_MSG_LINE = "-------------------------------------------------------------------------";

    private T9tUtil() {
        // empty, private to avoid instantiation
    }

    /**
     * Checks whether the given {@link Collection} parameter is null or empty.
     *
     * @param collection the {@link Collection} object to check
     * @return true if param is null or empty
     */
    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks whether the given {@link Collection} parameter is null or empty.
     *
     * @param collection the {@link Collection} object to check
     * @return true if param is null or empty
     */
    public static boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks whether the given {@link String} parameter is null or blank.
     *
     * @param string the {@link String} object to check
     * @return true if param is null or blank
     */
    public static boolean isBlank(final String string) {
        return string == null || string.isBlank();
    }

    /**
     * Convenience method to avoid duplicate evaluation of b, to check if a {@link Boolean} is <code>TRUE</code>.
     *
     * @param bool the {@link Boolean} object to check
     * @return true if param is null or true
     */
    public static boolean isTrue(final Boolean bool) {
        return bool == null ? false : bool.booleanValue();
    }

    /** Left-pads the input string to the given number of characters. */
    public static String lpad(final String srcIn, final int length, final char padCharacter) {
        if (srcIn.length() >= length) {
            return srcIn;
        }
        final StringBuilder b = new StringBuilder(length);
        int toFill = length - srcIn.length();
        do {
            b.append(padCharacter);
        } while (--toFill > 0);
        b.append(srcIn);
        return b.toString();
    }

    /** Formats an integer to the given length. */
    public static String toString(final int num, final int length) {
        return lpad(Integer.toString(num), length, '0');
    }

    /**
     * Convenience which creates a new list of transformed items of some input collection.
     *
     * @param collection the input data
     * @param transformer the conversion method
     * @return a list containing the converted elements or an empty immutable list
     */
    public static <X, Y> List<X> transformToList(final Collection<Y> collection, final Function<Y, X> transformer) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        final List<X> result = new ArrayList<>(collection.size());
        for (final Y data : collection) {
            result.add(transformer.apply(data));
        }
        return result;
    }

    /**
     * Convenience which creates a new set of transformed items of some input collection.
     *
     * @param collection the input data
     * @param transformer the conversion method
     * @return a list containing the converted elements or an empty immutable set
     */
    public static <X, Y> Set<X> transformToSet(final Collection<Y> collection, final Function<Y, X> transformer) {
        if (isEmpty(collection)) {
            return Collections.emptySet();
        }
        final Set<X> result = new HashSet<>(collection.size());
        for (final Y data : collection) {
            result.add(transformer.apply(data));
        }
        return result;
    }

    /**
     * Replacement for the xtend elvis operator :?.
     * Returns value == null ? useWhenNull : value;
     *
     * @param value the input data (value to test)
     * @param useWhenNull the default value
     * @return value, if not null, else useWhenNull
     */
    public static <X> X nvl(final X value, final X useWhenNull) {
        return value == null ? useWhenNull : value;
    }

    /** Computes the logical XOR of 2 parameters (exactly one of the arguments must be true). */
    public static boolean xor(final boolean a, final boolean b) {
        return a ? !b : b;
    }


    /** Returns the string with whitespace removed, or null if the string was null. */
    public static String trim(final String s) {
        return s == null ? s : s.trim();
    }

    /** Transforms any block of whitespace into single spaces, and also removed any leading and trailing spaces. */
    public static String spaceNormalize(final String s) {
        return s == null ? null : s.replaceAll("\\s{2,}", " ").trim();
    }

    /** Returns the token of an enum or null. */
    public static String getTokenOrNull(final TokenizableEnum e) {
        return e == null ? null : e.getToken();
    }

    /** Tests if the enum matches a given instance or null. */
    public static <E extends Enum<E>> boolean matchOrNull(final Enum<E> e, final Enum<E> valueToTest) {
        return e == null || e == valueToTest;
    }

    /**
     * Log given message with surrounding lines.
     *
     * @param logger the {@link Logger} to use for the logging.
     * @param msg the message to log out
     */
    public static void logWithLines(final Logger logger, final String msg) {
        logger.info(CON_MSG_LINE);
        logger.info(msg);
        logger.info(CON_MSG_LINE);
    }

    /** Checks if param {@code object} equals any of the given {@code objects}. */
    public static boolean equalsAny(final Object object, final Object... objects) {
        if (objects != null) {
            for (final Object obj : objects) {
                if (Objects.equals(object, obj)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Sleeps for the specified number of milliseconds, and complains if interrupted. A logger is passed to conserve the true origin of the message. */
    public static void sleepAndWarnIfInterrupted(final long milliseconds, @Nullable final Logger logger, @Nullable final String complainString) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            if (logger != null && complainString != null) {
                logger.warn(complainString);
            }
        }
    }

    /** Returns the current time as integral Instant (no fractional seconds). Often needed because some APIs desire integral seconds. */
    public static Instant currentInstantRoundedToSeconds() {
        return Instant.ofEpochSecond(System.currentTimeMillis() / 1000L);
    }

    /** Returns the passed Instant as integral Instant (no fractional seconds). */
    public static Instant roundedToSeconds(final Instant when) {
        // do not construct a new object in case we already have an integral Instant
        return when.getNano() == 0 ? when : Instant.ofEpochSecond(when.getEpochSecond());
    }

    /** Truncates the provided timestamp to the specified precision. */
    public static LocalDateTime truncate(@Nonnull final LocalDateTime when, @Nonnull final AggregationGranularityType precision) {
        return switch (precision) {
        case DAY    -> when.truncatedTo(ChronoUnit.DAYS);
        case HOUR   -> when.truncatedTo(ChronoUnit.HOURS);
        case MINUTE -> when.truncatedTo(ChronoUnit.MINUTES);
        case SECOND -> when.truncatedTo(ChronoUnit.SECONDS);
        default     -> throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "truncate LocalDateTime to " + precision);
        };
    }

    /** Subtracts the provided duration from the timestamp. */
    public static LocalDateTime minusDuration(@Nonnull final LocalDateTime when, @Nonnull final AggregationGranularityType precision, final long units) {
        return switch (precision) {
        case DAY    -> when.minusDays(units);
        case HOUR   -> when.minusHours(units);
        case MINUTE -> when.minusMinutes(units);
        case SECOND -> when.minusSeconds(units);
        default     -> throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "minusDuration LocalDateTime with " + precision);
        };
    }

    /** Adds the provided duration from the timestamp. */
    public static LocalDateTime plusDuration(@Nonnull final LocalDateTime when, @Nonnull final AggregationGranularityType precision, final long units) {
        return switch (precision) {
        case DAY    -> when.plusDays(units);
        case HOUR   -> when.plusHours(units);
        case MINUTE -> when.plusMinutes(units);
        case SECOND -> when.plusSeconds(units);
        default     -> throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "minusDuration LocalDateTime with " + precision);
        };
    }
}
