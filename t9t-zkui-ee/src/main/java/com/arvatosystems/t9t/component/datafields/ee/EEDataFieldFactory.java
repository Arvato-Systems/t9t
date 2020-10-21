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
package com.arvatosystems.t9t.component.datafields.ee;

import com.arvatosystems.t9t.component.datafields.DataFieldFactory;
import com.arvatosystems.t9t.component.datafields.DataFieldParameters;
import com.arvatosystems.t9t.component.datafields.IDataField;
import com.arvatosystems.t9t.component.ext.IDataFieldFactory;

import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;

@Singleton
@Specializes
public class EEDataFieldFactory extends DataFieldFactory implements IDataFieldFactory {

    // ZK edition specific data fields
    // ZK enterprise edition implementation
    @Override
    protected IDataField createEnumsetNumDataField(final DataFieldParameters params, String enumDtoRestrictions) {
        return new EnumsetDataField(params, enumDtoRestrictions);
    }

    @Override
    protected IDataField createEnumsetAlphaDataField(final DataFieldParameters params, String enumDtoRestrictions) {
        return new EnumsetDataField(params, enumDtoRestrictions);
    }

    @Override
    protected IDataField createXenumsetDataField(final DataFieldParameters params, String enumDtoRestrictions) {
        return new XEnumsetChosenboxDataField(params, enumDtoRestrictions);
    }
}
