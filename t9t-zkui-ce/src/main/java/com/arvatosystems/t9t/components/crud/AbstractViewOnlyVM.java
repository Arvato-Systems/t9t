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
package com.arvatosystems.t9t.components.crud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Init;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.services.T9TRemoteUtils;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;

@SuppressWarnings("rawtypes")
public abstract class AbstractViewOnlyVM<DTO extends BonaPortable, TRACKING extends TrackingBase> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractViewOnlyVM.class);
    protected final ApplicationSession session = ApplicationSession.get();
    protected final T9TRemoteUtils remoteUtil = Jdp.getRequired(T9TRemoteUtils.class);

    protected CrudViewModel<DTO, TRACKING> crudViewModel;  // set when gridId is defined

    // the data
    protected DTO data;
    protected TRACKING tracking;
    protected Long tenantRef;
    protected String tenantId;

    // to be overridden in case arrays need to be initialized
    protected void clearData() {   // TODO: init child objects if exist, do it via injected class, qualifier to be passed to @Init
        data = crudViewModel == null ? null : (DTO) crudViewModel.dtoClass.newInstance();
        tracking = null;
        tenantRef = session.getTenantRef();
    }

    // to be overridden in case arrays need to be sanitized
    protected void loadData(DataWithTracking<DTO, TRACKING> dwt) {
        if (dwt == null || dwt.getData() == null) {
            clearData();
        } else {
            data = (DTO) dwt.getData().ret$MutableClone(true, true);
            tracking = dwt.getTracking();
            if (dwt instanceof DataWithTrackingW)
                tenantRef = ((DataWithTrackingW)dwt).getTenantRef();
            else if (dwt instanceof DataWithTrackingS)
                tenantId = ((DataWithTrackingS)dwt).getTenantId();
        }
    }


    @Init
    public void setInitial(@BindingParam("vmId") String viewModelId) {
        CrudViewModel tmp = IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.get(viewModelId);
        if (tmp == null) {
            LOGGER.error("*** No viewModel of name {} found! CRUD will not work! ***", viewModelId);
        } else {
            LOGGER.debug("Setting initial VM as {}", viewModelId);
        }
        crudViewModel = tmp;
        clearData();
    }

    // boilerplate below

    public DTO getData() {
        return data;
    }

    public Long getTenantRef() {
        return tenantRef;
    }

    public void setTenantRef(Long tenantRef) {
        this.tenantRef = tenantRef;
    }

    public TrackingBase getTracking() {
        return tracking;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
