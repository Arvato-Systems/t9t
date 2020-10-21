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

import org.zkoss.zul.Textbox;

import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;

public class TextDataField extends AbstractDataField<Textbox, String> {
    protected final Textbox c = new Textbox();

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public TextDataField(DataFieldParameters params) {
        super(params);
        String bonaparteType = params.cfg.getBonaparteType();
        setConstraints(c, bonaparteType.equals("uppercase") ? "/[A-Z]*/" : bonaparteType.equals("lowercase") ? "/[a-z]*/" : null);
        c.setMaxlength(((AlphanumericElementaryDataItem)params.cfg).getLength());
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
    public String getValue() {
        return c.getValue();
    }

    @Override
    public void setValue(String data) {
        c.setValue(data);
    }
}
