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
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Window;

import com.arvatosystems.t9t.tfi.general.Constants;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

/** A single page main application window.
 * It sets a title, which is looked up by id + ".title".
 */
public class Window28 extends Window { // implements IGridIdOwner {
    private static final long serialVersionUID = 7107189989393924492L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Window28.class);
//    private String gridId;
//    private String viewModelId;
//    private CrudViewModel<BonaPortable, TrackingBase> crudViewModel;  // set when gridId is defined

    public Window28() {
        super();
        setVflex("1");
        setBorder(false);
        setBorder("none");
        LOGGER.debug("Creating new Window28");
    }

    @Override
    public void setId(String id) {
        super.setId(id);
//        if (gridId == null)
//          gridId = id;  // also set gridId

        if (id != null && id.contains("_" + Constants.Application.CachingType.CREATE_WITHOUT_CACHING)) {
            LOGGER.debug("Cache postfix detected id = {}, removing postfix {}", id,
                    "_" + Constants.Application.CachingType.CREATE_WITHOUT_CACHING);
            id = id.replaceAll("_" + Constants.Application.CachingType.CREATE_WITHOUT_CACHING, "");
        }

        LOGGER.debug("Setting Window28 id to {}", id);
//        String label = ApplicationSession.get().translate(null, id + ".title");
//        setTitle(label);
    }

//    public String getGridId() {
//        if (gridId == null) {
//            gridId = getId();  // use the id as fallback when no gridId has been specified
//            LOGGER.debug("Window28({}): no gridId defined, using my id", gridId);
//        }
//        setViewModelId(GridIdTools.getViewModelIdByGridId(gridId));
//        return gridId;
//    }
//
//    public void setGridId(String gridId) {
//        this.gridId = gridId;
//        if (getId() == null)
//          super.setId(gridId);  // also set id
//        LOGGER.debug("Setting Window28 gridId to {}", gridId);
//    }
//
//    @Listen("onCreate")
//    public void onCreate() {
//        LOGGER.debug("Window28.onCreate()");
//        GridIdTools.enforceGridId(this);
//    }
//
//    @Override
//    public CrudViewModel<BonaPortable, TrackingBase> getCrudViewModel() {
//        GridIdTools.enforceGridId(this);
//        return crudViewModel;
//    }
//
//    @Override
//    public String getViewModelId() {
//        return viewModelId;
//    }
//
//    @Override
//    public void setViewModelId(String viewModelId) {
//        this.viewModelId = viewModelId;
//        crudViewModel = GridIdTools.getViewModelByViewModelId(viewModelId);
//    }
}
