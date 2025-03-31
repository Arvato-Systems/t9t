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

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nonnull;

public final class Packages {
    private static final Logger LOGGER = LoggerFactory.getLogger(Packages.class);
    private static final String PROPERTIES_FILENAME = "/extraBonapartePrefixes.properties";
    private static final Map<String, String> EXTRA_PACKAGES = new ConcurrentHashMap<>();
    static {
        // obtain extra packages from some resource file, or initialize it to the empty map if no such resource can be found
        final Properties props = new Properties();
        try {
            props.load(Packages.class.getResourceAsStream(PROPERTIES_FILENAME));
        } catch (final Exception e) {
            // this is not an error
        }
        for (final Map.Entry<Object, Object> e: props.entrySet()) {
            if (e.getKey() instanceof String key && e.getValue() instanceof String value) {
                EXTRA_PACKAGES.put(key, value);
            }
        }
        LOGGER.info("{} extra package prefixes found in properties resource {}", EXTRA_PACKAGES.size(), PROPERTIES_FILENAME);
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
