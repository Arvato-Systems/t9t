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
package com.arvatosystems.t9t.zkui.components.datafields;

import org.zkoss.zul.Textbox;

import com.arvatosystems.t9t.base.T9tUtil;

import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;

public class TextDataField extends AbstractDataField<Textbox, String> {
    protected final Textbox c = new Textbox();
    protected final AlphanumericElementaryDataItem cfgParams;  // stored to have easy access to text specific values such as length and regexp

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    private String makeRegExpConstraint() {
        if (T9tUtil.isBlank(cfgParams.getRegexp())) {
            return null;
        }
        // the regular expression depends on whether the field is nullable or not
        return isRequired ? "/" + cfgParams.getRegexp() + "/" : "/(" + cfgParams.getRegexp() + ")?/";
    }

    public TextDataField(final DataFieldParameters params) {
        super(params);
        cfgParams = (AlphanumericElementaryDataItem)params.cfg;
        final String bonaparteType = cfgParams.getBonaparteType();
        setConstraints(c, bonaparteType.equals("uppercase") ? "/[A-Z]*/" : bonaparteType.equals("lowercase") ? "/[a-z]*/" : makeRegExpConstraint());
        c.setMaxlength(cfgParams.getLength());
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
    public void setValue(final String data) {
        c.setValue(data);
    }
}
