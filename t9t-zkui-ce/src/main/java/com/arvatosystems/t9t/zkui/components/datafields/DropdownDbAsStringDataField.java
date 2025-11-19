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
package com.arvatosystems.t9t.zkui.components.datafields;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28DbFactory;

import de.jpaw.bonaparte.pojos.apiw.Ref;

public class DropdownDbAsStringDataField extends AbstractDropdownDataField<Ref, String> {

    public DropdownDbAsStringDataField(final DataFieldParameters params, final String dropdownType, final IDropdown28DbFactory<Ref> dbFactory) {
        super(params, dbFactory);
    }

    @Override
    protected String extractValue(final Description desc) {
        return desc.getId();
    }

    @Override
    protected Description lookupDescription(final String data) {
        return c.lookupByKey(data);
    }
}
