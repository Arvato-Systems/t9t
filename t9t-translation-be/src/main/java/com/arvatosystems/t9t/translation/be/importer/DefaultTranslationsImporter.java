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
package com.arvatosystems.t9t.translation.be.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.translation.be.TranslationsStack;

import de.jpaw.dp.Startup;

/**
 * Imports translations from property files located on the classpath. <br/>
 * All files matching given pattern are taken into account.
 * <p>
 * Single entry in property file is build in the following way:<br/>
 * tenantId.subkeyGroup.subkeyName=Label <br/>
 * ex.: @.report/data_sink/1.dataSinkId=DataSinkDTO Id
 * </p>
 * subkeyGroup defines a group particular translation belongs to. it is "default" for defaults.<br/>
 * subkeyName is a most detailed part of a key. In most of the cases subkeyName represents field name.
 *
 * @author greg
 *
 */
@Startup(888)
public class DefaultTranslationsImporter {
    public static final String COMMENT_SIGN = "#";
    public static final String PROPERTY_FILE_CHARSET = "UTF-8";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTranslationsImporter.class);
    /**
     * Holds language codes of languages for which translations are available.
     */
    static private Set<String> supportedLanguages;

    /** Reads all property files for languages listed in the resource file supported_languages.properties. */
    public static void onStartup() {
        supportedLanguages = Collections.unmodifiableSet(new SupportedLanguagesImporter().readSupportedLanguages());

        TranslationsStack.reset();
        DefaultTranslationsImporter.readTranslationFiles("headers");
        DefaultTranslationsImporter.readTranslationFiles("report");
        DefaultTranslationsImporter.readTranslationFiles("enums");
        DefaultTranslationsImporter.readTranslationFiles("defaults");
        DefaultTranslationsImporter.readTranslationFiles("ui");

        TranslationsStack.dump(false);
    }

    /**
     * Reads translations from property files matching given filePattern. Only translations in selected languages are imported.
     *
     * @param filePattern
     * @param fileTypeName
     * @param supportedLanguages
     * @return
     */
    static private void readTranslationFiles(final String fileTypeName) {
        LOGGER.info("Reading property files with {} translations", fileTypeName);
        final boolean isDefaults = fileTypeName.equals("defaults");

        String fileName;
        for (String language : supportedLanguages) {
            LOGGER.info("Reading translations for language {}", language);
            fileName = "translations/" + fileTypeName + "_" + language + ".properties";

            ClassLoader cl = TranslationsStack.class.getClassLoader();
            try {
                Enumeration<URL> urls = cl.getResources(fileName);

                while (urls.hasMoreElements()) {
                    int count = 0;
                    URL nextUrl = urls.nextElement();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(nextUrl.openStream(), PROPERTY_FILE_CHARSET))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (line.trim().isEmpty() || line.startsWith(COMMENT_SIGN)) {
                                continue;
                            }

                            if (parseAndStoreLine(language, line, isDefaults))
                                ++count;
                        }
                    } catch (IOException e) {
                        LOGGER.error("Reading property file with {} translations failed.", fileTypeName);
                        LOGGER.error("Exception", e);
                    }
                    LOGGER.debug("Property files with {} translations read: {} translations read for language {} from {}",
                            fileTypeName, count, language, nextUrl);
                }
            } catch (IOException e) {
                LOGGER.error("Reading property file with {} translations failed.", fileTypeName);
                LOGGER.error("Exception", e);
            }
        }
    }

    // return true in case of success, else false
    private static boolean parseAndStoreLine(final String language, final String line, boolean isDefaults) {
        String[] lineParts = line.split("=");
        if (lineParts.length != 2) {
            LOGGER.warn("Line with translation won't be parsed since it doesn't follow 'key=value' pattern. Line: {}", line);
            return false;
        }
        final String key = lineParts[0].trim();

        String tenant = T9tConstants.GLOBAL_TENANT_ID;
        String fieldAndPathName = key;

        int firstDot = key.indexOf('.');
        if (firstDot >= 0) {
            // at least one dot: assume the line starts with a tenant ID
            tenant = key.substring(0, firstDot);
            fieldAndPathName = key.substring(firstDot+1);
            if (fieldAndPathName.startsWith("defaults.")) {
                fieldAndPathName = fieldAndPathName.substring(9);
                isDefaults = true;
            }
        }
        int lastDot = fieldAndPathName.lastIndexOf(':');   // preference is to split using a colon
        if (lastDot < 0)
            lastDot = fieldAndPathName.lastIndexOf('.');   // use a dot as a fallback
        if (isDefaults || lastDot < 0) {
            // do not split off a path, it's all the field name, either there is no prefix or it is put into defaults intentionally
            TranslationsStack.addTranslation(language, tenant, null, fieldAndPathName, lineParts[1].trim());
        } else {
            String fieldName = fieldAndPathName.substring(lastDot+1);
            String path = fieldAndPathName.substring(0, lastDot);
            TranslationsStack.addTranslation(language, tenant, path, fieldName, lineParts[1].trim());
        }
        return true;
    }
}
