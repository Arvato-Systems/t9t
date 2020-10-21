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
package com.arvatosystems.t9t.component.ext;

import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;

public class FilterGenerator {
    private static SearchFilter createFilter(Ref data, String fieldName) {
        final LongFilter l = new LongFilter();
        l.setFieldName(fieldName);
        l.setEqualsValue(data.getObjectRef());
        return l;
    }

    public static IFilterGenerator filterForFieldname(final String fieldName) {
        return (data) -> createFilter((Ref)data, fieldName);
    }

    public static IFilterGenerator filterForName(final String name) {
        return Jdp.getRequired(IFilterGenerator.class, name);
    }
}
