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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.util.ToStringHelper;

public abstract class AbstractEnumDataField<T> extends AbstractDataField<Combobox, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEnumDataField.class);
    protected final Combobox c = new Combobox();
    protected final Map<T, Comboitem> cbItems = new HashMap<T, Comboitem>();
    protected final Map<T, Integer> cbIndexes = new HashMap<T, Integer>();
    protected int indexCount = 0;
    protected final Set<String> enumRestrictions;

    public AbstractEnumDataField(DataFieldParameters params, String pqon, String enumDtoRestriction) {
        super(params);
        this.enumRestrictions = as.enumRestrictions(pqon, enumDtoRestriction, params.enumZulRestrictions);
        if (enumRestrictions != null) {
            LOGGER.debug("enum {} for field {} restricted to {} instances", pqon, getFieldName(), enumRestrictions.size());
            LOGGER.debug("instances are {}", ToStringHelper.toStringML(enumRestrictions));

        }
    }

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    @Override
    public void clear() {
        c.setSelectedIndex(-1);
    }

    @Override
    public Combobox getComponent() {
        return c;
    }

    @Override
    public T getValue() {
        Comboitem ci = c.getSelectedItem();
        LOGGER.debug("Getting item from {}: retrieved {}, size of cbItems is {}, indexCount is {}",
                getFieldName(),
                ci == null ? "NULL" : ci.getLabel(),
                cbItems.size(), indexCount);
        return ci == null ? null : ci.getValue();
    }

    @Override
    public void setValue(T data) {
        if (data == null) {
            clear();
        }
        Comboitem ci = cbItems.get(data);
        Integer index = cbIndexes.get(data);
        LOGGER.debug("Setting value {} to enum {}, ci is {}, index is {}", data, getFieldName(), ci, index);
        if (ci != null)
            LOGGER.debug("     Label is {}, value is {}", ci.getLabel(), ci.getValue());
        if (ci != null) {
            // c.setSelectedItem(ci);
            LOGGER.debug("Setting index {}", index);
            c.setSelectedIndex(index);
            c.setText(ci.getLabel());
        } else {
            clear();
        }
    }

    protected void newComboItem(T value, String text) {
        Comboitem ci = new Comboitem();
        ci.setLabel(text);
        ci.setValue(value);
        ci.setParent(c);
        cbItems.put(value, ci);
        cbIndexes.put(value, indexCount++);
    }

    // only called for Enum and EnumAlpha, therefore we can cast e to T
    protected void setEnumConstraintsAndModel(EnumDefinition ed) {
        setConstraints(c, null);
        Map<String, String> translations = as.translateEnum(ed.getName());
        Class<Enum> enumClass = ed.getClassRef();
        for (String s: ed.getIds()) {
            if (enumRestrictions == null || enumRestrictions.contains(s)) {
                String xlation = translations.get(s);
                Enum e = Enum.valueOf(enumClass, s);
                newComboItem((T)e, xlation == null ? s : xlation);
            }
        }
        // combobox does not generate onChange events, in order to update the viewmodel, onSelect must be mapped to it
        c.addEventListener(Events.ON_SELECT, (ev) -> Events.postEvent(Events.ON_CHANGE, c, null));
    }
}
