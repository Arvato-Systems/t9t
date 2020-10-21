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

import java.util.Map;
import java.util.StringJoiner;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.EnumSetDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.NumericEnumSetDataItem;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

// converts an enumset back to a list of tokens
@Singleton
@Named("numericenumset")
public class NumericEnumsetConverter implements IItemConverter<Number> {

    @Override
    public String getFormattedLabel(Number value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        final ApplicationSession as = ApplicationSession.get();

        NumericEnumSetDataItem di = (NumericEnumSetDataItem)meta;
        EnumSetDefinition esd = di.getBaseEnumset();
        EnumDefinition ed = esd.getBaseEnum();
        Map<String, String> enumXlation = as.translateEnum(ed.getName());

        long bitmap = value.longValue();
        // create a comma separated list of names
        StringJoiner sj = new StringJoiner(",");

        int ordinal = 0;
        while (bitmap != 0L && ordinal < ed.getIds().size()) {
            if ((bitmap & 1L) != 0) {
                // token set
                String instanceName = ed.getIds().get(ordinal);
                String xlate = enumXlation.get(instanceName);
                sj.add(xlate != null ? xlate : instanceName);
            }
            ++ordinal;
            bitmap >>= 1L;
        }
        return sj.toString();
    }

    @Override
    public Object getConvertedValue(Number value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        return value;
    }
}
