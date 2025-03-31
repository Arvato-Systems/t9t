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
package com.arvatosystems.t9t.translation.services;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.jpaw.bonaparte.enums.BonaTokenizableEnum;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface ITranslationProvider {
    /** Returns an array of primary and possible fallback language for translation lookup.
     * If tryFallbackLanguages is false, it is a singleton array of the input language,
     * otherwise it is an array consisting of
     * - the input language
     * - the input language, reduced to the first 5 characters (ll_CC)
     * - the input reduced to the first 2 characters (ll, ISO 639 language code)
     * - en
     * Any duplicate entries are removed.
     * */
    String[] resolveLanguagesToCheck(String language, boolean tryFallbackLanguages);

    /** Generic translation resolver. For the given input languages, the system looks for a translation
     * of fieldname, which may not be null.
     * Normally the fieldname does not contain dots, but in certain cases a custom separation of path and fieldname is acceptable.
     * field names in defaults translation files are assumed to have no path.
     * path is a partially qualified pathname which is prepended to fieldname, or null if no path exists.
     * The lookup attempts to resolve the fully qualified name, and falls back to the fieldname without path
     * (default translation). */
    String getTranslation(String tenantId, String[] langs, String path, String fieldname);

    /**
     * Returns report translation. Translation values are taken from reports*.properties and defaults*.properties files.
     *
     * @param tenantId the tenant Id
     * @param language the language to use
     * @param tryFallbackLanguages true if a default language should be used if no translation is found for the current ones
     * @param reportId the ID of the report
     * @param fieldName the field within the report
     *
     * @return translated text
     */
    @Nullable String getReportTranslation(@Nonnull String tenantId, @Nullable String language, boolean tryFallbackLanguages, @Nonnull String reportId,
        @Nonnull String fieldName);

    /**
     * Returns report translation. Translation values are taken from reports*.properties and defaults*.properties files.
     * Invokes <code>getReportTranslation(tenantId, locale.getLanguage(), true, reportId, fieldName)</code>
     *
     * @param tenantId the tenant Id
     * @param language the language to use
     * @param reportId the ID of the report
     * @param fieldName the field within the report
     *
     * @return translated text
     */
    @Nullable String getReportTranslation(@Nonnull String tenantId, @Nonnull Locale locale, @Nonnull String reportId, @Nonnull String fieldName);

    /**
     * Returns report translation. Translation values are taken from reports*.properties and defaults*.properties files.
     * Invokes <code>getReportTranslation(tenantId, locale.getLanguage(), true, reportId, fieldName)</code>
     *
     * @param tenantId the tenant Id
     * @param language the language to use
     * @param reportId the ID of the report
     * @param fieldName the field within the report
     * @param fallbackText the default text to use if no translation has been found.
     *
     * @return translated text
     */
    @Nonnull String getReportTranslation(@Nonnull String tenantId, @Nonnull Locale locale, @Nonnull String reportId, @Nonnull String fieldName,
        @Nonnull String fallbackText);

    String getHeaderTranslation(String tenantId, String language, boolean tryFallbackLanguages, String gridId, String fieldName);
    List<String> getHeaderTranslations(String tenantId, String language, boolean tryFallbackLanguages, String gridId, List<String> fieldNames);

    String getEnumTranslation(String tenantId, String language, boolean tryFallbackLanguages, String enumPqon, String fieldName);
    List<String> getEnumTranslations(String tenantId, String language, boolean tryFallbackLanguages, String enumPqon, List<String> fieldNames);

    Map<String, String> getEnumTranslation(String tenantId, String enumPQON, String language, boolean tryFallbackLanguages);
    // needed for IO
    <T extends BonaTokenizableEnum> String getEnumTranslation(T enu, String tenantId, String language);
}
