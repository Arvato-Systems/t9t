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

import com.arvatosystems.t9t.base.crud.RefResolverRequest;
import com.arvatosystems.t9t.base.crud.RefResolverResponse;
import com.arvatosystems.t9t.component.ext.EventDataSelect28;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.apiw.Ref;

public abstract class AbstractLongBandboxDataField extends AbstractBandboxDataField<Long> {
    protected AbstractLongBandboxDataField(DataFieldParameters params, String gridId) {
        super(params, gridId);
    }

    protected Long resolve(RefResolverRequest<?> rq) {
        RefResolverResponse res = remoteUtils.executeExpectOk(rq, RefResolverResponse.class);
        return res.getKey();
    }

    @Override
    public void setSelectionData(EventDataSelect28 eventData) {
        if (eventData == null || eventData.getDwt() == null) {
            clear();
        } else {
            DataWithTracking<BonaPortable, TrackingBase> dwt = eventData.getDwt();
            setValue(((Ref)dwt.getData()).getObjectRef());
        }
    }
}
