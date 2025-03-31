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
package com.arvatosystems.t9t.translation.be;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;

/**
 * Utility method to store a translation.
 */
public final class TranslationsStack {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationsStack.class);

    private TranslationsStack() { }

    /** An internal counter for duplicate translations, which is read by tests and causes failures / alerts if we have duplicates. */
    private static final AtomicInteger DUPLICATE_TRANSLATION_COUNTER = new AtomicInteger(0);

    // the top level map is indexed by field name. Beneath this, there is a map which provides language:tenant:path to translation
    private static final ConcurrentMap<String, ConcurrentMap<String, String>> TRANSLATIONS = new ConcurrentHashMap<>(1000);

    public static String makeKey(final String language, final String tenantId, final String path) {
        return tenantId + ":" + language + (path == null ? "" : ":" + path);
    }

    /** Retrieves the number of duplicate (overwritten / discarded) translation entries. */
    public static int getNumberOfDuplicateTranslations() {
        return DUPLICATE_TRANSLATION_COUNTER.get();
    }

    /** Clears all imported translations and resets the duplicate counter, use when multiple startups occur, for example multiple unit tests in the same JVM. */
    public static void reset() {
        LOGGER.info("Translations RESET");
        DUPLICATE_TRANSLATION_COUNTER.set(0);
        TRANSLATIONS.clear();
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
    public static void addTranslation(final String language, final String tenantId, final String path, final String inFieldname, final String translation) {
        // internalize the keys because they probably occur many times
        final String key = makeKey(language, tenantId, path).intern();
        final String fieldname = inFieldname.intern();

        ConcurrentMap<String, String> fieldTranslations = TRANSLATIONS.get(fieldname);
        if (fieldTranslations == null) {
            fieldTranslations = new ConcurrentHashMap<>(8);
            final ConcurrentMap<String, String> tmp = TRANSLATIONS.putIfAbsent(fieldname, fieldTranslations);
            if (tmp != null) // race condition: make sure only the first map created is used, discard the second
                fieldTranslations = tmp;
        }
        final String oldTrans = fieldTranslations.put(key, translation);
        if (oldTrans != null) {
            DUPLICATE_TRANSLATION_COUNTER.incrementAndGet();
            LOGGER.warn("Duplicate translation for field {}, key {}: {} and {}.", fieldname, key, oldTrans, translation);
        }
    }

    /**
     * @param fieldname
     * @return the translations stored, for any language
     */
    public static Map<String, String> getTranslationsForField(final String fieldname) {
        return TRANSLATIONS.get(fieldname);
    }

    public static void dump(final boolean detail) {
        final Collection<String> allFields = TRANSLATIONS.keySet();
        LOGGER.info("{} translation keys are stored in total", allFields.size());
        if (detail && LOGGER.isDebugEnabled()) {
            final StringBuilder b = new StringBuilder();
            b.append("Sorted keys:\n");
            final ArrayList<String> keys = new ArrayList<>(allFields);
            final List<String> subList = keys.subList(1, keys.size());
            Collections.sort(subList);
            for (final String e: subList) {
                b.append(e);
                b.append('\n');
            }
            LOGGER.debug(b.toString());
        }
    }

    // print all entries for a given key
    public static void dump(final String key) {
        final Map<String, String> tx = TRANSLATIONS.get(key);
        LOGGER.info("{} translations for key {}", tx == null ? 0 : tx.size(), key);
        if (LOGGER.isDebugEnabled() && tx != null) {
            for (final Map.Entry<String, String> e: tx.entrySet()) {
                LOGGER.debug("Translation for {} is {}", e.getKey(), e.getValue());
            }
        }
    }

    // perform a dup check on all entries
    public static void checkDuplicates(final boolean doOtherLangs, final boolean doGerman, final boolean checkMatchEn) {
        final Collection<String> allFields = TRANSLATIONS.keySet();
        for (final String field: allFields) {
            checkDuplicates(field, doOtherLangs, doGerman, checkMatchEn);
        }
    }

    // perform a dup check on a specific entry
    public static void checkDuplicates(final String key, final boolean doOtherLangs, final boolean doGerman, final boolean checkMatchEn) {
        final Map<String, String> tx = TRANSLATIONS.get(key);
        // convert the entries to a nested map, by language, by path. Store the default entry by path ""
        final Map<String, Map<String, String>> restructured = new HashMap<>(20);
        for (final Map.Entry<String, String> e: tx.entrySet()) {
            // split the key
            final String[] keyParts = e.getKey().split(":");
            if (keyParts.length < 2) {
                LOGGER.error("Bad key for {}: {}", key, e.getKey());
            } else {
                if (!T9tConstants.GLOBAL_TENANT_ID.equals(keyParts[0])) {
                    LOGGER.warn("skipping non-default tenant for {}: {}", key, e.getKey());
                } else {
                    // process this part
                    final String language = keyParts[1];
                    final String path = keyParts.length < 3 ? "" : (keyParts[2] == null ? "" : keyParts[2]);
                    final Map<String, String> mapForLanguage = restructured.computeIfAbsent(language, l -> new HashMap<>());
                    mapForLanguage.put(path, e.getValue() == null ? "" : e.getValue().trim());
                }
            }
        }
        // check en: translations which match default
        final Map<String, String> translationsForEn = restructured.get("en");
        if (translationsForEn == null) {
            LOGGER.error("No English entry for {}!", key);
        } else {
            final StringBuilder b = new StringBuilder(1000);
            checkForRedundantToDefault(b, translationsForEn);
            if (doGerman) {
                final Map<String, String> translationsForDe = restructured.get("de");
                if (translationsForDe != null) {
                    checkForRedundantToDefaultAndEn(b, "de", translationsForDe, translationsForEn, checkMatchEn);
                }
            }
            if (doOtherLangs) {
                for (final Map.Entry<String, Map<String, String>> other: restructured.entrySet()) {
                    final String thisLang = other.getKey();
                    if (!thisLang.equals("de") && !thisLang.equals("en")) {
                        checkForRedundantToDefaultAndEn(b, thisLang, other.getValue(), translationsForEn, checkMatchEn);
                    }
                }
            }
            // finally, log it if required
            if (b.length() > 0) {
                LOGGER.error("{}: {}", key, b);
            }
        }
    }

    private static void addToLog(final StringBuilder b, final String language, final int match, final int noMatch, final int matchEn) {
        if (match > 0 || matchEn > 0) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(String.format("%s: %d match default", language, match));
            if (noMatch > 0) {
                b.append(String.format(" (%d not)", noMatch));
            }
            if (matchEn > 0) {
                b.append(String.format(", %d=EN", matchEn));
            }
        }
    }

    private static void checkForRedundantToDefault(final StringBuilder b, final Map<String, String> translationsForLanguage) {
        final String defaultEntry = translationsForLanguage.get("");
        if (defaultEntry == null) {
            return;
        }
        int match = 0;
        int noMatch = 0;
        for (final Map.Entry<String, String> e: translationsForLanguage.entrySet()) {
            if (e.getKey().length() != 0) {
                if (defaultEntry.equalsIgnoreCase(e.getValue())) {
                    ++match;
                } else {
                    ++noMatch;
                }
            }
        }
        addToLog(b, "en", match, noMatch, 0);
    }

    private static void checkForRedundantToDefaultAndEn(final StringBuilder b, final String language, final Map<String,
      String> translationsForLanguage, final Map<String, String> translationsForEn, final boolean checkMatchEn) {
        final String defaultEntry = translationsForLanguage.get("");
        int match = 0;
        int noMatch = 0;
        int matchEn = 0;
        for (final Map.Entry<String, String> e: translationsForLanguage.entrySet()) {
            final String defaultEn = translationsForEn.get("");
            if (e.getKey().length() == 0) {
                // is default: skip!
                if (checkMatchEn && defaultEn != null && defaultEntry != null && defaultEntry.equalsIgnoreCase(defaultEn)) {
                    ++matchEn;
                }
            } else {
                final String thisEntry = e.getValue();
                if (thisEntry != null) {
                    if (defaultEntry != null) {
                        if (defaultEntry.equalsIgnoreCase(thisEntry)) {
                            ++match;
                        } else {
                            ++noMatch;
                        }
                    }
                    if (checkMatchEn) {
                        if (thisEntry.equalsIgnoreCase(defaultEn)) {
                            ++matchEn;
                        } else if (thisEntry.equalsIgnoreCase(translationsForEn.get(e.getKey()))) {
                            ++matchEn;
                        }
                    }
                }
            }
        }
        addToLog(b, language, match, noMatch, matchEn);
    }
}
