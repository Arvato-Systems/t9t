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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

import org.zkoss.zul.Timebox;

import com.arvatosystems.t9t.zkui.session.ApplicationSession;

public class TimeDataField extends AbstractDataField<Timebox, LocalTime> {
    protected final Timebox c = new Timebox();

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public TimeDataField(DataFieldParameters params) {
        super(params);
        setConstraints(c, null);
        c.setFormat("medium");
        c.setTimeZone(TimeZone.getDefault());  // do not convert between user's time zone and UTC here
    }

    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public Timebox getComponent() {
        return c;
    }

    @Override
    public LocalTime getValue() {
        Date d = c.getValue();
        if (d == null)
            return null;
        return LocalTime.ofInstant(d.toInstant(), ZoneOffset.UTC);
    }

    @Override
    public void setValue(LocalTime data) {
        c.setValue(data == null ? null : ApplicationSession.toDateSystemZone(data.atDate(LocalDate.now()))); // TODO: better use day of Epoch?
    }
}
