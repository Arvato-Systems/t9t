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
package com.arvatosystems.t9t.base;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.jpaw.util.CharTestsASCII;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * A utility class which converts between strings which contain comma separated values and sorted sets.
 */
public class StringSetConverter {

    private final Set<String> elements = new TreeSet<>();
    private final char delimiter = ',';
    private final Predicate<String> validator;

    /** Constructs a set converter for a specified validator. */
    public StringSetConverter(@Nonnull Predicate<String> validator) {
        this.validator = validator;
    }

    /** Constructs a set converter which does not perform any validation. */
    public static StringSetConverter withoutValidation() {
        return new StringSetConverter(s -> true);
    }

    /** Constructs a set converter for a specified list of elements. */
    public static StringSetConverter forListOfAllowedElements(@Nonnull Collection<String> allowedElements) {
        return new StringSetConverter(allowedElements::contains);
    }

    /** Constructs a set converter for country codes, using a very simple (non-tight) check. */
    public static StringSetConverter forCountryCodes() {
        return new StringSetConverter(StringSetConverter::isValidCountryCode);
    }

    /** Constructs a set converter for currency codes, using a very simple (non-tight) check. */
    public static StringSetConverter forCurrencyCodes() {
        return new StringSetConverter(StringSetConverter::isValidCurrencyCode);
    }

    /** Utility method to just convert a delimited list, without any validation. */
    @Nonnull
    public static Set<String> toSet(@Nullable final String elements) {
        return T9tUtil.isBlank(elements) ? Collections.emptySet() : withoutValidation().addElements(elements).asSet();
    }

    /** Checks if a given element is part of the current set. */
    public boolean contains(@Nonnull final String element) {
        return elements.contains(element);
    }

    /** Returns the set of elements, as immutable set. */
    public Set<String> asSet() {
        return Collections.unmodifiableSet(elements);
    }

    private static boolean isValidCountryCode(final String countryCode) {
        return countryCode.length() == 2
                && CharTestsASCII.isAsciiUpperCase(countryCode.charAt(0))
                && CharTestsASCII.isAsciiUpperCase(countryCode.charAt(1));
    }

    private static boolean isValidCurrencyCode(final String currencyCode) {
        return currencyCode.length() == 3
                && CharTestsASCII.isAsciiUpperCase(currencyCode.charAt(0))
                && CharTestsASCII.isAsciiUpperCase(currencyCode.charAt(1))
                && CharTestsASCII.isAsciiUpperCase(currencyCode.charAt(2));
    }

    /** Checks if all elements are valid. (Should never fail because we perform checks while adding). */
    public final boolean allElementsValid() {
        for (final String element : elements) {
            if (!validator.test(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Performs a logical union with the input.
     * Validates the input and throws an Exception if an element is invalid.
     * Returns the object itself.
     */
    public final StringSetConverter addElements(@Nullable final String element) {
        if (element == null || element.isEmpty()) {
            return this;
        }
        final String[] parts = element.split(",");
        for (final String s : parts) {
            final String s2 = s.trim();
            if (!s2.isEmpty()) {
                if (!validator.test(s2)) {
                    throw new T9tException(T9tException.INVALID_ELEMENT_ERROR, s2);
                }
                elements.add(s2);
            }
        }
        return this;
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int size() {
        return elements.size();
    }

    public final String toString() {
        if (elements.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(3 * elements.size());
        boolean first = true;
        for (final String element : elements) {
            if (first) {
                first = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(element);
        }
        return sb.toString();
    }

    /** Returns null instead of the empty string in case of an empty set. */
    public final String toStringOrNull() {
        if (elements.isEmpty()) {
            return null;
        }
        return toString();
    }

    /** Iterates all elements. */
    public void forEach(@Nonnull final Consumer<String> consumer) {
        for (final String element : elements) {
            consumer.accept(element);
        }
    }
}
