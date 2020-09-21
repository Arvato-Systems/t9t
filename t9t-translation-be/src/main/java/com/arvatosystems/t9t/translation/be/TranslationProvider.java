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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    final boolean useLocalTenantTranslation = true;  // right now, only check for translations in the global tenant (TODO: make t9t config property, or tenant property)


    /** Returns an array of one to 4 languages which should be checked. */
    @Override
    public String[] resolveLanguagesToCheck(String language, boolean tryFallbackLanguages) {
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
    public String getReportTranslation(String tenantId, String language, boolean tryFallbackLanguages, String reportId, String fieldName) {
        String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
        return getTranslation(tenantId, langs, reportId, fieldName);
    }

    @Override
    public String getReportTranslation(String tenantId, Locale locale, String reportId, String fieldName) {
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
    public String getHeaderTranslation(String tenantId, String language, boolean tryFallbackLanguages, String gridId, String fieldName) {
        String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
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
    public List<String> getHeaderTranslations(String tenantId, String language, boolean tryFallbackLanguages, String gridId, List<String> fieldNames) {
        String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
        List<String> trList = new ArrayList<>(fieldNames.size());

        for (String fieldName : fieldNames) {
            String tx = getTranslation(tenantId, langs, gridId, fieldName);
            // construct a technical text as fallback
            trList.add(tx != null ? tx : "${" + fieldName + "}");
        }

        return trList;
    }


    @Override
    public String getEnumTranslation(String tenantId, String language, boolean tryFallbackLanguages, String enumPqon, String instanceName) {
        String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
        String translated = getTranslation(tenantId, langs, enumPqon, instanceName);
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
    public List<String> getEnumTranslations(String tenantId, String language, boolean tryFallbackLanguages, String enumPqon, List<String> fieldNames) {
        String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
        List<String> trList = new ArrayList<>(fieldNames.size());

        for (String fieldName : fieldNames) {
            String tx = getTranslation(tenantId, langs, enumPqon, fieldName);
            // construct a technical text as fallback
            trList.add(tx != null ? tx : fieldName);  // no ${} here
        }
        return trList;
    }

    protected String getTranslation(Map<String, String> translations, String tenantId, String lang, String path, String indexStr) {
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
                final int oldIndex = Integer.parseInt(indexStr);
                replacement = String.format(" #%d", oldIndex+1);
            } catch(Exception e) {
                LOGGER.error("Badly formatted index for {}[{}]: {}", path, indexStr, translation);
                replacement = indexStr;
            }
            translation = translation.replace("#", replacement);
        }
        return translation;
    }

    @Override
    public String getTranslation(String tenantId, String[] langs, String path, String fieldname) {
        Map<String, String> translations = TranslationsStack.getTranslationsForField(fieldname);
        String indexStr = null;

        if (translations == null) {
            // fallback 1: check for arrays
            final int bracketPos1 = fieldname.indexOf('[');
            if (bracketPos1 > 0) {
                // try the wildcard ID
                translations = TranslationsStack.getTranslationsForField(fieldname.substring(0, bracketPos1) + "*");
            }
            if (translations != null) {
                // fallback 1 worked: set indexStr
                final int bracketPos2 = fieldname.indexOf(']');
                if (bracketPos2 > bracketPos1) {
                    indexStr = fieldname.substring(bracketPos1+1, bracketPos2);
                }
            } else {
                // fallback 2: check for base ID
                int lastDot = fieldname.lastIndexOf('.');
                if (lastDot > 0) {
                    // no dots, only last component
                    final String basename = fieldname.substring(lastDot+1);
                    translations = TranslationsStack.getTranslationsForField(basename);
                    if (translations == null && bracketPos1 > 0) {
                        // fallback 3: check for arrays in last component
                        final int bracketPos2 = basename.indexOf('[');
                        if (bracketPos2 > 0) {
                            translations = TranslationsStack.getTranslationsForField(basename.substring(0, bracketPos2) + "*");
                            if (translations != null) {
                                // fallback 3 worked: set indexStr
                                final int bracketPos3 = fieldname.indexOf(']');
                                if (bracketPos3 > bracketPos2) {
                                    indexStr = fieldname.substring(bracketPos2+1, bracketPos3);
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
        for (String lang : langs) { // usually country specific and general language, ex.: en_US, en
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
    public Map<String, String> getEnumTranslation(String tenantId, String enumPQON, String language, boolean tryFallbackLanguages) {
        String[] langs = resolveLanguagesToCheck(language, tryFallbackLanguages);
        // get all instance names of the enum
        EnumDefinition ed = InitContainers.getEnumByPQON(enumPQON);
        if (ed == null)
            throw new ApplicationException(T9tException.NOT_AN_ENUM, enumPQON);

        Map<String, String> translations = new HashMap<>(ed.getIds().size() * 2);

        for (String instanceName : ed.getIds()) {
            String instanceTranslated = getTranslation(tenantId, langs, enumPQON, instanceName);
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
    public <T extends BonaTokenizableEnum> String getEnumTranslation(T enu, String tenantId, String language) {
        String enumPQON = enu.ret$PQON();
        String instanceName = enu.name();
        String [] langs = new String [] { language };
        String instanceTranslated = getTranslation(tenantId, langs, enumPQON, instanceName);
        if (instanceTranslated != null)
            return instanceTranslated;

        LOGGER.warn("No translation was found for enum: {}, tenantID: {}, language: {}", enu, tenantId, language);
        return null;
    }
}
