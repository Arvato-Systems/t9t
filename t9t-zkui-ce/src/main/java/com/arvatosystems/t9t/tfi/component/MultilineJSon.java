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
package com.arvatosystems.t9t.tfi.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Textbox;

import com.arvatosystems.t9t.tfi.web.ZulUtils;

import de.jpaw.json.JsonException;
import de.jpaw.json.JsonParser;

public class MultilineJSon extends Textbox implements IdSpace, AfterCompose  {
    private static final long serialVersionUID = 78048814132135L;

    private boolean isNullAllowed = true;

    public boolean isNullAllowed() {
        return isNullAllowed;
    }

    public void setNullAllowed(boolean isNullAllowed) {
        this.isNullAllowed = isNullAllowed;
    }

    public MultilineJSon() {
        super();
        JSonConstraint jSonConstraint = new JSonConstraint();
        this.setConstraint(jSonConstraint);
        this.setHflex("1");
    }

    class JSonConstraint implements Constraint{
        @Override
        public void validate(Component comp, Object value) throws WrongValueException {
            if (value != null) {
                String componentValue = null;
                if (comp instanceof Textbox && value instanceof String) {
                    componentValue = (String) value;
                }
                if (componentValue != null && !componentValue.equals("")) {
                    try {
                        new JsonParser(componentValue, false).parseObject();
                    } catch (JsonException ex) {
                        throw new WrongValueException(comp, ZulUtils.translate("err","invalidJsonFormat"));
                    }
                }
            }
        }
    }

    @Override
    public void afterCompose() {

    }
}
