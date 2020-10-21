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

import org.joda.time.LocalDate;
import org.zkoss.zul.Datebox;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.DayFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;


public class DayField extends AbstractField<Datebox> {
    @Override
    protected Datebox createComponent(String suffix) {
        Datebox d = new Datebox();
        d.setId(cfg.getFieldName() + suffix);
        d.setHflex("1");
        d.setFormat("medium");
        d.setPlaceholder(label);
        d.setTimeZone("UTC"); // no conversion between user time zone and UTC for dates
        return d;
    }

    @Override
    protected boolean componentEmpty(Datebox c) {
        return c.getValue() == null;
    }

    protected LocalDate getVal(Datebox d) {
        Date vv = d.getValue();
        return vv == null ? null : LocalDate.fromDateFields(vv);
    }

    @Override
    public SearchFilter getSearchFilter() {
        if (empty())
            return null;
        // depending on which values are set, create a lower, upper, equals or range filter
        DayFilter f = new DayFilter();
        f.setFieldName(getFieldName());
        LocalDate v = getVal(components.get(0));
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

    public DayField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session) {
        super(fieldname, cfg, desc, gridId, session);
        createComponents();
    }

    @Override
    public void clear() {
        for (Datebox e : components)
            e.setValue(null);
    }
}
