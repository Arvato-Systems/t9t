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
package com.arvatosystems.t9t.component.datafields;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import de.jpaw.bonaparte.pojos.meta.EnumDataItem;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;

public class EnumAlphaDataField extends AbstractEnumDataField<Enum> {
    protected final Class<? extends Enum<?>> enumClass;
    protected final Map<String,Object> converterArg;
    protected final EnumDefinition ed;

    public EnumAlphaDataField(DataFieldParameters params, String enumDtoRestriction) {
        super(params, ((EnumDataItem)params.cfg).getBaseEnum().getName(), enumDtoRestriction);
        ed = ((EnumDataItem)cfg).getBaseEnum();
        enumClass = ed.getClassRef();
        converterArg = ImmutableMap.of("enumClass", enumClass);

        setEnumConstraintsAndModel(ed);
    }

    @Override
    public String getConverter() {
        return "enumAlpha";
    }

    @Override
    public Map<String,Object> getConverterArgs() {
        return null;
    }
}
