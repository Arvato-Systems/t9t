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
import org.zkoss.zul.Tabpanel;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.component.ext.EventDataSelect28;
import com.arvatosystems.t9t.component.ext.FilterGenerator;
import com.arvatosystems.t9t.component.ext.IDataSelectReceiver;
import com.arvatosystems.t9t.component.ext.IFilterGenerator;
import com.arvatosystems.t9t.component.ext.IGridIdOwner;
import com.arvatosystems.t9t.components.extensions.ITabpanelExtension;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;

/** A tabpanel usually hosts its own grid to display data. It therefore must have a gridId.
 * It may or may not have an own filter section.
 * The major part of the filter is provided by an event.
 *
 *  The tabpanel must have an ID assigned, this is used to register it at the tabbox, and responsible
 *  for the tab description text. */
public class Tabpanel28 extends Tabpanel implements IdSpace, IGridIdOwner, IDataSelectReceiver {
    private static final long serialVersionUID = -7854204476233671081L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Tabpanel28.class);

    private EventDataSelect28 lastSelected;
    private final ApplicationSession as = ApplicationSession.get();
    private Tabbox28 myBox;
    private Grid28 targetGrid;  // could wire events for this to be more flexible...

    private boolean postSelected = false;
    private String gridId;
    private String viewModelId;
    private CrudViewModel<BonaPortable, TrackingBase> crudViewModel;  // set when gridId is defined
    private IFilterGenerator filterGenerator;   // filter to be used if no tab specific filter is provided
    private ITabpanelExtension extension = null;
    private boolean selected28 = false;

    public Tabpanel28() {
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
        LOGGER.debug("Tabpanel28.onSelect({})", getId());
        if (extension != null)
            extension.onSelect(this);
    }

    @Listen("onCreate")
    public void onCreate() {
        LOGGER.debug("Tabpanel28.onCreate({})", getId());
        if (extension != null)
            extension.beforeOnCreate(this);

        // find the tab box
        myBox = (Tabbox28)getTabbox();// GridIdTools.findTabbox28(this);
        myBox.register(this);

        // set the filter generator from default, if no specific one has been defined
        if (filterGenerator == null)
            filterGenerator = myBox.getDefaultFilterGenerator();

        if (extension != null)
            extension.afterOnCreate(this);
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
        LOGGER.debug("Tabpanel28() assigned grid ID {}", gridId);
        this.gridId = gridId;
        setViewModelId(GridIdTools.getViewModelIdByGridId(gridId));
    }

    @Override
    public String getGridId() {
        return gridId;
    }

    @Override
    public void setSelectionData(EventDataSelect28 eventData) {
        lastSelected = eventData;
        if (postSelected) {
            Events.postEvent(new Event(EventDataSelect28.ON_DATA_SELECT, this, eventData));
        }
        SearchFilter filter = SearchFilters.FALSE;
        if (eventData != null) {
            DataWithTracking<BonaPortable, TrackingBase> dwt = eventData.getDwt();
            if (dwt != null && dwt.getData() != null) {
                BonaPortable d = dwt.getData();
                Long ref = d instanceof Ref ? ((Ref)d).getObjectRef() : -1L;
                LOGGER.debug("Tabpanel28 {} received selected event for {}", getId(), ref);
                filter = filterGenerator.createFilter(d);
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
    public void setExtension28(String qualifier) {
        this.extension = Jdp.getRequired(ITabpanelExtension.class, qualifier);
        this.extension.init(this);
    }

    public boolean isSelected28() {
        return selected28;
    }

    public void setSelected28(boolean selected28) {
        this.selected28 = selected28;
    }
}
