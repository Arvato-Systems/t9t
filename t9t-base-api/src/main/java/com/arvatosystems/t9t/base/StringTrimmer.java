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
package com.arvatosystems.t9t.base;

import de.jpaw.bonaparte.converter.DataConverterAbstract;
import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;

public final class StringTrimmer extends DataConverterAbstract<String, AlphanumericElementaryDataItem>
  implements DataConverter<String, AlphanumericElementaryDataItem> {

    @Override
    public String convert(final String oldValue, final AlphanumericElementaryDataItem meta) {
        if (oldValue == null || meta.getAllowControlCharacters() || meta.getRegexp() != null) {
            // no change for these fields / values
            return oldValue;
        }
        // trim and return, but if now the empty string, return null instead
        final String newValue = oldValue.trim();
        return newValue.isEmpty() ? null : newValue;
    }
}
