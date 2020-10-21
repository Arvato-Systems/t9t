/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.tfi.general;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.lang.Generics;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;

public class Maps {

    public static Map<String, Object> newMap(Object... keyValue) {
        Map<String, Object> maps = new HashMap<>();
        if (keyValue != null) {
            if ((keyValue.length % 2) != 0) {
                throw new IllegalArgumentException(String.format("The key-value pairs are odd and not even (size:%s). Check your implemention!!", keyValue.length));
            }
            String key;
            Object value;
            for (int i = 0; i < keyValue.length; i++) {
                key = String.valueOf(keyValue[i]);
                i++;
                value = keyValue[i];
                maps.put(key, value);
            }
        }
        return maps;
    }

    public static <K extends Object, V extends Object> HashMap<K, V> newRegexMap() {
        return self().new RegExHashMap<>();
    }

    public static <K extends Object, V extends Object> HashMap<K, V> newRegexMap(HashMap<K, V> userHashMap) {
        HashMap<K, V> newMap = self().new RegExHashMap<>();
        newMap.putAll((Map<K, V>) userHashMap);
        return newMap;
    }

    private static Maps self() {
        return new Maps();
    }

    public static <K extends Object, V extends Object> FluentMap<K, V> fluentMap() {
        return self().new FluentMap<K, V>(new HashMap<K, V>());
    }

    public static FluentMap<String, Object> fluentMapSO() {
        return self().new FluentMap<String, Object>(new HashMap<String, Object>());
    }

    public static <K extends Object, V extends Object> FluentMap<K, V> fluentMap(HashMap<K, V> userHashMap) {
        return self().new FluentMap<K, V>(userHashMap);
    }

    public class FluentMap<K, V> {
        HashMap<K, V> usedMap = null;

        public FluentMap(HashMap<K, V> usedMap) {
            this.usedMap = usedMap;
        }

        public FluentMap<K, V> put(K key, V value) {
            usedMap.put(key, value);
            return this;
        }

        public HashMap<K, V> asMap() {
            return this.usedMap;
        }

    }

    /**
     * This class is an extended version of Java HashMap
     * and includes pattern-value lists which are used to
     * evaluate regular expression values. If given item
     * is a regular expression, it is saved in regexp lists.
     * If requested item matches with a regular expression,
     * its value is get from regexp lists.
     * @param <K> : Key of the map item.
     * @param <V> : Value of the map item.
     */
    public class RegExHashMap<K, V> extends HashMap<K, V> {
        private static final long  serialVersionUID = 5832094803205800967L;
        // list of regular expression patterns
        private ArrayList<Pattern> regExPatterns    = new ArrayList<Pattern>();
        // list of regular expression values which match patterns
        private ArrayList<V>       regExValues      = new ArrayList<V>();

        /*
         * (non-Javadoc)
         * @see java.util.HashMap#putAll(java.util.Map)
         */
        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }

        /**
         * Compile regular expression and add it to the regexp list as key.
         */
        @Override
        public V put(K key, V value) {

            regExPatterns.add(Pattern.compile(key.toString()));
            regExValues.add(value);
            return super.put(key, value);
        }

        /**
         * If requested value matches with a regular expression,
         * returns it from regexp lists.
         */
        @Override
        public V get(Object key) {
            CharSequence cs = new String(key.toString());

            for (int i = 0; i < regExPatterns.size(); i++) {
                if (regExPatterns.get(i).matcher(cs).matches()) {
                    return regExValues.get(i);
                }
            }
            return super.get(key);
        }

        /*
         * (non-Javadoc)
         * @see java.util.HashMap#containsKey(java.lang.Object)
         */
        @Override
        public boolean containsKey(Object key) {
            return get(key) != null;
        }

    }

    public static int MAP_TO_STRING_MAX_WIDTH = 80;

    public static String mapToString(Map<Object, Object> map) {
        return mapToString(map, MAP_TO_STRING_MAX_WIDTH);
    }

    public static String mapToString(Map<Object, Object> map, int maxWidth) {
        if (map == null) {
            return "null";
        }
        Set<Entry<Object, Object>> entrySet = map.entrySet();
        Iterator<Entry<Object, Object>> i = entrySet.iterator();
        if (!i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<?, ?> e = i.next();
            Object key = e.getKey();
            Object value = e.getValue();
            sb.append(key == map ? "(this Map)" : key);
            sb.append('=');
            if (value instanceof Map) {
                Map<Object, Object> innerMap = Generics.cast(value);
                sb.append(mapToString(innerMap));
            } else if (value instanceof BonaPortable) {

                sb.append("((");
                BonaPortable bonaPortable = Generics.cast(value);
                List<FieldDefinition> fieldDefinitions = bonaPortable != null ? bonaPortable.ret$MetaData().getFields() : Collections.<FieldDefinition>emptyList();
                for (FieldDefinition fieldDefinition : fieldDefinitions) {
                    if (fieldDefinition instanceof ObjectReference) {
                        try {
                            Field field = bonaPortable.getClass().getDeclaredField(fieldDefinition.getName());
                            field.setAccessible(true);
                            sb.append(StringUtils.abbreviate(String.valueOf(field.get(bonaPortable)), maxWidth));
                        } catch (Exception e1) {
                            sb.append(String.valueOf(e1.getMessage()));
                        }
                        sb.append(',').append(' ');
                    }
                }
                sb.append("))");

            } else {
                sb.append(StringUtils.abbreviate((value == map ? "(this Map)" : String.valueOf(value)), maxWidth));
            }
            if (!i.hasNext()) {
                return sb.append('}').toString();
            } else {
                sb.append(',').append(' ');
            }
        }
    }

}
