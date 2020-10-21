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

import java.util.List;

import org.zkoss.zul.Comboitem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.search.XenumsetFilter;

import de.jpaw.bonaparte.enums.BonaEnum;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.XEnumDefinition;
import de.jpaw.bonaparte.pojos.meta.XEnumSetDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;
import de.jpaw.enums.AbstractXEnumBase;
import de.jpaw.enums.XEnumFactory;

public class XenumsetField extends EnumBaseField {
    private static final Logger LOGGER = LoggerFactory.getLogger(XenumsetField.class);
    protected final XEnumSetDefinition xesd;
    protected final XEnumDefinition xed;
    protected final EnumDefinition ed;

    @Override
    public SearchFilter getSearchFilter() {
        Comboitem ci = cb.getSelectedItem();
        if (ci == null || empty())
            return null;
        XenumsetFilter f = new XenumsetFilter();
        f.setFieldName(getFieldName());
        f.setXenumsetPqon(xesd.getName());
        f.setEqualsName(ci.getValue());
        f.setSubset(true);               // match any record which includes the selected value, not just equality
        return f;
    }

    public XenumsetField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session, XEnumSetDefinition xesd) {
        super(fieldname, cfg, desc, gridId, session, xesd.getBaseXEnum().getName());

        LOGGER.debug("XenumsetField called with fieldname={}, gridId={}", fieldname, gridId);

        this.xesd = xesd;
        xed = xesd.getBaseXEnum();
        ed = xed.getBaseEnum();
        if (cfg.getFilterType() != UIFilterType.EQUALITY) {
            throw new RuntimeException("xenumset combobox must have equality constraint:" + fieldname);
        }
        createComponents();
        XEnumFactory factory = XEnumFactory.getFactoryByPQON(xed.getName());
        List<AbstractXEnumBase> instances = factory.valuesAsList();
        for (AbstractXEnumBase e: instances) {
            if (enumRestrictions == null || enumRestrictions.contains(e.name())) {
                LOGGER.debug("XenumsetField asks for translation of {}", (BonaEnum)e.getBaseEnum());
                newComboItem(e.name(), session.translateEnum((BonaEnum)e.getBaseEnum()));
            }
        }
    }
}
