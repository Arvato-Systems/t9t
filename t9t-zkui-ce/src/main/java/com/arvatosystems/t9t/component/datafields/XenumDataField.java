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

import java.util.List;

import org.zkoss.zk.ui.event.Events;

import de.jpaw.bonaparte.enums.BonaEnum;
import de.jpaw.bonaparte.pojos.meta.XEnumDataItem;
import de.jpaw.bonaparte.pojos.meta.XEnumDefinition;
import de.jpaw.enums.AbstractXEnumBase;
import de.jpaw.enums.XEnumFactory;

public class XenumDataField extends AbstractEnumDataField<AbstractXEnumBase<?>> {
    protected final XEnumDefinition xed;

    private final void createModel() {
        XEnumFactory<?> factory = XEnumFactory.getFactoryByPQON(xed.getName());
        cbItems.clear();
        @SuppressWarnings("unchecked")
        List<AbstractXEnumBase<?>> instances = (List<AbstractXEnumBase<?>>) factory.valuesAsList();
        for (AbstractXEnumBase<?> e: instances) {
            if (enumRestrictions == null || enumRestrictions.contains(e.name()))
                newComboItem(e, as.translateEnum((BonaEnum)e.getBaseEnum()));
        }
    }

    public XenumDataField(DataFieldParameters params, String enumDtoRestriction) {
        super(params, ((XEnumDataItem)params.cfg).getBaseXEnum().getName(), enumDtoRestriction);
        xed = ((XEnumDataItem)cfg).getBaseXEnum();
        setConstraints(c, null);
        createModel();
        // combobox does not generate onChange events, in order to update the viewmodel, onSelect must be mapped to it
        c.addEventListener(Events.ON_SELECT, (ev) -> Events.postEvent(Events.ON_CHANGE, c, null));
    }
}
