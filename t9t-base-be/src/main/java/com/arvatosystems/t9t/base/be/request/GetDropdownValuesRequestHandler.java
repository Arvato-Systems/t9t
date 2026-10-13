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
package com.arvatosystems.t9t.base.be.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Currency;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.request.GetDropdownValuesRequest;
import com.arvatosystems.t9t.base.request.GetDropdownValuesResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

/**
 * Returns the values of one of the well-known, runtime-backed UI dropdowns (time zone, charset, country, language, currency).
 */
public class GetDropdownValuesRequestHandler extends AbstractReadOnlyRequestHandler<GetDropdownValuesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetDropdownValuesRequestHandler.class);

    private static final String LANGUAGES_FILE_DIRECTORY  = "translations/";
    private static final String DEFAULT_LANGUAGES_FILE    = "supported_languages.properties";
    private static final String CLIENT_LANGUAGES_FILE     = "client_languages.properties";
    private static final String COMMENT_SIGN              = "#";

    private static final List<String> TIME_ZONE_IDS       = List.copyOf(ZoneId.getAvailableZoneIds());
    private static final List<String> CHARSET_ENCODINGS   = List.copyOf(Charset.availableCharsets().keySet());
    private static final List<String> COUNTRY_CODES       = buildCountryCodes();
    private static final List<String> LANGUAGE_CODES      = readSupportedLanguages();
    private static final List<String> CURRENCY_CODES      = buildCurrencyCodes();

    private static List<String> buildCountryCodes() {
        final List<String> countries = new ArrayList<>();
        countries.add("XX");  // used for wildcards in t9t doc module
        countries.add("XK");  // inofficial code for Kosovo
        countries.add("EU");  // sometimes used for European Union, will be mapped to specific code by the backend
        countries.addAll(Arrays.stream(Locale.getISOCountries()).sorted().collect(Collectors.toList()));
        return List.copyOf(countries);
    }

    private static List<String> buildCurrencyCodes() {
        final List<String> currencies = Currency.getAvailableCurrencies().stream()
                .sorted(Comparator.comparing(Currency::getCurrencyCode))
                .map(Currency::getCurrencyCode).collect(Collectors.toList());
        return List.copyOf(currencies);
    }

    @Override
    public GetDropdownValuesResponse execute(final RequestContext ctx, final GetDropdownValuesRequest request) {
        final GetDropdownValuesResponse resp = new GetDropdownValuesResponse();
        resp.setValues(switch (request.getDropdownId()) {
            case "timeZoneId"      -> TIME_ZONE_IDS;
            case "charsetEncoding" -> CHARSET_ENCODINGS;
            case "countryCode"     -> COUNTRY_CODES;
            case "languageCode"    -> LANGUAGE_CODES;
            case "currencyCode"    -> CURRENCY_CODES;
            default                -> throw new T9tException(T9tException.INVALID_PARAMETER, request.getDropdownId());
        });
        return resp;
    }

    /**
     * Reads all classpath 'translations/client_languages.properties' files and, if they yield no languages,
     * falls back to 'translations/supported_languages.properties'. Entries are trimmed, de-duplicated and
     * returned sorted, in contrast to the translation module importer which returns a hash set.
     */
    private static List<String> readSupportedLanguages() {
        Set<String> languages = readLanguages(CLIENT_LANGUAGES_FILE);
        if (languages.isEmpty()) {
            LOGGER.info("Client languages are empty, proceeding with default supported languages.");
            languages = readLanguages(DEFAULT_LANGUAGES_FILE);
        }
        final List<String> result = new ArrayList<>(languages);
        result.sort(Comparator.naturalOrder());
        return List.copyOf(result);
    }

    private static Set<String> readLanguages(final String fileName) {
        final Set<String> languages = new LinkedHashSet<>();
        LOGGER.info("Reading property file with supported languages, file {}", fileName);

        final ClassLoader cl = GetDropdownValuesRequestHandler.class.getClassLoader();
        try {
            final Enumeration<URL> urls = cl.getResources(LANGUAGES_FILE_DIRECTORY + fileName);
            while (urls.hasMoreElements()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(urls.nextElement().openStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.isBlank() || line.startsWith(COMMENT_SIGN)) {
                            continue;
                        }
                        languages.add(line.trim());
                    }
                } catch (final IOException e) {
                    LOGGER.error("Reading property file with supported languages failed, file " + fileName + ".", e);
                }
            }
        } catch (final IOException e) {
            LOGGER.error("Reading property file with supported languages failed, file " + fileName + ".", e);
        }
        LOGGER.info("Languages supported in translations read successfully. Found {} languages in file {}", languages.size(), fileName);
        return languages;
    }
}
