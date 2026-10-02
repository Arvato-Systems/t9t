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
package com.arvatosystems.t9t.cfg;

import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import jakarta.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Packages {
    private static final Logger LOGGER = LoggerFactory.getLogger(Packages.class);
    private static final String PROPERTIES_FILENAME = "extraBonapartePrefixes.properties";
    private static final Map<String, String> EXTRA_PACKAGES = new ConcurrentHashMap<>();
    static {
        // scan all resource files of given name
        final ClassLoader cl = Packages.class.getClassLoader();
        try {
            final Enumeration<URL> urls = cl.getResources(PROPERTIES_FILENAME);

            while (urls.hasMoreElements()) {
                final URL nextUrl = urls.nextElement();
                try {
                    try (java.io.InputStream stream = nextUrl.openStream()) {
                        final Properties props = new Properties();
                        props.load(stream);
                        for (final Map.Entry<Object, Object> e: props.entrySet()) {
                            if (e.getKey() instanceof String key && e.getValue() instanceof String value) {
                                EXTRA_PACKAGES.put(key, value);
                                LOGGER.info("Mapping prefix {} to package {} (resource {})", e.getKey(), e.getValue(), nextUrl);
                            }
                        }
                    }
                } catch (final Exception e) {
                    LOGGER.error("Error reading extra package prefixes from resource {}", nextUrl, e);
                }
            }
            if (EXTRA_PACKAGES.isEmpty()) {
                LOGGER.info("No extra bonaparte package prefixes found in any resource {}", PROPERTIES_FILENAME);
            }
        } catch (final Exception e) {
            LOGGER.error("Error reading extra package prefixes from resources {}", PROPERTIES_FILENAME, e);
        }
    }

    private Packages() {
    }

    public static int numberOfExtraPackages() {
        return EXTRA_PACKAGES.size();
    }
    /**
     * Implementation emits any pair of prefix / package pair to the processor.
     *
     * @param processor takes parameters prefix and package name
     */
    public static void walkExtraPackages(@Nonnull final BiConsumer<String, String> processor) {
        for (final Map.Entry<String, String> e: EXTRA_PACKAGES.entrySet()) {
            processor.accept(e.getKey(), e.getValue());
        }
    }
}
