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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zkmax.zul.Chosenbox;
import org.zkoss.zul.ListModelList;

import com.arvatosystems.t9t.component.datafields.AbstractCoreDataField;
import com.arvatosystems.t9t.component.datafields.DataFieldParameters;
import com.arvatosystems.t9t.tfi.model.bean.ComboBoxItem2;

import de.jpaw.bonaparte.pojos.meta.XEnumDefinition;
import de.jpaw.bonaparte.pojos.meta.XEnumSetDataItem;
import de.jpaw.bonaparte.pojos.meta.XEnumSetDefinition;
import de.jpaw.enums.AbstractStringXEnumSet;
import de.jpaw.enums.AbstractXEnumBase;
import de.jpaw.enums.XEnumFactory;
import de.jpaw.util.ExceptionUtil;

/** DataField for XEnumsets, using the ZK EE Chosenbox. Suitable for Xenums with single-character tokens. */
public class XEnumsetChosenboxDataField<E extends AbstractXEnumBase<E>, S extends AbstractStringXEnumSet<E>> extends AbstractCoreDataField<Chosenbox, S> {
    private static final Logger LOGGER = LoggerFactory.getLogger(XEnumsetChosenboxDataField.class);
    protected final Chosenbox c = new Chosenbox();
    protected final XEnumSetDefinition xesd;
    protected final XEnumDefinition xed;
    protected final Map<E, ComboBoxItem2<E>> cbItems = new HashMap<E, ComboBoxItem2<E>>();
    protected final Set<String> enumRestrictions;

    public XEnumsetChosenboxDataField(DataFieldParameters params, String enumDtoRestrictions) {
        super(params);
        XEnumSetDataItem xdi = (XEnumSetDataItem)params.cfg;
        xesd = xdi.getBaseXEnumset();
        xed = xesd.getBaseXEnum();

        this.enumRestrictions = as.enumRestrictions(xed.getName(), enumDtoRestrictions, params.enumZulRestrictions);
        if (enumRestrictions != null)
            LOGGER.debug("xenumset {} for field {} restricted to {} instances", xed.getName(), getFieldName(), enumRestrictions.size());

        final XEnumFactory factory = XEnumFactory.getFactoryByPQON(xed.getName());

        c.setHflex("1");
        List<AbstractXEnumBase<E>> instances = factory.valuesAsList();
        if (enumRestrictions != null) {
            instances = instances.stream().filter(e -> enumRestrictions.contains(e.name())).collect(Collectors.toList());
        }
        c.setModel(new ListModelList<>(instances));
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
    public S getValue() {
        Class<S> cls = xesd.getClassRef();
        try {
            Set<E> data = c.getSelectedObjects();
            S instance = cls.newInstance();
            if (data == null || data.isEmpty())
                return instance;
                // return isRequired ? instance : null;  // return null for empty sets, unless the field is required, which means return an empty set
            for (E x : data)
                instance.add(x);
            return instance;
        } catch (Exception e) {
            LOGGER.error("Cannot construct XEnumset {} ({}): {}", getFieldName(), cls.getSimpleName(), ExceptionUtil.causeChain(e));
            return null;
        }
    }

    @Override
    public void setValue(S data) {
        LOGGER.debug("{}.setData({})", getFieldName(), data);
        if (data == null) {
            clear();
        } else {
            c.setSelectedObjects(data);
        }
    }

    @Override
    public void setDisabled(boolean disabled) {
        c.setDisabled(disabled);
    }
}
