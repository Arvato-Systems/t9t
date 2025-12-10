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

import java.text.DecimalFormat;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.fixedpoint.FixedPointBase;

@Singleton
@Named("fixedpoint")
public class FixedPointConverter extends AbstractDecimalFormatConverter<FixedPointBase<?>> implements IItemConverter<FixedPointBase<?>> {

    @Override
    protected String getPattern() {
        return "0.00";  // default pattern for fixed point
    }

    @Override
    public String getFormattedLabel(FixedPointBase<?> value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        final DecimalFormat df = getLocalizedDecimalFormat(this.format, value.scale());
        return df.format(value.doubleValue());
    }
}
