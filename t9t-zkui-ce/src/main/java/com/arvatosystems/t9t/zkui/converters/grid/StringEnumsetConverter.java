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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.AlphanumericEnumSetDataItem;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;

// converts an enumset back to a list of tokens
public class StringEnumsetConverter implements IItemConverter<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringEnumsetConverter.class);

    @Override
    public String getFormattedLabel(String value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        try {
            if (T9tUtil.isNotBlank(value) && meta instanceof AlphanumericEnumSetDataItem enumSet && enumSet.getBaseEnumset().getBaseEnum().getMaxTokenLength() == 1) {
                final EnumDefinition baseEnum = enumSet.getBaseEnumset().getBaseEnum();
                final ApplicationSession as = ApplicationSession.get();
                final Map<String, String> enumTranslations = as.translateEnum(baseEnum.getName());
                final String[] values = value.split("");
                final StringBuilder result = new StringBuilder();
                for (String v : values) {
                    final int index = baseEnum.getTokens().indexOf(v);
                    if (index < 0) {
                        LOGGER.warn("Token {} not found in enum {}", v, baseEnum.getName());
                        return value;
                    }
                    final String enumId = baseEnum.getIds().get(index);
                    final String translatedValue = enumTranslations.get(enumId);
                    if (!result.isEmpty()) {
                        result.append(",");
                    }
                    result.append(translatedValue);
                }
                return result.toString();
            }
        } catch (Exception ex) {
            LOGGER.error("Error occured while converting string enumset {} for field: {}, value : {}, error: {}",
                    wholeDataObject.getClass().getName(), fieldName, value, ex.getMessage());
        }
        return value;
    }
}
