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
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Div;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.component.ext.EventDataSelect28;
import com.arvatosystems.t9t.component.ext.FilterGenerator;
import com.arvatosystems.t9t.component.ext.IDataSelectReceiver;
import com.arvatosystems.t9t.component.ext.IFilterGenerator;
import com.arvatosystems.t9t.component.ext.IGridIdOwner;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.apiw.Ref;

/** A direct link from detail section to sub grid, where Tabbox28 and Tabpanel is not required because
 * a single page exists. This can be used to link 2 grids with each other. */
public class Direct28 extends Div implements IdSpace, IGridIdOwner, IDataSelectReceiver {
    private static final long serialVersionUID = -78512376233671081L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Direct28.class);

    private final ApplicationSession as = ApplicationSession.get();
    private Grid28 targetGrid;  // could wire events for this to be more flexible...

    private boolean postSelected = false;
    private String gridId;
    private String viewModelId;
    private CrudViewModel<BonaPortable, TrackingBase> crudViewModel;  // set when gridId is defined
    private IFilterGenerator filterGenerator;   // filter to be used if no tab specific filter is provided

    public Direct28() {
        super();
    }

    public void setFilterFieldname(String fieldname) {
        filterGenerator = FilterGenerator.filterForFieldname(fieldname);
    }

    public void setFilterName(String name) {
        filterGenerator = FilterGenerator.filterForName(name);
    }

    public IFilterGenerator getFilterGenerator() {
        return filterGenerator;
    }


    @Listen("onSelect")
    public void onSelect() {
        LOGGER.debug("Direct28.onSelect({})", getId());
    }

    @Listen("onCreate")
    public void onCreate() {
        LOGGER.debug("Direct28.onCreate({})", getId());

        if (targetGrid != null) {
            targetGrid.setFilter2(SearchFilters.FALSE);
        }
    }

    @Override
    public CrudViewModel<BonaPortable, TrackingBase> getCrudViewModel() {
        GridIdTools.enforceGridId(this);
        return crudViewModel;
    }

    @Override
    public String getViewModelId() {
        return viewModelId;
    }

    @Override
    public void setViewModelId(String viewModelId) {
        this.viewModelId = viewModelId;
        crudViewModel = GridIdTools.getViewModelByViewModelId(viewModelId);
    }

    @Override
    public void setGridId(String gridId) {
        LOGGER.debug("Direct28() assigned grid ID {}", gridId);
        this.gridId = gridId;
        setViewModelId(GridIdTools.getViewModelIdByGridId(gridId));
    }

    @Override
    public String getGridId() {
        return gridId;
    }

    @Override
    public void setSelectionData(EventDataSelect28 eventData) {
        if (postSelected) {
            Events.postEvent(new Event(EventDataSelect28.ON_DATA_SELECT, this, eventData));
        }
        SearchFilter filter = SearchFilters.FALSE;
        if (eventData != null) {
            DataWithTracking<BonaPortable, TrackingBase> dwt = eventData.getDwt();
            if (dwt != null && dwt.getData() != null) {
                BonaPortable d = dwt.getData();
                Long ref = d instanceof Ref ? ((Ref)d).getObjectRef() : -1L;
                LOGGER.debug("Direct28 {} received selected event for {}", getId(), ref);

                //Direct28 preset search filter will override the tab specific filter
                filter = as.getFilterForPresetSearchOnDirect28();
                if (filter == null) {
                    filter = filterGenerator.createFilter(d);
                }
            }
        }

        if (targetGrid != null)
            targetGrid.setFilter2(filter);
    }

    public Grid28 getTargetGrid() {
        return targetGrid;
    }

    public void setTargetGrid(Grid28 targetGrid) {
        this.targetGrid = targetGrid;
    }

    @Override
    public ApplicationSession getSession() {
        return as;
    }

    public boolean isPostSelected() {
        return postSelected;
    }

    public void setPostSelected(boolean postSelected) {
        this.postSelected = postSelected;
    }
}
