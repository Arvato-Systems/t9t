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
