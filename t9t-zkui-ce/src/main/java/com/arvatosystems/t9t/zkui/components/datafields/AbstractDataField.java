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

import org.zkoss.zul.impl.InputElement;

public abstract class AbstractDataField<E extends InputElement, T> extends AbstractCoreDataField<E, T> {

    protected AbstractDataField(final DataFieldParameters params) {
        super(params);
    }

    protected String combineConstraints(final String a, final String b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        return a + "," + b;
    }

    protected void setConstraints(final InputElement c, final String moreConstraints) {
        c.setHflex("1");
        final String noEmpty = isRequired ? "no empty" : null;
        final String allConstraints = combineConstraints(noEmpty, moreConstraints);
        if (allConstraints != null)
            c.setConstraint(allConstraints);
    }

    @Override
    public void setDisabled(final boolean disabled) {
        getComponent().setDisabled(disabled);
    }
}
