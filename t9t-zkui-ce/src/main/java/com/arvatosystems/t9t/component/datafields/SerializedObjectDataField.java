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

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaparteJsonEscaper;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;
import de.jpaw.json.JsonParser;

public class SerializedObjectDataField extends AbstractDataField<Textbox, BonaPortable> {
    protected final Textbox c = new Textbox();
    protected final ObjectReference myCfg;
    protected final String bonaparteType;

    @Override
    public boolean empty() {
        return c.getValue() == null || c.getValue().length() == 0;
    }

    public SerializedObjectDataField(DataFieldParameters params, String bonaparteType) {
        super(params);
        this.bonaparteType = bonaparteType;
        this.myCfg = (ObjectReference)params.cfg;
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
    public BonaPortable getValue() {
        if (empty())
            return null;
        // analyse JSON
        Map<String, Object> asMap = (new JsonParser(c.getValue(), false)).parseObject();
        return MapParser.asBonaPortable(asMap, myCfg);
    }

    @Override
    public void setValue(BonaPortable data) {
        c.setValue(BonaparteJsonEscaper.asJson(data));
    }
}
