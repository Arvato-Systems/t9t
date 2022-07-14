package com.arvatosystems.t9t.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Utility class for common checks and operations.
 */
public final class T9tUtil {

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

    /** Transforms any block of whitespace into single spaces, and also removed any leading and trailing spaces. */
    public static String spaceNormalize(final String s) {
        return s == null ? null : s.replaceAll("\\s{2,}", " ").trim();
    }
}
