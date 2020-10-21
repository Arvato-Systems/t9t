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
package com.arvatosystems.t9t.component.fields;

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.LocalTime;
import org.zkoss.zul.Timebox;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TimeFilter;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;


public class TimeField extends AbstractField<Timebox> {
    @Override
    protected Timebox createComponent(String suffix) {
        Timebox t = new Timebox();
        t.setId(cfg.getFieldName() + suffix);
        t.setHflex("1");
        t.setFormat("medium");
        t.setPlaceholder(label);
        t.setTimeZone(TimeZone.getDefault());  // do not convert between user's time zone and UTC here
        return t;
    }

    @Override
    protected boolean componentEmpty(Timebox c) {
        return c.getValue() == null;
    }

    protected LocalTime getVal(Timebox d) {
        Date vv = d.getValue();
        return vv == null ? null : LocalTime.fromDateFields(vv);
    }

    @Override
    public SearchFilter getSearchFilter() {
        if (empty())
            return null;
        // depending on which values are set, create a lower, upper, equals or range filter
        TimeFilter f = new TimeFilter();
        f.setFieldName(getFieldName());
        LocalTime v = getVal(components.get(0));
        switch (cfg.getFilterType()) {
        case EQUALITY:
            f.setEqualsValue(v);
            break;
        case LIKE:
            noLikeFilter();
            f.setEqualsValue(v);
            break;
        case LOWER_BOUND:
            f.setLowerBound(v);
            break;
        case RANGE:
            f.setLowerBound(v);
            f.setUpperBound(getVal(components.get(1)));
            break;
        case UPPER_BOUND:
            f.setUpperBound(v);
            break;
        default:
            break;
        }
        return f;
    }

    public TimeField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session) {
        super(fieldname, cfg, desc, gridId, session);
        createComponents();
    }

    @Override
    public void clear() {
        for (Timebox e : components)
            e.setValue(null);
    }
}
