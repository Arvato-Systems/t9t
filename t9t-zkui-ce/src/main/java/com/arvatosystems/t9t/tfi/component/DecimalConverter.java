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
package com.arvatosystems.t9t.tfi.component;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.lang.Generics;
import org.zkoss.util.resource.Labels;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.util.BigDecimalTools;

public class DecimalConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DecimalConverter.class);

    private String format;

    public DecimalConverter(String format) {
        if (format == null) {
            throw new NullPointerException("format not set in constructor");
        }
        this.format = Labels.getLabel(format);
        if (this.format == null) {
            this.format = format;
        }
    }

    @Override
    public String getFormattedLabel(Object value, Object wholeDataObject, String fieldName) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            BonaPortable root = getObjectWithoutDataOrTracking(wholeDataObject);
            String path = getPathWithoutDataOrTracking(fieldName);
            BigDecimal scaledValue = BigDecimalTools.retrieveScaled(root, path);
            if (scaledValue == null) {
                LOGGER.info("Can't find BigDecimal value in {}#{}. Pleased check decimal property in bon file.", root.ret$PQON(), path);
                scaledValue = setDefaultMinScale(value);
            }
            return ComponentUtil.getLocalizedDecimalFormat(this.format, scaledValue.scale()).format(scaledValue);
        } else {
            throw new UnsupportedOperationException("Instance " + value.getClass().getName() + " is not supported. Field:" + fieldName + "->" + value);
        }

    }


    @Override
    public Object getConvertedValue(Object value, Object wholeDataObject, String fieldName) {
        return value;
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
