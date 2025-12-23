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
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

import org.zkoss.zul.Datebox;

import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.ApplicationUtil;

public class DayDataField extends AbstractDataField<Datebox, LocalDate> {
    protected final Datebox c = new Datebox();

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public DayDataField(DataFieldParameters params, boolean withToday) {
        super(params);
        setConstraints(c, null);
        c.setFormat(ApplicationUtil.getDateFormat(as.getUserLocale()));
        c.setTimeZone(TimeZone.getDefault());  // do not convert between user's time zone and UTC here
        if (withToday) {
            c.setShowTodayLink(withToday);
            c.setTodayLinkLabel(as.translate("datePicker", "todayLabel"));
        }
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
    public LocalDate getValue() {
        Date d = c.getValue();
        if (d == null)
            return null;
        return LocalDate.ofInstant(d.toInstant(), ZoneOffset.UTC);
    }

    @Override
    public void setValue(LocalDate data) {
        c.setValue(data == null ? null : ApplicationSession.toDateSystemZone(data.atStartOfDay()));
    }
}
