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
package com.arvatosystems.t9t.tfi.component;

import de.jpaw.fixedpoint.types.MicroUnits;

public class MicroUnitsConverter implements Converter {

    @Override
    public String getFormattedLabel(Object value, Object wholeDataObject, String fieldName) {
        if (value == null) {
            return null;
        }

        if (value instanceof MicroUnits) {
            return value.toString();
        } else {
            throw new UnsupportedOperationException("Instance " + value.getClass().getName() + " is not supported. Field:" + fieldName + "->" + value);
        }
    }

    @Override
    public Object getConvertedValue(Object value, Object wholeDataObject, String fieldName) {
        return value;
    }

}
