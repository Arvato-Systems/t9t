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
package com.arvatosystems.t9t.client.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Singleton;

@Singleton
public class SystemConfigurationProvider extends DefaultsConfigurationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemConfigurationProvider.class);

    private static String lookup(final String name1, final String name2) {
        String val = System.getProperty(name1);
        if (val != null) {
            LOGGER.info("Obtained {} via JVM System property as {}", name1, val);
            return val;
        }
        val = System.getenv(name1);
        if (val != null) {
            LOGGER.info("Obtained {} via environment variable as {}", name1, val);
            return val;
        }
        val = System.getProperty(name2);
        if (val != null) {
            LOGGER.info("Obtained {} via JVM System property as {}", name2, val);
            return val;
        }
        val = System.getenv(name2);
        if (val != null) {
            LOGGER.info("Obtained {} via environment variable as {}", name2, val);
            return val;
        }
        LOGGER.info("Unable to find {} or {} via System property or environment variable - using default value", name1, name2);
        return null;
    }

    public SystemConfigurationProvider() {
        super(
            "SYSTEM",
            lookup("t9t.port", "PORT"),
            lookup("t9t.host", "HOST"),
            lookup("t9t.rpcpath", "RPCPATH")
        );
    }
}
