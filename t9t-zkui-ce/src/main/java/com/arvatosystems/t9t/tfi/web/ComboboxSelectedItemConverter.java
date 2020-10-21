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
package com.arvatosystems.t9t.tfi.web;

import java.util.Iterator;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

/**
 * Convert combobox selected comboitem to value and vice versa.
 */
public class ComboboxSelectedItemConverter implements Converter<Object, Object, Component>, java.io.Serializable {
    private static final long serialVersionUID = 201108171811L;

    @Override
    public Object coerceToUi(Object val, Component comp, BindContext ctx) {
        Combobox cbx = (Combobox) comp;
        if (val != null) {
            for (final Iterator<?> it = cbx.getItems().iterator(); it.hasNext();) {
                final Comboitem ci = (Comboitem) it.next();

                Object bean = ci.getValue();

                if (val.equals(bean)) {
                    return ci;
                }
            }
        }
        return null;
    }

    @Override
    public Object coerceToBean(Object val, Component comp, BindContext ctx) {
        return val != null ? ((Comboitem) val).getValue() : null;
    }

}
