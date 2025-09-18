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
package com.arvatosystems.t9t.hs.search.be.impl;

import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.HibernateSearchConfiguration;
import com.arvatosystems.t9t.hs.configurate.be.core.impl.EntityConfigurer;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class HibernateSearchHelper {

    private static final HibernateSearchConfiguration HIBERNATE_SEARCH_CONFIGURATION = ConfigProvider.getConfiguration().getHibernateSearchConfiguration();

    private HibernateSearchHelper() {
        // Utility class - private constructor to prevent instantiation
    }

    static int getFuzzyIntForAValue (String value) {
        return (value.length() >= 7 ? 2 : 1);
    }

    static int getFuzziness(String value) {
        if (value == null) {
            return 0;
        }
        return HIBERNATE_SEARCH_CONFIGURATION == null || HIBERNATE_SEARCH_CONFIGURATION.getFuzzySearchLevel() == null
                ? getFuzzyIntForAValue(value)
                : HIBERNATE_SEARCH_CONFIGURATION.getFuzzySearchLevel();
    }

    static PredicateFinalStep getBool(SearchPredicateFactory factory, String entityName, String fieldName, String value) {

        if (value == null || value.isBlank()) {
            return factory.matchAll();
        }

        // Retrieve keyword fields for the entity
        Set<String> keywordFields = EntityConfigurer.getCachedKeywordFields().get(entityName);
        Map<Boolean, Set<String>> fulltextFields = EntityConfigurer.getCachedFullTextFields().get(entityName);
        if ((keywordFields == null || keywordFields.isEmpty()) && (fulltextFields == null || fulltextFields.isEmpty())) {
            return factory.matchAll();
        }

        // Retrieve keyword fulltext fields for the entity
        final Set<String> fuzzyFulltextFields;
        final Set<String> exactFulltextFields;
        if (fulltextFields != null && !fulltextFields.isEmpty()) {
            // Cache the fulltext field sets to avoid repeated map lookups
            final Set<String> fuzzyFields = fulltextFields.get(Boolean.TRUE);
            final Set<String> exactFields = fulltextFields.get(Boolean.FALSE);
            final boolean hasFieldName = fieldName != null && !fieldName.isEmpty();

            if (hasFieldName) {
                // Determine which fulltext fields to use based on the fieldName
                fuzzyFulltextFields = (fuzzyFields != null && fuzzyFields.contains(fieldName))
                        ? Set.of(fieldName)
                        : null;
                exactFulltextFields = (exactFields != null && exactFields.contains(fieldName))
                        ? Set.of(fieldName)
                        : null;
            } else {
                // Use all fulltext fields if no specific fieldName is provided
                fuzzyFulltextFields = fuzzyFields;
                exactFulltextFields = exactFields;
            }
        } else {
            fuzzyFulltextFields = null;
            exactFulltextFields = null;
        }

        // Preprocess the value string by replacing '%' and '_' before splitting into terms
        String processedValue = value.trim().replace("%", "*").replace("_", "?");
        Set<String> terms = new LinkedHashSet<>();
        for (String t : processedValue.split("\\s+")) {
            if (!t.isEmpty()) terms.add(t);
        }
        if (terms.isEmpty()) return factory.matchAll();

        // Build the boolean predicate
        return factory.bool(outer -> {
            for (String term : terms) {
                outer.should(factory.bool(inner -> {
                    boolean added = false;
                    if (keywordFields != null && keywordFields.size() > 0) {
                        // Keyword-Match (Boost 5.0)
                        final String[] fieldnames = findFieldnames(fieldName, keywordFields);
                        if (fieldnames != null && fieldnames.length > 0) {
                            inner.should(
                                    factory.match()
                                            .fields(fieldnames)
                                            .matching(term)
                                            .boost(5.0f)
                            );
                            added = true;
                        }
                    }
                    if (fuzzyFulltextFields != null) {
                        final String[] fieldnames = findFieldnames(fieldName, fuzzyFulltextFields);
                        if (fieldnames != null && fieldnames.length > 0) {
                            // Wildcard-Match (Boost 3.0)
                            inner.should(factory.wildcard().fields(fieldnames).matching(term).boost(3.0f));
                            // Fuzzy-Match (Boost 1.0) - cache fuzziness value to avoid repeated calculation
                            var m = factory.match()
                                    .fields(fieldnames)
                                    .matching(term);
                            final int fuzziness = getFuzziness(term);
                            if (fuzziness > 0) {
                                m = m.minimumShouldMatchNumber(Math.min(term.length(), 3)).fuzzy(fuzziness);
                            }
                            inner.should(m);
                            added = true;
                        }
                    }
                    if (exactFulltextFields != null) {
                        final String[] fieldnames = findFieldnames(fieldName, exactFulltextFields);
                        if (fieldnames != null && fieldnames.length > 0) {
                            // Wildcard-Match (Boost 3.0)
                            inner.should(
                                    factory.wildcard()
                                            .fields(fieldnames)
                                            .matching(term)
                                            .boost(3.0f)
                            );
                            added = true;
                        }
                    }
                    if (added) {
                        inner.minimumShouldMatchNumber(1);
                    }
                }));
            }
            outer.minimumShouldMatchNumber(1);
        });
    }

    private static String[] findFieldnames(String fieldName, Set<String> fields) {
        return fieldName == null || fieldName.isEmpty()
            ? fields.toArray(String[]::new)
            : fields.contains(fieldName)
                ? new String[]{fieldName}
                : null;
    }

    static <T> Long extractId(T entity, String resultFieldName) {

        try {
            if (entity == null || resultFieldName == null || resultFieldName.isBlank()) {
                return null;
            }

            // Try to access the getter method first
            String getterName = "get" + Character.toUpperCase(resultFieldName.charAt(0)) + resultFieldName.substring(1);
            try {
                var getter = entity.getClass().getMethod(getterName);
                Long value = getLongValue(getter.invoke(entity));
                if (value != null) return value;
            } catch (Exception ignored) {
                // ignore
            }
            // Fallback to field access
            var field = entity.getClass().getDeclaredField(resultFieldName);
            field.setAccessible(true);
            return getLongValue(field.get(entity));
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static Long getLongValue(Object value) {
        if (value instanceof Long l) {
            return l;
        } else if (value instanceof Number n) {
            return n.longValue();
        }
        return null;
    }
}
