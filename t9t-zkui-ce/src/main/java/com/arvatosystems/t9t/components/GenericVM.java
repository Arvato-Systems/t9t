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
package com.arvatosystems.t9t.components;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;

/** Generic viewModel for modal windows MVVM. */
@SuppressWarnings("rawtypes")
public class GenericVM {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericVM.class);
    private BonaPortable data;
    private TrackingBase tracking;
    private Long tenantRef;
    private String tenantId;

    @Init
    public void setInitial() {  // @BindingParam("inst") HashMap<String, Object> inst
        Map<?,?> arg = Executions.getCurrent().getArg();  // the 3rd parameter to Executions.createObjects() is available via getArg()
        Object pojo = arg == null ? null : arg.get("inst");
        LOGGER.debug("Setting initial VM as {}", arg == null ? "NULL" : pojo);
        if (pojo instanceof DataWithTracking) {
            DataWithTracking<BonaPortable, TrackingBase> dwt = (DataWithTracking)pojo;
            data = dwt.getData();
            tracking = dwt.getTracking();
            if (dwt instanceof DataWithTrackingW)
                tenantRef = ((DataWithTrackingW)dwt).getTenantRef();
            else if (dwt instanceof DataWithTrackingS)
                tenantId = ((DataWithTrackingS)dwt).getTenantId();
        } else {
            data = (BonaPortable)pojo;
            tracking = null;
            tenantRef = null;
        }
    }

    public BonaPortable getData() {
        return data;
    }

    public Long getTenantRef() {
        return tenantRef;
    }

    public TrackingBase getTracking() {
        return tracking;
    }

    public String getTenantId() {
        return tenantId;
    }
}
