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
package com.arvatosystems.t9t.translation.services;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import de.jpaw.bonaparte.enums.BonaTokenizableEnum;

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

    String getReportTranslation(String tenantId, String language, boolean tryFallbackLanguages, String reportId, String fieldName);
    String getReportTranslation(String tenantId, Locale locale, String reportId, String fieldName);

    String getHeaderTranslation(String tenantId, String language, boolean tryFallbackLanguages, String gridId, String fieldName);
    List<String> getHeaderTranslations(String tenantId, String language, boolean tryFallbackLanguages, String gridId, List<String> fieldNames);

    String getEnumTranslation(String tenantId, String language, boolean tryFallbackLanguages, String enumPqon, String fieldName);
    List<String> getEnumTranslations(String tenantId, String language, boolean tryFallbackLanguages, String enumPqon, List<String> fieldNames);

    Map<String, String> getEnumTranslation(String tenantId, String enumPQON, String language, boolean tryFallbackLanguages);
    // needed for IO
    <T extends BonaTokenizableEnum> String getEnumTranslation(T enu, String tenantId, String language);
}
