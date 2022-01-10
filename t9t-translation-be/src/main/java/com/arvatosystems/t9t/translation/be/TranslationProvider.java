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
package com.arvatosystems.t9t.translation.be;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.init.InitContainers;
import com.arvatosystems.t9t.translation.services.ITranslationProvider;

import de.jpaw.bonaparte.enums.BonaTokenizableEnum;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

/**
 * Component storing translations of headers, enums, reports.
 *
 * <p>
 * Translations are imported from property files. Each file contains translations in one language only.
 * </p>
 * <p>
 * Header translations are imported from property files called headers_en.properties, headers_de_DE.properties, etc.
 * </p>
 * <p>
 * Report translations are imported from property files called report_en.properties, report_de_DE.properties, etc.
 * </p>
 * <p>
 * Enum translations are imported from property files called enums_en.properties, enums_de_DE.properties, etc.
 * </p>
 * <p>
 * Translations defined for global tenant are the default translations. They can be overridden for every other tenant.
 * </p>
 *
 * @author greg
 *
 */
@Singleton
public class TranslationProvider implements ITranslationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationProvider.class);
    private static final String REGEXP_FOR_INDEX = "\\[[0-9]+\\]";
    private static final Pattern PATTERN_FOR_INDEX = Pattern.compile(REGEXP_FOR_INDEX);

    // right now, only check for translations in the global tenant (TODO: make t9t config property, or tenant property)
    final boolean useLocalTenantTranslation = true;

    /** Returns an array of one to 4 languages which should be checked. */
    @Override
    public String[] resolveLanguagesToCheck(final String language, final boolean tryFallbackLanguages) {
        if (!tryFallbackLanguages && language != null)
            return new String[] { language };
        return MessagingUtil.getLanguagesWithFallback(language);
    }

    /**
     * Returns report translation. Translation value is taken from reports*.properties and defaults*.properties files.
     *
     * @param tenantId
     *            tenant Id
     * @param language
     *            language
     * @param reportId
     *            report Id
     * @param fieldName
     *            field name
     * @return translation value
     */
    @Override
    public String getReportTranslation(final String tenantId, final String language, final boolean tryFallbackLanguages,
      final String reportId, final String fieldName) {
        final String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
        return getTranslation(tenantId, langs, reportId, fieldName);
    }

    @Override
    public String getReportTranslation(final String tenantId, final Locale locale, final String reportId, final String fieldName) {
        return getReportTranslation(tenantId, locale.getLanguage(), true, reportId, fieldName);
    }

    /**
     * Returns header translation. Translation value is taken from headers*.properties and defaults*.properties files.
     *
     * @param tenantId
     *            tenant Id
     * @param language
     *            language
     * @param gridId
     *            grid Id
     * @param fieldName
     *            field name
     * @return translation value
     */
    @Override
    public String getHeaderTranslation(final String tenantId, final String language, final boolean tryFallbackLanguages,
      final String gridId, final String fieldName) {
        final String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
        return getTranslation(tenantId, langs, gridId, fieldName);
    }

    /**
     * Returns header translations for given filed names.
     *
     * @param tenantId
     *            tenant Id
     * @param language
     *            language
     * @param gridId
     *            grid Id
     * @param fieldNames
     *            field names
     * @return field name to translation mapping
     */
    @Override
    public List<String> getHeaderTranslations(final String tenantId, final String language, final boolean tryFallbackLanguages,
      final String gridId, final List<String> fieldNames) {
        final String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
        final List<String> trList = new ArrayList<>(fieldNames.size());

        for (final String fieldName : fieldNames) {
            final String tx = getTranslation(tenantId, langs, gridId, fieldName);
            // construct a technical text as fallback
            trList.add(tx != null ? tx : "${" + fieldName + "}");
        }

        return trList;
    }


    @Override
    public String getEnumTranslation(final String tenantId, final String language, final boolean tryFallbackLanguages,
      final String enumPqon, final String instanceName) {
        final String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
        final String translated = getTranslation(tenantId, langs, enumPqon, instanceName);
        return translated != null ? translated : instanceName;
    }

    /**
     * Returns header translations for given filed names.
     *
     * @param tenantId
     *            tenant Id
     * @param language
     *            language
     * @param gridId
     *            grid Id
     * @param fieldNames
     *            field names
     * @return field name to translation mapping
     */
    @Override
    public List<String> getEnumTranslations(final String tenantId, final String language, final boolean tryFallbackLanguages,
      final String enumPqon, final List<String> fieldNames) {
        final String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
        final List<String> trList = new ArrayList<>(fieldNames.size());

        for (final String fieldName : fieldNames) {
            final String tx = getTranslation(tenantId, langs, enumPqon, fieldName);
            // construct a technical text as fallback
            trList.add(tx != null ? tx : fieldName);  // no ${} here
        }
        return trList;
    }

    protected String getTranslation(final Map<String, String> translations, final String tenantId, final String lang,
      final String path, final String indexStr) {
        String translation = null;
        if (useLocalTenantTranslation) {
            // only check local if it is different from the global one
            if (!T9tConstants.GLOBAL_TENANT_ID.equals(tenantId)) {
                // first attempt in specific tenant - currently not done
                if (path != null)
                    translation = translations.get(TranslationsStack.makeKey(lang, tenantId, path));

                if (translation == null) {
                    translation = translations.get(TranslationsStack.makeKey(lang, tenantId, null));
                }
            }
        }
        if (translation == null) {
            if (path != null)
                translation = translations.get(TranslationsStack.makeKey(lang, T9tConstants.GLOBAL_TENANT_ID, path));

            if (translation == null) {
                // strip qualifiers, if any
                translation = translations.get(TranslationsStack.makeKey(lang, T9tConstants.GLOBAL_TENANT_ID, null));
            }
        }
        if (indexStr != null && translation != null) {
            /// parse the old index, add 1, and use it as new index
            String replacement = " #?";
            try {
                final int oldIndex = Integer.parseInt(indexStr.substring(1, indexStr.length() - 1));
                replacement = String.format(" #%d", oldIndex + 1);
            } catch (final Exception e) {
                LOGGER.error("Badly formatted index for {}[{}]: {}", path, indexStr, translation);
                replacement = indexStr;
            }
            translation = translation.replace("#", replacement);
        }
        return translation;
    }

    /** Common evaluation method for all other external entry points. */
    @Override
    public String getTranslation(final String tenantId, final String[] langs, final String path, final String fieldname) {
        Map<String, String> translations = TranslationsStack.getTranslationsForField(fieldname);
        String indexStr = null;

        if (translations == null) {
            // fallback 1: check for arrays
            final int bracketPos1 = fieldname.indexOf('[');
            if (bracketPos1 > 0) {
                // try the wildcard ID
                translations = TranslationsStack.getTranslationsForField(fieldname.replaceAll(REGEXP_FOR_INDEX, "*"));
            }
            if (translations != null) {
                // fallback 1 worked: set indexStr
                final Matcher m = PATTERN_FOR_INDEX.matcher(fieldname);
                if (m.find(bracketPos1)) {
                    indexStr = m.group();
                }
            } else {
                // fallback 2: check for base ID
                final int lastDot = fieldname.lastIndexOf('.');
                if (lastDot > 0) {
                    // no dots, only last component
                    final String basename = fieldname.substring(lastDot + 1);
                    translations = TranslationsStack.getTranslationsForField(basename);
                    if (translations == null && bracketPos1 > 0) {
                        // fallback 3: check for arrays in last component
                        final int bracketPos2 = basename.indexOf('[');
                        if (bracketPos2 > 0) {
                            translations = TranslationsStack.getTranslationsForField(basename.replaceAll(REGEXP_FOR_INDEX, "*"));
                            if (translations != null) {
                                // fallback 3 worked: set indexStr
                                final Matcher m = PATTERN_FOR_INDEX.matcher(basename);
                                if (m.find(bracketPos2)) {
                                    indexStr = m.group();
                                }
                            }
                        }
                    }
                }
                if (translations == null) {
                    // still no translation: it does not exist!
                    return null;
                }
            }
        }

        String translation = null;
        for (final String lang : langs) { // usually country specific and general language, ex.: en_US, en
            translation = getTranslation(translations, tenantId, lang, path, indexStr);
            if (translation != null) {
                break;
            }
        }

        return translation;
    }

    /**
     * Returns a map of translations for all enum symbols.
     *
     * @param enumPQON enum class
     * @param language language in which translation is needed
     * @param tenantId ID of a tenant requesting for translations
     * @return the translation
     */
    @Override
    public Map<String, String> getEnumTranslation(final String tenantId, final String enumPQON, final String language, final boolean tryFallbackLanguages) {
        final String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
        // get all instance names of the enum
        final EnumDefinition ed = InitContainers.getEnumByPQON(enumPQON);
        if (ed == null)
            throw new ApplicationException(T9tException.NOT_AN_ENUM, enumPQON);

        final Map<String, String> translations = new HashMap<>(ed.getIds().size() * 2);

        for (final String instanceName : ed.getIds()) {
            final String instanceTranslated = getTranslation(tenantId, langs, enumPQON, instanceName);
            translations.put(instanceName, instanceTranslated != null ? instanceTranslated : instanceName);
        }
        return translations;
    }

    /**
     * Returns translation for the given enum.
     *
     * @param enu
     *            enum
     * @param tenantId
     *            ID of a tenant requesting for translations
     * @param language
     *            language in which translation is needed
     * @return enum translation
     */
    @Override
    public <T extends BonaTokenizableEnum> String getEnumTranslation(final T enu, final String tenantId, final String language) {
        final String enumPQON = enu.ret$PQON();
        final String instanceName = enu.name();
        final String[] langs = new String[] { language };
        final String instanceTranslated = getTranslation(tenantId, langs, enumPQON, instanceName);
        if (instanceTranslated != null)
            return instanceTranslated;

        LOGGER.warn("No translation was found for enum: {}, tenantID: {}, language: {}", enu, tenantId, language);
        return null;
    }
}
