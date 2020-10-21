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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.base.types.BooleanEnum;

import de.jpaw.bonaparte.pojos.meta.EnumDefinition;

public class BooleanTristateDataField extends AbstractEnumDataField<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BooleanTristateDataField.class);
    protected final EnumDefinition ed;

    public BooleanTristateDataField(DataFieldParameters params) {
        super(params, BooleanEnum.TRUE.ret$PQON(), null);
        ed = BooleanEnum.enum$MetaData();

        setConstraints(c, null);
        Map<String, String> translations = as.translateEnum(ed.getName());
        String trueText = translations.get(BooleanEnum.TRUE.name());
        String falseText = translations.get(BooleanEnum.FALSE.name());
        newComboItem(Boolean.TRUE, trueText == null ? BooleanEnum.TRUE.name() : trueText);
        newComboItem(Boolean.FALSE, falseText == null ? BooleanEnum.FALSE.name() : falseText);
        // combobox does not generate onChange events, in order to update the viewmodel, onSelect must be mapped to it
        c.addEventListener(Events.ON_SELECT, (ev) -> Events.postEvent(Events.ON_CHANGE, c, null));
    }

    @Override
    public Boolean getValue() {
        Comboitem ci = c.getSelectedItem();
        if (ci == null || empty())
            return null;
        Object val = ci.getValue();
        LOGGER.debug("GetValue: ci.value = {} of class {}", val, val == null ? "NULL" : val.getClass().getSimpleName());
        return val != null && val instanceof Boolean && ((Boolean)val).booleanValue();
    }
}
