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
package com.arvatosystems.t9t.init;

import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.BaseConfigurationProvider;

import de.jpaw.dp.Singleton;

/**
 * Initializes the application configuration. At the moment this only applies to retrieving the password reset API Key
 */
@Singleton
public class ApplicationConfigurationInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfigurationInitializer.class);

    private final String forgetPasswordApiKey;

    public ApplicationConfigurationInitializer() {
        LOGGER.debug("Trying to retrieve passwordReset API KEY");
        forgetPasswordApiKey = getForgetPasswordApiKeyFromFile();
        if (forgetPasswordApiKey == null) {
            LOGGER.error("No API-KEY present!");
        } else {
            try {
                UUID.fromString(forgetPasswordApiKey);
            } catch (final Exception e) {
                LOGGER.error("Specified ResetPassword-API-KEY is not a valid UUID!");
            }
        }
    }

    public String getForgetPasswordApiKey() {
        return forgetPasswordApiKey;
    }

    private final String getForgetPasswordApiKeyFromFile() {
        final Properties baseProperties = BaseConfigurationProvider.getBaseProperties();
        return baseProperties.getProperty("forget.password.api.key");  // this can be null!
    }
}
