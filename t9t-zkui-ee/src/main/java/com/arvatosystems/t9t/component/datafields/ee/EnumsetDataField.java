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
package com.arvatosystems.t9t.component.datafields.ee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkmax.zul.Chosenbox;
import org.zkoss.zul.ListModelList;

import com.arvatosystems.t9t.component.datafields.AbstractCoreDataField;
import com.arvatosystems.t9t.component.datafields.DataFieldParameters;
import com.arvatosystems.t9t.tfi.model.bean.ComboBoxItem2;

import de.jpaw.bonaparte.pojos.meta.AlphanumericEnumSetDataItem;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.EnumSetDefinition;
import de.jpaw.bonaparte.pojos.meta.NumericEnumSetDataItem;
import de.jpaw.enums.GenericEnumSetMarker;

public class EnumsetDataField<E extends Enum<E>, T extends GenericEnumSetMarker<E>> extends AbstractCoreDataField<Chosenbox, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumsetDataField.class);
    protected final Chosenbox c = new Chosenbox();
    protected final EnumDefinition ed;
    protected final EnumSetDefinition baseEnumset;
    protected final Class<T> enumsetClass;
    protected final Map<E, ComboBoxItem2<E>> cbItems = new HashMap<E, ComboBoxItem2<E>>();
    protected final Set<String> enumRestrictions;

    public EnumsetDataField(DataFieldParameters params, String enumDtoRestrictions) {
        super(params);
        this.baseEnumset = (cfg instanceof AlphanumericEnumSetDataItem)
            ? ((AlphanumericEnumSetDataItem)cfg).getBaseEnumset()
            : ((NumericEnumSetDataItem)cfg).getBaseEnumset();
        enumsetClass = baseEnumset.getClassRef();
        ed = baseEnumset.getBaseEnum();

        this.enumRestrictions = as.enumRestrictions(ed.getName(), enumDtoRestrictions, params.enumZulRestrictions);
        if (enumRestrictions != null)
            LOGGER.debug("enumset {} for field {} restricted to {} instances", ed.getName(), getFieldName(), enumRestrictions.size());

        c.setHflex("1");
        Map<String, String> translations = as.translateEnum(ed.getName());
        Class<Enum> enumClass = ed.getClassRef();
        List<E> instances = new ArrayList<E>(ed.getIds().size());
        for (String s: ed.getIds()) {
            if (enumRestrictions == null || enumRestrictions.contains(s)) {
                String xlation = translations.get(s);
                E e = (E) Enum.valueOf(enumClass, s);
                instances.add(e);
                // newComboItem((T)e, xlation == null ? s : xlation);
            }
        }
        c.setModel(new ListModelList<>(instances));
        // chosenbox does not generate onChange events, in order to update the viewmodel, onSelect must be mapped to it
        c.addEventListener(Events.ON_SELECT, (ev) -> Events.postEvent(Events.ON_CHANGE, c, null));
    }

    @Override
    public boolean empty() {
        return c.getSelectedObjects().isEmpty();
    }

    @Override
    public void clear() {
        c.setSelectedObjects(Collections.EMPTY_SET);
    }

    @Override
    public Chosenbox getComponent() {
        return c;
    }

    @Override
    public T getValue() {
        Set<E> data = c.getSelectedObjects();
        try {
            T instance = enumsetClass.newInstance();
            instance.assign(data);
            LOGGER.debug("{}.getData() returning {}", getFieldName(), instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("cannot getValue()", e);
            return null;
        }
    }

    @Override
    public void setValue(T data) {
        LOGGER.debug("{}.setData({})", getFieldName(), data);
        if (data == null) {
            clear();
        } else {
            c.setSelectedObjects(data);
        }
    }

//    protected void newComboItem(T value, String text) {
//        Comboitem ci = new Comboitem();
//        ci.setLabel(text);
//        ci.setValue(value);
//        ci.setParent(c);
//        cbItems.put(value, ci);
//    }

    @Override
    public void setDisabled(boolean disabled) {
        c.setDisabled(disabled);
    }
}
