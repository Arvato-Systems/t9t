/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a property file (path to this file given by VM argument -DbasePropertyFile).
 */

public final class BaseConfigurationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseConfigurationProvider.class);

    private BaseConfigurationProvider() {
    }

    public static Properties getBaseProperties() {
        final Properties properties = new Properties();

        // if propertyFile parameter exists, use this file instead of the one from the project
        final String basePropertyFileName = System.getProperty("basePropertyFile");
        if (basePropertyFileName != null) {
            final File fileFromVmArgument = new File(basePropertyFileName);
            final File file = fileFromVmArgument.canRead() ? fileFromVmArgument : null;

            if (file == null) {
                LOGGER.error("Cannot find following base property file: {}", basePropertyFileName);
                throw new T9tException(T9tException.INVALID_CONFIGURATION, "Base configuration file not found");
            } else {
                LOGGER.info("Use following base property file: {}", file.getAbsolutePath());
            }

            // read file
            BufferedInputStream stream = null;
            try {
                stream = new BufferedInputStream(new FileInputStream(file));
                properties.load(stream);
            } catch (final FileNotFoundException e1) {
                LOGGER.error("Cannot find following base property file: {}", file.getAbsolutePath());
            } catch (final IOException e) {
                LOGGER.error("Cannot load following base property file: {}", file.getAbsolutePath());
            } finally {
                 try {
                    stream.close();
                } catch (final IOException e) {
                    LOGGER.error("Cannot close stream to following base property file: {}", file.getAbsolutePath());
                }
            }

        } else {
            LOGGER.info("No VM argument for base property file name (-DbasePropertyFileName=...) (including resetPassword API Key) given!");
        }

        return properties;
    }
}
