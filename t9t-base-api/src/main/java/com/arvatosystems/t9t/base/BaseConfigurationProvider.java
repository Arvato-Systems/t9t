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
package com.arvatosystems.t9t.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;

/**
 * Reads a property file (path to this file given by VM argument -DbasePropertyFile).
 */

public class BaseConfigurationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseConfigurationProvider.class);

    public static Properties getBaseProperties() {
        Properties properties = new Properties();
        File file = null;

        // if propertyFile parameter exists, use this file instead of the one from the project
        if (System.getProperty("basePropertyFile") != null) {
            File fileFromVmArgument = new File(System.getProperty("basePropertyFile"));
            if (fileFromVmArgument.canRead()) {
                file = fileFromVmArgument;
            }

            if (file == null) {
                LOGGER.error("Cannot find following base property file: {}", System.getProperty("propertyFile"));
                throw new T9tException(T9tException.INVALID_CONFIGURATION, "Base configuration file not found");
            } else {
                LOGGER.info("Use following base property file: {}", file.getAbsolutePath());
            }

            // read file
            BufferedInputStream stream = null;
            try {
                stream = new BufferedInputStream(new FileInputStream(file));
                properties.load(stream);
            } catch (FileNotFoundException e1) {
                LOGGER.error("Cannot find following base property file: {}", file.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.error("Cannot load following base property file: {}", file.getAbsolutePath());
            } finally {
                 try {
                    stream.close();
                } catch (IOException e) {
                    LOGGER.error("Cannot close stream to following base property file: {}", file.getAbsolutePath());
                }
            }

        } else {
            LOGGER.info("No VM argument for base property file (including resetPassword API Key) given!");
        }

        return properties;
    }
}
