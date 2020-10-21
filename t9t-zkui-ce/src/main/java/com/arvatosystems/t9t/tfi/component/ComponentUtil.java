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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.Locales;

public class ComponentUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentUtil.class);

    // will look like:
    // data.addressLines[0]  --> data.addressLines01
    public static String computeFieldForUnrolledListSorting(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        if (fieldName.indexOf('[') < 0)
            return fieldName;  // shortcut: not index fields
        String[] tokens = fieldName.split("\\[|\\]"); // split by "[" and "]"
        StringBuilder preparedFieldName = new StringBuilder();
        for (String token : tokens) {
            if (NumberUtils.isDigits(token)) {
                token = StringUtils.leftPad(String.valueOf(Integer.parseInt(token) + 1), 2, "0"); // fill with leading 0 size two digits
            }
            preparedFieldName.append(token);
        }

        return preparedFieldName.toString();
    }

    public static DecimalFormat getLocalizedDecimalFormat(String pattern, int minimumFractionDigits) {
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locales.getCurrent());
        df.applyPattern(pattern);
        df.setMinimumFractionDigits(minimumFractionDigits);
        return df;
    }
}
