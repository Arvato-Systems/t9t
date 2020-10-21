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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Div;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.component.ext.IDataFactoryOwner;
import com.arvatosystems.t9t.component.ext.IDataFieldFactory;
import com.arvatosystems.t9t.component.ext.IViewModelOwner;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;

/** A stripped down version of Form28, which does not support CRUD parents, and does not inherit from Grid, but Div.
 */
public class ViewModel28 extends Div implements IDataFactoryOwner, IViewModelOwner {
    private static final long serialVersionUID = -60360112038781123L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewModel28.class);
    private final ApplicationSession as = ApplicationSession.get();
    private final IDataFieldFactory dataFieldFactory = Jdp.getRequired(IDataFieldFactory.class);
    private CrudViewModel<BonaPortable, TrackingBase> crudViewModel;
    private String viewModelId;

    public ViewModel28() {
        super();
        LOGGER.debug("new ViewModel28() created");
        setVflex("1");
    }

    @Override
    public void setViewModelId(String viewModelId) {
        this.viewModelId = viewModelId;
        crudViewModel = GridIdTools.getViewModelByViewModelId(viewModelId);
    }

    @Listen("onCreate")
    public void onCreate() {
        GridIdTools.enforceViewModelId(this);
        if (crudViewModel == null) {
            LOGGER.error("ViewModel28 without viewModelId");
            throw new RuntimeException("ViewModel28 without viewModelId");
        }
    }

    @Override
    public CrudViewModel<BonaPortable, TrackingBase> getCrudViewModel() {
        return crudViewModel;
    }

    @Override
    public IDataFieldFactory getDataFactory() {
        return dataFieldFactory;
    }

    @Override
    public String getViewModelId() {
        return viewModelId;
    }

    @Override
    public ApplicationSession getSession() {
        return as;
    }
}
