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
package com.arvatosystems.t9t.base.services;

import java.util.HashMap;
import java.util.Map;

import de.jpaw.bonaparte.pojos.apiw.Ref;

public final class MapperCache {
    private MapperCache() {
    }

    /** Produces a field path name from a given prefix and a field name. */
    public static String concat(final String prefix, final String field) {
        return prefix == null ? field : (prefix + "." + field);
    }
    public static Map<Long, Ref> getCache(final Map<String, Map<Long, Ref>> cache, final String pqon) {
        return cache.computeIfAbsent(pqon, (x) -> new HashMap<>());
    }
    public static Map<Long, Ref> getCache(final Map<String, Map<Long, Ref>> cache, final String pqon, final int initialSize) {
        return cache.computeIfAbsent(pqon, (x) -> new HashMap<>(initialSize * 2));
    }
    public static <V extends Ref> Map<Long, V> getCache(final Map<String, Map<Long, Ref>> cache, final Class<V> dtoCls, final int initialSize) {
        return (Map<Long, V>) cache.computeIfAbsent(dtoCls.getSimpleName(), x -> new HashMap<>(initialSize * 2));
    }
    public static void addToCache(final Map<String, Map<Long, Ref>> cache, final String pqon, final Long key, final Ref dto) {
        getCache(cache, pqon).put(key, dto);
    }
    public static Ref getFromCache(final Map<String, Map<Long, Ref>> cache, final String pqon, final Long key) {
        return getCache(cache, pqon).get(key);
    }
}
