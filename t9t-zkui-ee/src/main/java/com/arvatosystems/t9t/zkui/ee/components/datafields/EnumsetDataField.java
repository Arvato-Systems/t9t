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
package com.arvatosystems.t9t.zkui.ee.components.datafields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkmax.zul.Chosenbox;
import org.zkoss.zul.ItemRenderer;
import org.zkoss.zul.ListModelList;

import com.arvatosystems.t9t.zkui.components.datafields.AbstractCoreDataField;
import com.arvatosystems.t9t.zkui.components.datafields.DataFieldParameters;
import com.arvatosystems.t9t.zkui.viewmodel.beans.ComboBoxItem2;

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
    protected final Map<E, ComboBoxItem2<E>> cbItems = new HashMap<>();
    protected final Predicate<String> enumRestrictions;

    public EnumsetDataField(final DataFieldParameters params, final String enumDtoRestrictions) {
        super(params);
        baseEnumset = cfg instanceof AlphanumericEnumSetDataItem alphaSetDataItem
            ? alphaSetDataItem.getBaseEnumset()
            : ((NumericEnumSetDataItem)cfg).getBaseEnumset();
        enumsetClass = baseEnumset.getClassRef();
        ed = baseEnumset.getBaseEnum();

        enumRestrictions = as.enumRestrictions(ed.getName(), enumDtoRestrictions, params.enumZulRestrictions);

        c.setHflex("1");
        final Map<String, String> translations = as.translateEnum(ed.getName());
        final Class<Enum> enumClass = ed.getClassRef();
        final List<E> instances = new ArrayList<>(ed.getIds().size());
        for (final String s: ed.getIds()) {
            if (enumRestrictions.test(s)) {
                final E e = (E) Enum.valueOf(enumClass, s);
                instances.add(e);
            }
        }
        c.setModel(new ListModelList<>(instances));
        // chosenbox does not generate onChange events, in order to update the viewmodel, onSelect must be mapped to it
        c.addEventListener(Events.ON_SELECT, (ev) -> Events.postEvent(Events.ON_CHANGE, c, null));

        c.setItemRenderer(new ItemRenderer<E>() {
            @Override
            public String render(final Component owner, final E data, final int index) throws Exception {
                final String xlation = translations.get(data.name());
                return xlation != null ? xlation : data.name();
            }
        });
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
        final Set<E> data = c.getSelectedObjects();
        try {
            final T instance = enumsetClass.newInstance();
            instance.assign(data);
            LOGGER.debug("{}.getData() returning {}", getFieldName(), instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("cannot getValue()", e);
            return null;
        }
    }

    @Override
    public void setValue(final T data) {
        LOGGER.debug("{}.setData({})", getFieldName(), data);
        if (data == null) {
            clear();
        } else {
            c.setSelectedObjects(data);
        }
    }

    @Override
    public void setDisabled(final boolean disabled) {
        c.setDisabled(disabled);
    }
}
