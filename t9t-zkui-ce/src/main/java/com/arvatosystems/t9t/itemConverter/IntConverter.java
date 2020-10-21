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
package com.arvatosystems.t9t.itemConverter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("java.lang.Integer")
public class IntConverter implements IItemConverter<Integer> {

    @Override
    public boolean isRightAligned() {
        return true;
    }

    @Override
    public String getFormattedLabel(Integer value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        return value.toString();
    }

    @Override
    public Object getConvertedValue(Integer value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        return value;
    }
}
