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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Intbox;

import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.EnumSetDefinition;
import de.jpaw.bonaparte.pojos.meta.NumericEnumSetDataItem;
import de.jpaw.enums.AbstractIntEnumSet;
import de.jpaw.enums.GenericEnumSetMarker;
import de.jpaw.util.ExceptionUtil;

/** DataField for Enumsets, using a Textbox. Suitable for enums with single-character tokens. */
public class EnumsetNumIntboxDataField<E extends Enum<E>, T extends GenericEnumSetMarker<E>> extends AbstractCoreDataField<Intbox, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumsetNumIntboxDataField.class);
    protected final Intbox c = new Intbox();
    protected final EnumSetDefinition esd;
    protected final EnumDefinition ed;

    @Override
    public boolean empty() {
        return false;  // enumsets are never empty in the sense they require contents
    }

    public EnumsetNumIntboxDataField(DataFieldParameters params, String enumDtoRestrictions) {
        super(params);
        NumericEnumSetDataItem di = (NumericEnumSetDataItem)params.cfg;
        esd = di.getBaseEnumset();
        ed = esd.getBaseEnum();

        // the allowed range for the enumset is 0 to 2^(number of instances) - 1
        c.setHflex("1");
        c.setConstraint("no negative");
        LOGGER.debug("Created numeric Enumset data field for {}", getFieldName());
    }

    @Override
    public void clear() {
        c.setValue(0);
    }

    @Override
    public Intbox getComponent() {
        return c;
    }

    @Override
    public T getValue() {
        Class<T> cls = esd.getClassRef();
        try {
            Integer bitmap = c.getValue();
            if (bitmap == null || bitmap.intValue() == 0)
                return cls.newInstance();
                // return isRequired ? cls.newInstance() : null;  // return null for empty sets, unless the field is required, which means return an empty set
            return cls.getDeclaredConstructor(Integer.class).newInstance(bitmap);
        } catch (Exception e) {
            LOGGER.error("Cannot construct Enumset {} ({}): {}", getFieldName(), cls.getSimpleName(), ExceptionUtil.causeChain(e));
            return null;
        }
    }

    @Override
    public void setValue(T data) {
        c.setValue(data == null ? null : ((AbstractIntEnumSet<E>)data).getBitmap());
    }

    @Override
    public void setDisabled(boolean disabled) {
        c.setDisabled(disabled);
    }
}
