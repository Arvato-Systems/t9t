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
package com.arvatosystems.t9t.init;

import java.util.Properties;

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

    private String forgetPasswordApiKey = null;

    public ApplicationConfigurationInitializer() {
        LOGGER.debug("Trying to retrieve passwordReset API KEY");
        forgetPasswordApiKey = getForgetPasswordApiKeyFromFile();
    }

    public String getForgetPasswordApiKey() {
        return forgetPasswordApiKey;
    }

    public String getForgetPasswordApiKeyFromFile() {
        Properties baseProperties = BaseConfigurationProvider.getBaseProperties();
        if (baseProperties.getProperty("forget.password.api.key") != null) {
            return (String) baseProperties.getProperty("forget.password.api.key");
        } else {
            return null;
        }
    }

}
