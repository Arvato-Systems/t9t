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

import java.util.Map;

import org.zkoss.zul.Textbox;

import de.jpaw.bonaparte.core.BonaparteJsonEscaper;
import de.jpaw.json.JsonParser;

public class JsonDataField extends AbstractDataField<Textbox, Map<String, Object>> {
    protected final Textbox c = new Textbox();

    @Override
    public boolean empty() {
        return c.getValue() == null || c.getValue().length() == 0;
    }

    public JsonDataField(DataFieldParameters params) {
        super(params);
        c.setMultiline(true);
        setConstraints(c, null);
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
    public Map<String, Object> getValue() {
        if (empty())
            return null;
        // analyse JSON
        return (new JsonParser(c.getValue(), false)).parseObject();
    }

    @Override
    public void setValue(Map<String, Object> data) {
        c.setValue(BonaparteJsonEscaper.asJson(data));
    }
}
