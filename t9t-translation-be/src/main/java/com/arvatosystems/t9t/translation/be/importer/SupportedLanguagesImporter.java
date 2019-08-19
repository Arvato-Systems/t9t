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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports information about supported languages within system. <br/>
 * All files located on the classpath matching 'translations/supported_languages.properties' pattern are taken into account. Every file is expected to contain
 * one language code in single line.
 *
 * @author greg
 *
 */
public class SupportedLanguagesImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupportedLanguagesImporter.class);

    private static final String SUPPORTED_LANGUAGES_FILE_NAME = "translations/supported_languages.properties";
    public static final String COMMENT_SIGN = "#";
    /**
     * Returns collection of supported languages by system.
     *
     * @return
     */
    public Set<? extends String> readSupportedLanguages() {
        Set<String> su = new HashSet<>();
        LOGGER.info("Reading property file with languages supported in translations of headers and enums");

        ClassLoader cl = this.getClass().getClassLoader();
        try {
            Enumeration<URL> urls = cl.getResources(SUPPORTED_LANGUAGES_FILE_NAME);

            while (urls.hasMoreElements()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(urls.nextElement().openStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.isEmpty() || line.startsWith(COMMENT_SIGN)) {
                            continue;
                        }

                        su.add(line.trim());
                    }
                } catch (IOException e) {
                    LOGGER.error("Reading property file with supported languages failed.", e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Reading property file with supported languages failed.", e);
        }

        LOGGER.info("Languages supported in translations read successfully. Found {} languages.", su.size());
        return su;
    }
}
