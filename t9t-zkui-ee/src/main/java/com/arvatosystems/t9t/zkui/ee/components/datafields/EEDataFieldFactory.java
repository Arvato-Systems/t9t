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
package com.arvatosystems.t9t.zkui.ee.components.datafields;

import com.arvatosystems.t9t.zkui.components.IDataFieldFactory;
import com.arvatosystems.t9t.zkui.components.datafields.DataFieldFactory;
import com.arvatosystems.t9t.zkui.components.datafields.DataFieldParameters;
import com.arvatosystems.t9t.zkui.components.datafields.IDataField;

import com.arvatosystems.t9t.zkui.util.Constants;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;
import jakarta.annotation.Nonnull;

@Singleton
@Specializes
public class EEDataFieldFactory extends DataFieldFactory implements IDataFieldFactory {

    // ZK edition specific data fields
    // ZK enterprise edition implementation
    @Override
    protected IDataField createEnumsetNumDataField(final DataFieldParameters params, final String enumDtoRestrictions) {
        return new EnumsetDataField(params, enumDtoRestrictions);
    }

    @Override
    protected IDataField createEnumsetAlphaDataField(final DataFieldParameters params, final String enumDtoRestrictions) {
        return new EnumsetDataField(params, enumDtoRestrictions);
    }

    @Override
    protected IDataField createXenumsetDataField(final DataFieldParameters params, final String enumDtoRestrictions) {
        return new XEnumsetChosenboxDataField(params, enumDtoRestrictions);
    }

    @Nonnull
    @Override
    protected IDataField createMultiStringDropdownDataField(@Nonnull final DataFieldParameters params) {
        final String multiDropdownType = params.cfg.getProperties() != null ? params.cfg.getProperties().get(Constants.UiFieldProperties.MULTI_DROPDOWN) : null;
        if (multiDropdownType != null) {
            return new StringChosenboxDataField(params, multiDropdownType);
        }
        return super.createMultiStringDropdownDataField(params);
    }
}
