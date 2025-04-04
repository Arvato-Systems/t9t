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

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Bandbox;

import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.zkui.components.EventDataSelect28;
import com.arvatosystems.t9t.zkui.components.ISelectReceiver;
import com.arvatosystems.t9t.zkui.components.basic.Bandpopup28;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.dp.Jdp;

public abstract class AbstractBandboxDataField<T> extends AbstractDataField<Bandbox, T> implements ISelectReceiver {
    protected final Bandbox c = new Bandbox();
    protected final Bandpopup28 popup;
    protected final IT9tRemoteUtils remoteUtils = Jdp.getRequired(IT9tRemoteUtils.class);

    protected AbstractBandboxDataField(DataFieldParameters params, String gridId) {
        super(params);
        popup = new Bandpopup28();

        c.setSclass("bandboxField28");
        c.setWidgetOverride("slideDown_", "function(pp) {"
            + "jq(this.getPopupNode_()).css({'left': '0','top': '0', 'right':'0', 'bottom':'0', 'width':'80%','height':'85%','margin':'auto','display':''});"
            + "this.$supers('slideDown_',arguments);"
            + "}");


        popup.setParent(c);
        popup.setId(path + ".popup");
        popup.setGridId(gridId);
        popup.addSelectReceiver(this);
        setConstraints(c, null);
    }

    protected BonaPortable dtoForObjectRef(Long objectRef) {
        if (objectRef == null)
            return null;
        SearchCriteria rq = popup.getCrudViewModel().searchClass.newInstance();
        LongFilter f = new LongFilter();
        f.setFieldName("objectRef");
        f.setEqualsValue(objectRef);
        rq.setSearchFilter(f);
        ReadAllResponse res = remoteUtils.executeExpectOk(rq, ReadAllResponse.class);
        if (res.getReturnCode() == 0) {
            if (res.getDataList().size() > 0) {
                DataWithTracking<BonaPortable, TrackingBase> dwt = (DataWithTracking<BonaPortable, TrackingBase>) res.getDataList().get(0);
                return dwt.getData();
            }
        }
        return null;
    }

    @Override
    public void setSelectionData(EventDataSelect28 eventData) {
        if (eventData == null || eventData.getDwt() == null) {
            clear();
        } else {
            DataWithTracking<BonaPortable, TrackingBase> dwt = eventData.getDwt();
            setValue((T)dwt.getData());
            Events.postEvent(new Event(Events.ON_CHANGE, c, null));
        }
    }

    @Override
    public Bandbox getComponent() {
        return c;
    }

    @Override
    public void clear() {
        c.setRawValue(null);
    }

    @Override
    public boolean empty() {
        return c.getValue() == null || c.getValue().length() == 0;
    }


}
