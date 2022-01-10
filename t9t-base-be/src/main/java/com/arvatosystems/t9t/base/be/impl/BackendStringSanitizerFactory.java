/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.StringSanitizer;
import com.arvatosystems.t9t.base.services.IBackendStringSanitizerFactory;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.ServerConfiguration;

import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.dp.Singleton;

@Singleton
public class BackendStringSanitizerFactory implements IBackendStringSanitizerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackendStringSanitizerFactory.class);

    @Override
    public DataConverter<String, AlphanumericElementaryDataItem> createStringSanitizerForBackend() {
        final ServerConfiguration serverConfig = ConfigProvider.getConfiguration().getServerConfiguration();
        if (serverConfig == null) {
            LOGGER.info("No characters to be filtered configured for String sanitizer (no server configuration section)");
            return null;
        }
        final String forbiddenCharacters = serverConfig.getForbiddenCharacters();
        if (forbiddenCharacters != null && forbiddenCharacters.trim().length() > 0) {
            final String forbidden = forbiddenCharacters.trim();
            LOGGER.info("Installing String sanitizer for {} special characters: {}", forbidden.length(), forbidden);
            return new StringSanitizer(forbidden, serverConfig.getReplacementCharacter());
        } else {
            LOGGER.info("No characters to be filtered configured for String sanitizer");
            return null;
        }
    }
}
