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

import java.util.Set;
import java.util.StringJoiner;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.enums.BonaEnum;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.enums.AbstractStringXEnumSet;
import de.jpaw.enums.AbstractXEnumBase;
import de.jpaw.enums.EnumSetMarker;

@Singleton
@Named("enumset")
public class EnumsetConverter implements IItemConverter<EnumSetMarker> {

    @Override
    public String getFormattedLabel(EnumSetMarker value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        final ApplicationSession as = ApplicationSession.get();

        // create a comma separated list of names
        StringJoiner sj = new StringJoiner(",");
        if (value instanceof AbstractStringXEnumSet) {
            for (AbstractXEnumBase<?> xe : (Set<AbstractXEnumBase<?>>)value)
                sj.add(as.translateEnum((BonaEnum)xe.getBaseEnum()));
        } else {
            for (BonaEnum be : (Set<BonaEnum>)value)
                sj.add(as.translateEnum(be));
        }
        return sj.toString();
    }

    @Override
    public Object getConvertedValue(EnumSetMarker value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        return value;
    }
}
