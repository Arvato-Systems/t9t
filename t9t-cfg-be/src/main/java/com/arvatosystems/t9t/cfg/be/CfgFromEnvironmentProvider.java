/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.cfg.be;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.converter.DataConverterAbstract;
import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;

public final class CfgFromEnvironmentProvider extends DataConverterAbstract<String, AlphanumericElementaryDataItem>
  implements DataConverter<String, AlphanumericElementaryDataItem> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CfgFromEnvironmentProvider.class);

    @Override
    public String convert(final String oldValue, final AlphanumericElementaryDataItem meta) {
        if (oldValue == null) {
            return null;
        }
        if (oldValue.startsWith("$env:")) {
            final String envName = oldValue.substring(5);
            final String envValue = System.getenv(envName);
            if (envValue == null) {
                LOGGER.error("Missing environment variable {}", envName);
                return null;
            }
            LOGGER.debug("Replaced environment variable {}", envName);
            if (envValue.isEmpty()) {
                return null;
            }
            return envValue;
        }
        return oldValue;
    }
}
