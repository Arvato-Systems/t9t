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
import de.jpaw.util.ByteArray;

@Singleton
@Named("de.jpaw.util.ByteArray")
public class ByteArrayConverter implements IItemConverter<ByteArray> {

    @Override
    public String getFormattedLabel(final ByteArray value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        // display it as a string if all bytes are printable ASCII (check the first 32 only...)
        int lengthToCheck = value.length() < 32 ? value.length() : 32;
        if (lengthToCheck == 0)
            return "''";   // empty string
        for (int i = 0; i < lengthToCheck; ++i) {
            byte c = value.byteAt(i);
            if (c < 0x20 || c > 0x7e) {
                // at least one not printable: return hexdump
                return "0x" + value.hexdump(0, 16) + (value.length() <= 16 ? "" : "...");
            }
        }
        if (lengthToCheck == 32)
            return "'" + new String(value.getBytes(0, lengthToCheck)) + "...";  // partial string
        else
            return "'" + new String(value.getBytes()) + "'";  // full string
    }

    @Override
    public Object getConvertedValue(ByteArray value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        return value;
    }
}
