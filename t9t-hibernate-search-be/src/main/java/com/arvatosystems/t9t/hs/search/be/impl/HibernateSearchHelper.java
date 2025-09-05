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
        if (fieldName != null && !fieldName.isEmpty()) {
            // Determine which fulltext fields to use based on the fieldName
            fuzzyFulltextFields = (fulltextFields.get(Boolean.TRUE) != null && fulltextFields.get(Boolean.TRUE)
                    .contains(fieldName))
                    ? Set.of(fieldName)
                    : null;
            exactFulltextFields = (fulltextFields.get(Boolean.FALSE) != null && fulltextFields.get(Boolean.FALSE)
                    .contains(fieldName))
                    ? Set.of(fieldName)
                    : null;
        } else {
            // Use all fulltext fields if no specific fieldName is provided
            fuzzyFulltextFields = fulltextFields.get(Boolean.TRUE);
            exactFulltextFields = fulltextFields.get(Boolean.FALSE);
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
                            // Fuzzy-Match (Boost 1.0)
                            var m = factory.match()
                                    .fields(fieldnames)
                                    .matching(term);
                            if (getFuzziness(term) > 0) {
                                m = m.minimumShouldMatchNumber(Math.min(term.length(), 3)).fuzzy(getFuzziness(term));
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
        return (fieldName != null && !fieldName.isEmpty() && fields.contains(fieldName))
                ? new String[]{fieldName}
                : (fieldName == null || fieldName.isEmpty()
                    ? fields.toArray(String[]::new)
                    : null);
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
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
}
