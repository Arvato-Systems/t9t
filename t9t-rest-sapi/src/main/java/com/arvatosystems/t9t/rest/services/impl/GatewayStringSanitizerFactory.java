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
package com.arvatosystems.t9t.rest.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.StringSanitizer;
import com.arvatosystems.t9t.rest.services.IGatewayStringSanitizerFactory;
import com.arvatosystems.t9t.rest.utils.RestUtils;

import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.dp.Singleton;

@Singleton
public class GatewayStringSanitizerFactory implements IGatewayStringSanitizerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayStringSanitizerFactory.class);

    @Override
    public DataConverter<String, AlphanumericElementaryDataItem> createStringSanitizerForGateway() {
        final String forbiddenCharacters = RestUtils.getConfigValue("t9t.restapi.forbiddenchars");
        if (forbiddenCharacters != null && forbiddenCharacters.trim().length() > 0) {
            final String forbidden = forbiddenCharacters.trim();
            LOGGER.info("Installing String sanitizer for {} special characters: {}", forbidden.length(), forbidden);
            Character replacementChar = null;
            final String replacementStr = RestUtils.getConfigValue("t9t.restapi.replacementchar");
            if (replacementStr != null && !replacementStr.isEmpty()) {
                replacementChar = replacementStr.charAt(0);
            }
            return new StringSanitizer(forbidden, replacementChar);
        } else {
            LOGGER.info("No characters to be filtered configured for String sanitizer");
            return null;
        }
    }
}
