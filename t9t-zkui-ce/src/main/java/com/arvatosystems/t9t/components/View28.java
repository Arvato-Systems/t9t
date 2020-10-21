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

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.Binder;
import org.zkoss.bind.impl.BinderUtil;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Div;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.component.ext.EventDataSelect28;
import com.arvatosystems.t9t.component.ext.IDataSelectReceiver;
import com.arvatosystems.t9t.component.ext.IViewModelOwner;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;

/** The View28 component also serves as the ViewModel for view only screens (a subset of CRUD, without any buttons).
 */
public class View28 extends Div implements IViewModelOwner, IDataSelectReceiver, IdSpace {
    private static final long serialVersionUID = -82034057035123L;
    private static final Logger LOGGER = LoggerFactory.getLogger(View28.class);

    protected IDataSelectReceiver detailsSection;  // the data form, which may be a tabbbox with separate panels
    protected Permissionset perms;  // available after onCreate

    protected String viewModelId;
    protected CrudViewModel<BonaPortable, TrackingBase> crudViewModel;  // set when gridId is defined
    protected final ApplicationSession session = ApplicationSession.get();

    public View28() {
        super();
        LOGGER.debug("new View28() created");
    }

    @Listen("onCreate")
    public void onCreate() {
        LOGGER.debug("View28.onCreate()");
        GridIdTools.enforceViewModelId(this);

        perms = GridIdTools.getPermissionFromAnchestor(this);
        getParent().addEventListener(EventDataSelect28.ON_DATA_SELECT, (ev) -> {
            setSelectionData((EventDataSelect28) ev.getData());
        });
    }

    @Override
    public void setSelectionData(EventDataSelect28 eventData) {
        if (eventData != null) {
            LOGGER.debug("received event data {}", eventData);
            if (detailsSection != null) {
                detailsSection.setSelectionData(eventData);
            }
            Binder binder = BinderUtil.getBinder(this);
//        if (binder != null) {
//            Object viewModelInstance = binder.getViewModel();
//            LOGGER.debug("viewmodel is of class {}", viewModelInstance.getClass().getCanonicalName());
//            ((ViewOnlyVM)viewModelInstance).setSelectionData(eventData.getDwt());
//        }
        // AbstractCrudVM viewModelInstance = (AbstractCrudVM)binder.getViewModel();
            binder.sendCommand("setSelectionData", Collections.singletonMap("dwt", eventData.getDwt()));
        }
    }

    @Override
    public CrudViewModel<BonaPortable, TrackingBase> getCrudViewModel() {
        GridIdTools.enforceViewModelId(this);
        return crudViewModel;
    }

    @Override
    public String getViewModelId() {
        return viewModelId;
    }

    @Override
    public void setViewModelId(String viewModelId) {
        LOGGER.debug("Setting view model ID to {}", viewModelId);
        this.viewModelId = viewModelId;
        crudViewModel = GridIdTools.getViewModelByViewModelId(viewModelId);
    }

    @Override
    public ApplicationSession getSession() {
        return session;
    }
}
