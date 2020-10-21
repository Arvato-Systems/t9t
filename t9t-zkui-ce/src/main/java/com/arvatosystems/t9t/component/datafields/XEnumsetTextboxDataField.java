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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Textbox;

import de.jpaw.bonaparte.pojos.meta.XEnumDefinition;
import de.jpaw.bonaparte.pojos.meta.XEnumSetDataItem;
import de.jpaw.bonaparte.pojos.meta.XEnumSetDefinition;
import de.jpaw.enums.AbstractStringXEnumSet;
import de.jpaw.enums.AbstractXEnumBase;
import de.jpaw.enums.XEnumFactory;
import de.jpaw.util.ExceptionUtil;

/** DataField for XEnumsets, using a textbox. Suitable for Xenums with single-character tokens. */
public class XEnumsetTextboxDataField<E extends AbstractXEnumBase<E>, S extends AbstractStringXEnumSet<E>> extends AbstractDataField<Textbox, S> {
    private static final Logger LOGGER = LoggerFactory.getLogger(XEnumsetTextboxDataField.class);
    protected final Textbox c = new Textbox();
    protected final XEnumSetDefinition xesd;
    protected final XEnumDefinition xed;

    @Override
    public boolean empty() {
        return false;  // Xenumsets are never empty in the sense they require contents
    }

    public XEnumsetTextboxDataField(DataFieldParameters params, String enumDtoRestrictions) {
        super(params);
        XEnumSetDataItem xdi = (XEnumSetDataItem)params.cfg;
        xesd = xdi.getBaseXEnumset();
        xed = xesd.getBaseXEnum();

        final XEnumFactory factory = XEnumFactory.getFactoryByPQON(xed.getName());
        StringBuilder sb = new StringBuilder();
        sb.append("/[");
        List<AbstractXEnumBase<E>> instances = factory.valuesAsList();
        for (AbstractXEnumBase<E> e: instances)
            sb.append(e.getToken());
        sb.append("]*/");
        setConstraints(c, sb.toString());
        c.setMaxlength(instances.size());
        LOGGER.debug("Created XEnumset data field for {} with max length {} chars, constraint is {}", getFieldName(), instances.size(), sb.toString());
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
    public S getValue() {
        Class<S> cls = xesd.getClassRef();
        try {
            String s = c.getValue();
            if (s == null || s.length() == 0)
                return cls.newInstance();
                // return isRequired ? cls.newInstance() : null;  // return null for empty sets, unless the field is required, which means return an empty set
            return cls.getDeclaredConstructor(String.class).newInstance(s);
        } catch (Exception e) {
            LOGGER.error("Cannot construct XEnumset {} ({}): {}", getFieldName(), cls.getSimpleName(), ExceptionUtil.causeChain(e));
            return null;
        }
    }

    @Override
    public void setValue(S data) {
        c.setValue(data == null ? null : data.getBitmap());
    }
}
