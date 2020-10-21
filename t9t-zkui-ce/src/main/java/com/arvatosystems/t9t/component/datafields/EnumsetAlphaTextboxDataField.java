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
import org.zkoss.zul.Textbox;

import de.jpaw.bonaparte.pojos.meta.AlphanumericEnumSetDataItem;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.EnumSetDefinition;
import de.jpaw.enums.AbstractStringAnyEnumSet;
import de.jpaw.enums.GenericEnumSetMarker;
import de.jpaw.util.ExceptionUtil;

/** DataField for Enumsets, using a Textbox. Suitable for enums with single-character tokens. */
public class EnumsetAlphaTextboxDataField<E extends Enum<E>, T extends GenericEnumSetMarker<E>> extends AbstractCoreDataField<Textbox, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumsetAlphaTextboxDataField.class);
    protected final Textbox c = new Textbox();
    protected final EnumSetDefinition esd;
    protected final EnumDefinition ed;

    @Override
    public boolean empty() {
        return false;  // enumsets are never empty in the sense they require contents
    }

    public EnumsetAlphaTextboxDataField(DataFieldParameters params, String enumDtoRestrictions) {
        super(params);
        AlphanumericEnumSetDataItem di = (AlphanumericEnumSetDataItem)params.cfg;
        esd = di.getBaseEnumset();
        ed = esd.getBaseEnum();

        // the allowed character set for the enumset is the list of all tokens. Here we assume that the tokens are alphabetic or numeric
        // and currently do not care about escaping them.
        // The current implementation does not support enums with a null / empty string token
        StringBuilder sb = new StringBuilder();
        sb.append("/[");
        for (String token: ed.getTokens())
            sb.append(token);
        sb.append("]*/");
        c.setHflex("1");
        c.setConstraint(sb.toString());
        c.setMaxlength(ed.getTokens().size());
        LOGGER.debug("Created XEnumset data field for {} with max length {} chars, constraint is {}", getFieldName(), ed.getTokens().size(), sb.toString());
    }

    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public Textbox getComponent() {
        return c;
    }

    @Override
    public T getValue() {
        Class<T> cls = esd.getClassRef();
        try {
            String s = c.getValue();
            if (s == null || s.length() == 0)
                return cls.newInstance();
                // return isRequired ? cls.newInstance() : null;  // return null for empty sets, unless the field is required, which means return an empty set
            return cls.getDeclaredConstructor(String.class).newInstance(s);
        } catch (Exception e) {
            LOGGER.error("Cannot construct Enumset {} ({}): {}", getFieldName(), cls.getSimpleName(), ExceptionUtil.causeChain(e));
            return null;
        }
    }

    @Override
    public void setValue(T data) {
        c.setValue(data == null ? null : ((AbstractStringAnyEnumSet<E>)data).getBitmap());
    }

    @Override
    public void setDisabled(boolean disabled) {
        c.setDisabled(disabled);
    }
}
