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
package com.arvatosystems.t9t.zkui.converters.grid;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.lang.Generics;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.util.BigDecimalTools;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("java.math.BigDecimal")
public class DecimalConverter extends AbstractDecimalFormatConverter<BigDecimal> implements IItemConverter<BigDecimal> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DecimalConverter.class);
    private static final String DEFAULT_PATTERN = "###,##0.00";

    @Override
    protected String getPattern() {
        return DEFAULT_PATTERN;
    }

    @Override
    public String getFormattedLabel(BigDecimal value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            BonaPortable root = getObjectWithoutDataOrTracking(wholeDataObject);
            String path = getPathWithoutDataOrTracking(fieldName);
            BigDecimal scaledValue = BigDecimalTools.retrieveScaled(root, path);
            if (scaledValue == null) {
                LOGGER.warn("Can't find BigDecimal value in {}#{}. Pleased check decimal property in bon file.", root.ret$PQON(), path);
                scaledValue = setDefaultMinScale(value);
            }
            return getLocalizedDecimalFormat(this.format, scaledValue.scale()).format(scaledValue);
        } else {
            throw new UnsupportedOperationException("Instance " + value.getClass().getName() + " is not supported. Field:" + fieldName + "->" + value);
        }

    }

    private String getPathWithoutDataOrTracking(String fullPath) {
        // check if fullPath starts with "data.", this means we use DataWithTracking
        // -> remove the data.
        String clearedField = fullPath.startsWith("data.") || fullPath.startsWith("tracking.")
                ? StringUtils.substringAfter(fullPath, ".") : fullPath;
                return clearedField;
    }

    private BonaPortable getObjectWithoutDataOrTracking(Object wholeDataObject) {
        if (wholeDataObject instanceof DataWithTracking<?, ?>)
            return ((DataWithTracking<?, ?>)wholeDataObject).getData();
        return (BonaPortable)wholeDataObject;
    }

    private BigDecimal setDefaultMinScale(Object value) {
        BigDecimal tmp = Generics.cast(value);
        tmp = tmp.stripTrailingZeros();
        if (tmp.scale() < 0) {
            tmp = tmp.setScale(0);
        }
        return tmp;
    }
}
