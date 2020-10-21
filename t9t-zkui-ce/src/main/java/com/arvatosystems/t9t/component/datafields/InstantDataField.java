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

import java.util.Date;

import org.joda.time.Instant;
import org.zkoss.zul.Datebox;

public class InstantDataField extends AbstractDataField<Datebox, Instant> {
    protected final Datebox c = new Datebox();

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public InstantDataField(DataFieldParameters params) {
        super(params);
        setConstraints(c, null);
        c.setFormat("medium+medium");
    }

    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public Datebox getComponent() {
        return c;
    }

    @Override
    public Instant getValue() {
        Date d = c.getValue();
        if (d == null)
            return null;
        return new Instant(d.getTime());
    }

    @Override
    public void setValue(Instant data) {
        c.setValue(data == null ? null : new Date(data.getMillis()));
    }
}
