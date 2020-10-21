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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Bandbox;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.component.ext.EventDataSelect28;
import com.arvatosystems.t9t.component.ext.ISelectReceiver;
import com.arvatosystems.t9t.components.Bandpopup28;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;
import de.jpaw.dp.Jdp;


public class BandboxField extends AbstractField<Bandbox> implements ISelectReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(BandboxField.class);
    protected final Bandpopup28 popup = new Bandpopup28();
    protected final IBandboxConverter bandboxConverter;
    protected final String bandboxGridId;
    protected Long value = null;

    @Override
    protected Bandbox createComponent(String suffix) {
        LOGGER.debug("Creating new bandbox (long) component for gridId {}, field {}, bandbox grid is {}",
                gridId, cfg.getFieldName(), bandboxGridId);
        Bandbox d = new Bandbox();
        d.setId(cfg.getFieldName() + suffix);
        d.setHflex("1");
        d.setPlaceholder(label);
        popup.setParent(d);
        popup.setId(cfg.getFieldName() + suffix + ".popup");
        popup.setGridId(bandboxGridId);
        popup.addSelectReceiver(this);
        return d;
    }

    @Override
    public void setSelectionData(EventDataSelect28 eventData) {
        if (eventData == null || eventData.getDwt() == null) {
            clear();
        } else {
            DataWithTracking<BonaPortable, TrackingBase> dwt = eventData.getDwt();
            Ref ref = (Ref) dwt.getData();
            value = ref.getObjectRef();
            components.get(0).setValue(bandboxConverter.describe(ref));
        }
    }

    @Override
    protected boolean componentEmpty(Bandbox c) {
        return c.getValue() == null || c.getValue().length() == 0;
    }

    @Override
    public SearchFilter getSearchFilter() {
        if (empty())
            return null;
        // depending on which values are set, create a lower, upper, equals or range filter
        LongFilter f = new LongFilter();
        f.setFieldName(getFieldName());
        f.setEqualsValue(value);
        return f;
    }

    public BandboxField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session, String bandboxGridId) {
        super(fieldname, cfg, desc, gridId, session);
        if (cfg.getFilterType() != UIFilterType.EQUALITY) {
            LOGGER.error("Only EQUALITY filter is supported for field type {} {}",
                    getClass().getSimpleName(), getFieldName());
            throw new RuntimeException("Only EQUALITY filter is supported for field type " + getClass().getSimpleName() + ": " + getFieldName());
        }
        bandboxConverter = Jdp.getRequired(IBandboxConverter.class, bandboxGridId);
        this.bandboxGridId = bandboxConverter.getBandboxgridId() != null ? bandboxConverter.getBandboxgridId() : bandboxGridId;
        createComponents();
    }

    @Override
    public void clear() {
        value = null;
        for (Bandbox e : components)
            e.setValue(null);
    }
}
