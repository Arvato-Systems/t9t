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

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.enums.BonaEnum;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.enums.XEnum;

@Singleton
@Named("xenum")
public class XenumTranslationConverter implements IItemConverter<XEnum<?>> {

    @Override
    public String getFormattedLabel(XEnum<?> value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        return ApplicationSession.get().translateEnum((BonaEnum)value.getBaseEnum());
    }

    @Override
    public Object getConvertedValue(XEnum<?> value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        return value;
    }
}
