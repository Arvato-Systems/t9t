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
package com.arvatosystems.t9t.itemConverter;

import com.arvatosystems.t9t.tfi.web.ZulUtils;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("java.lang.Boolean")
@Singleton
public class BooleanTranslationConverter implements IItemConverter<Boolean> {

    @Override
    public String getFormattedLabel(final Boolean value, final BonaPortable wholeDataObject, final String fieldName, final FieldDefinition meta) {
        return ZulUtils.readConfig("com.boolean." + value + ".format");
    }

    @Override
    public Object getConvertedValue(final Boolean value, final BonaPortable wholeDataObject, final String fieldName, final FieldDefinition meta) {
        return value;
    }
}
