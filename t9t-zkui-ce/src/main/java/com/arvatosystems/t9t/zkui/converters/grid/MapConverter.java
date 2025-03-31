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
package com.arvatosystems.t9t.zkui.converters.grid;

import java.util.Map;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named("java.util.Map")
public class MapConverter implements IItemConverter<Map> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapConverter.class);

    @Override
    public boolean isRightAligned() {
        return false; // The z field itself should be left aligned. For indexed entries, it could be either left or right, the majority is assumed to be left.
    }

    @Override
    public String getFormattedLabel(final Map value, final BonaPortable wholeDataObject, final String fieldName, final FieldDefinition d) {
        final Object convertedValue = getConvertedValue(value, fieldName);
        if (convertedValue != null) {
            return convertedValue.toString();
        } else {
            LOGGER.debug("key {} not found in {}", extractKeyName(fieldName), value);
            return null;
        }
    }

    private String extractKeyName(final String fieldName) {
        final int startPos = fieldName.lastIndexOf("[");
        if (startPos != -1) {
            final int endPos = fieldName.lastIndexOf("]");
            if (endPos != -1) {
                return fieldName.substring(startPos + 1, endPos);
            }
        }
        return null;
    }

    private Object getConvertedValue(final Map value, final String fieldName) {
        final String keyName = extractKeyName(fieldName);

        //no key for subfield provided (e.g. "data.z" -> return self
        if (keyName == null) {
            return value;
        }

        //key for subfield present (e.g. "data.z[giftCard]" -> return value or null if nothing is there
        return value.get(keyName);
    }
}
