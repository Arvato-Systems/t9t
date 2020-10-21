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

import java.util.UUID;

import org.zkoss.zul.Textbox;

import com.google.common.base.Strings;

public class UUIDDataField extends AbstractDataField<Textbox, UUID> {
    protected final Textbox c = new Textbox();

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public UUIDDataField(DataFieldParameters params) {
        super(params);
        setConstraints(c, "/[a-fA-F0-9-]*/");
        c.setMaxlength(36);
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
    public UUID getValue() {
        String s = c.getValue();
        if (Strings.isNullOrEmpty(s))
            return null;
        return UUID.fromString(c.getValue());
    }

    @Override
    public void setValue(UUID data) {
        c.setValue(data == null ? null : data.toString());
    }
}
