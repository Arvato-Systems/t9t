package com.arvatosystems.t9t.base;

/**
 * Simple generic pair class.
 *
 * This is there to avoid using a zoo of 3rd party library Pairs (xtend lib, commons, javafx, vavr, ...).
 * However given how simple a specific record can be created with Java 17 ff, consider using a specific record instead.
 *
 * @param <K> the type of the key / left field
 * @param <V> the type of the value / right field
 */
public record Pair<K, V>(K key, V value) {
}
