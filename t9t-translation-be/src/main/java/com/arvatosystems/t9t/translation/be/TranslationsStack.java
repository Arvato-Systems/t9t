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
package com.arvatosystems.t9t.translation.be;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility method to store a translation.
 */
public class TranslationsStack {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationsStack.class);

    /** An internal counter for duplicate translations, which is read by tests and causes failures / alerts if we have duplicates. */
    private static final AtomicInteger duplicateTranslationCounter = new AtomicInteger(0);

    // the top level map is indexed by field name. Beneath this, there is a map which provides language:tenant:path to translation
    private static final ConcurrentMap<String, ConcurrentMap<String, String>> translations = new ConcurrentHashMap<>(1000);

    public static String makeKey(String language, String tenantId, String path) {
        return tenantId + ":" + language + (path == null ? "" : ":" + path);
    }

    /** Retrieves the number of duplicate (overwritten / discarded) translation entries. */
    public static int getNumberOfDuplicateTranslations() {
        return duplicateTranslationCounter.get();
    }

    /** Clears all imported translations and resets the duplicate counter, use when multiple startups occur, for example multiple unit tests in the same JVM. */
    public static void reset() {
        LOGGER.info("Translations RESET");
        duplicateTranslationCounter.set(0);
        translations.clear();
    }

    /**
     * Adds single translation.
     *
     * @param language
     *            language
     * @param tenantId
     *            tenant Id
     * @param path
     *            group this translation belongs to. Can't be null.
     * @param fieldname
     *            simple field name
     * @param translation
     *            translation value
     */
    public static void addTranslation(String language, String tenantId, String path, String inFieldname, String translation) {
        // internalize the keys because they probably occur many times
        final String key = makeKey(language, tenantId, path).intern();
        final String fieldname = inFieldname.intern();

        ConcurrentMap<String, String> fieldTranslations = translations.get(fieldname);
        if (fieldTranslations == null) {
            fieldTranslations = new ConcurrentHashMap<>(8);
            ConcurrentMap<String, String> tmp = translations.putIfAbsent(fieldname, fieldTranslations);
            if (tmp != null) // race condition: make sure only the first map created is used, discard the second
                fieldTranslations = tmp;
        }
        String oldTrans = fieldTranslations.put(key, translation);
        if (oldTrans != null) {
            duplicateTranslationCounter.incrementAndGet();
            LOGGER.warn("Duplicate translation for field {}, key {}: {} and {}.", fieldname, key, oldTrans, translation);
        }
    }

    /**
     * @param fieldname
     * @return the translations stored, for any language
     */
    public static Map<String, String> getTranslationsForField(String fieldname) {
        return translations.get(fieldname);
    }

    public static void dump(boolean detail) {
        Collection<String> allFields = translations.keySet();
        LOGGER.info("{} translation keys are stored in total", allFields.size());
        if (detail && LOGGER.isDebugEnabled()) {
            StringBuilder b = new StringBuilder();
            b.append("Sorted keys:\n");
            ArrayList<String> keys = new ArrayList<String>(allFields);
            List<String> subList = keys.subList(1, keys.size());
            Collections.sort(subList);
            for (String e: subList) {
                b.append(e);
                b.append('\n');
            }
            LOGGER.debug(b.toString());
        }
    }

    // print all entries for a given key
    public static void dump(String key) {
        Map<String, String> tx = translations.get(key);
        LOGGER.info("{} translations for key {}", tx == null ? 0 : tx.size(), key);
        if (LOGGER.isDebugEnabled() && tx != null) {
            for (Map.Entry<String, String> e: tx.entrySet()) {
                LOGGER.debug("Translation for {} is {}", e.getKey(), e.getValue());
            }
        }
    }
}
