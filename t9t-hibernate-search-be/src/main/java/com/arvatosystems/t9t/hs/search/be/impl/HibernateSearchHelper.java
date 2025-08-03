package com.arvatosystems.t9t.hs.search.be.impl;

public final class HibernateSearchHelper {

    private HibernateSearchHelper() {
        // Utility class - private constructor to prevent instantiation
    }

    protected static <T> Long extractId(T entity, String resultFieldName) {

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
