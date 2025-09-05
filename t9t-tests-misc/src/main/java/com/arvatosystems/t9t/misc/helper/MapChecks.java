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
package com.arvatosystems.t9t.misc.helper;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Tools to check map contents.
 */
public final class MapChecks {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapChecks.class);

    private MapChecks() {
        // empty, private to avoid instantiation
    }

    public enum Values {
        NOT_PRESENT,    // indicates there should be no element of this key.
        ANY,            // anything, but not null
        ANY_NUMBER,     // indicates there should be a value, and type numeric
        ANY_STRING,     // indicates there should be a value, and type string
        ANY_BOOLEAN,    // indicates there should be a value, and type boolean (either true or false)
        ANY_LIST,       // indicates there should be a value, and type list
        ANY_OBJECT,     // indicates there should be a value, and type object (a BonaPortable, or a map)
        OPTIONAL        // indicates ignore any value (no matter if exists or not)
    }

    /**
     * Checks the contents of a map against the expected values.
     * Numeric values are compared checking only integral portions (data is expected to be integral).
     */
    public static void ensureMapContentsIntegralChecks(@Nonnull final Map<String, Object> expected, @Nullable final Map<String, Object> actual, final boolean noOtherEntries, @Nonnull final String what) {
        ensureMapContents(expected, actual, noOtherEntries, what, (a, b) -> a.longValue() == b.longValue());
    }

    /**
     * Checks the contents of a map against the expected values.
     * Numeric values are compared using exact comparison of double values. (Use if results should be exactly reproducible).
     */
    public static void ensureMapContentsDoubleNoToleranceChecks(@Nonnull final Map<String, Object> expected, @Nullable final Map<String, Object> actual, final boolean noOtherEntries, @Nonnull final String what) {
        ensureMapContents(expected, actual, noOtherEntries, what, (a, b) -> a.doubleValue() == b.doubleValue());
    }

    /**
     * Checks the contents of a map against the expected values.
     * Numeric values are compared using comparison of double values, with an allowed absolute tolerance.
     */
    public static void ensureMapContentsDoubleAbsoluteToleranceChecks(@Nonnull final Map<String, Object> expected, @Nullable final Map<String, Object> actual, final boolean noOtherEntries, @Nonnull final String what, final double tolerance) {
        ensureMapContents(expected, actual, noOtherEntries, what, (a, b) -> {
            final double ad = a.doubleValue();
            final double bd = b.doubleValue();
            return ad >= bd - tolerance && ad <= bd + tolerance;
        });
    }

    /**
     * Checks the contents of a map against the expected values.
     * Numeric values are compared using comparison of double values, with an allowed relative tolerance where the expected value is not 0.
     * The tolerance provided must be a value between 0 and 1.
     */
    public static void ensureMapContentsDoubleRelativeToleranceChecks(@Nonnull final Map<String, Object> expected, @Nullable final Map<String, Object> actual, final boolean noOtherEntries, @Nonnull final String what, final double tolerance) {
        ensureMapContents(expected, actual, noOtherEntries, what, (a, b) -> {
            final double ad = a.doubleValue();
            final double bd = b.doubleValue();
            if (ad == 0.0) {
                return bd == 0.0;
            }
            // check for absolute equality (shortcut to avoid divison)
            if (ad == bd) {
                return true;
            }
            // check for acceptable tolerance parameter
            Assertions.assertTrue(tolerance > 0.0 && tolerance < 1.0, what + ": Tolerance must be between 0 and 1");

            // catch exceptions to cater for overflows or NaN
            try {
                final double reverseTolearance = 1.0 / tolerance;
                final double ratio = bd / ad;
                return ratio >= tolerance && ratio <= reverseTolearance;
            } catch (final Throwable t) {
                LOGGER.error(what + ": Exception while checking relative tolerance", t);
                return false;
            }
        });
    }

    /**
     * Checks the contents of a map against the expected values.
     *
     * @param expected          the Map of expected keys and values
     * @param actual            the Map of actual keys and values
     * @param noOtherEntries    true if no other entries than expected should be present
     * @param what              the description of the check (will be logged in case of failure)
     * @param numberComparator  the comparator to use for comparing numeric values
     */
    public static void ensureMapContents(@Nonnull final Map<String, Object> expected, @Nullable final Map<String, Object> actual,
            final boolean noOtherEntries, @Nonnull final String what, @Nonnull BiPredicate<Number, Number> numberComparator) {
        Assertions.assertNotNull(actual, what + ": Map is null but should not");

        int actualValuesMatched = 0;
        // check all entries of the expected map and compare with actual
        for (final Map.Entry<String, Object> entry : expected.entrySet()) {
            final String key = entry.getKey();
            final Object expectedValue = entry.getValue();
            final Object actualValue = actual.get(key);
            if (actualValue != null) {
                actualValuesMatched++;
            }

            // first, check for specific values to compare
            if (expectedValue instanceof Values specialValue) {
                switch (specialValue) {
                case OPTIONAL:
                    // do nothing - anything is OK (test required in cases when combined with noOtherEntries)
                    break;
                case NOT_PRESENT:
                    Assertions.assertNull(actualValue, complainText(what, key, "null or not present", actualValue));
                    break;
                case ANY:
                    Assertions.assertNotNull(actualValue, what + ": Key " + key + " should not be null");
                    break;
                case ANY_NUMBER:
                    Assertions.assertTrue(actualValue instanceof Number, complainText(what, key, "number", actualValue));
                    break;
                case ANY_STRING:
                    Assertions.assertTrue(actualValue instanceof String, complainText(what, key, "string", actualValue));
                    break;
                case ANY_BOOLEAN:
                    Assertions.assertTrue(actualValue instanceof Boolean, complainText(what, key, "boolean", actualValue));
                    break;
                case ANY_LIST:
                    Assertions.assertTrue(actualValue instanceof List<?>, complainText(what, key, "list", actualValue));
                    break;
                case ANY_OBJECT:
                    if (actualValue instanceof Map<?, ?>) {
                        break;  // all fine
                    }
                    Assertions.assertTrue(actualValue instanceof BonaPortable, complainText(what, key, "child object (map or BonaPortable)", actualValue));
                    break;
                default:
                    break;
                }
            } else {
                // any of the direct checks requires a value to be present
                Assertions.assertNotNull(actualValue, what + ": Entry for key " + key + " must be present (expected value " + expectedValue + ")");
                // for numeric, we need special comparison, because they could be various types
                if (expectedValue instanceof Number expectedNumber) {
                    // use compareTo to compare numeric values
                    if (actualValue instanceof Number actualNumber) {
                        // java does not provide a compareTo method with two Number type parameters, therefore we have to convert anything to double and hope we do not get precision errors
                        Assertions.assertTrue(numberComparator.test(expectedNumber.doubleValue(), actualNumber.doubleValue()), what + ": Entry for key " + key + " has unexpected value: Expected " + expectedNumber + ", got " + actualNumber);
                    } else {
                        Assertions.fail(what + ": Entry for key " + key + " should be a number, but is of type " + actualValue.getClass().getSimpleName());
                    }
                } else {
                    // anything else (boolean or string values) should satisfy equals
                    Assertions.assertEquals(expectedValue, actualValue, what + ": Entry for key " + key + " has unexpected value");
                }
            }
        }
        // all value checks OK, but check for unwanted extra values
        if (noOtherEntries) {
            if (actualValuesMatched != actual.size()) {
                // iterate and log entries we did not expect / want
                for (final Map.Entry<String, Object> entry : actual.entrySet()) {
                    if (!expected.containsKey(entry.getKey())) {
                        LOGGER.error("{}: Unwanted map entry {} -> {}", what, entry.getKey(), entry.getValue());
                    }
                }
                Assertions.fail(what + ": Unexpected map entries found");
            }
        }
    }

    /** Creates the text to complain about. */
    private static String complainText(@Nonnull final String what, @Nonnull String key, @Nonnull String expected, @Nullable Object actualValue) {
        return what + ": Entry for key " + key + " should be a " + expected + ", but found " + (actualValue == null ? "null" : actualValue.getClass().getSimpleName());
    }
}
