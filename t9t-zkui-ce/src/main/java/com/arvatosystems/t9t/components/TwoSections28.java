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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Vlayout;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.component.ext.EventDataSelect28;
import com.arvatosystems.t9t.component.ext.IDataSelectReceiver;
import com.arvatosystems.t9t.component.ext.IGridIdOwner;
import com.arvatosystems.t9t.component.ext.IPermissionOwner;
import com.arvatosystems.t9t.component.fields.fixedfilters.IFixedFilter;
import com.google.common.base.Strings;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;

public class TwoSections28 extends Vlayout implements IGridIdOwner, IPermissionOwner {
    private static final long serialVersionUID = -4837267188947096696L;
    private static final Logger LOGGER = LoggerFactory.getLogger(TwoSections28.class);

    protected final ApplicationSession as = ApplicationSession.get();
    protected String gridId;
    protected String viewModelId;
    protected CrudViewModel<BonaPortable, TrackingBase> crudViewModel;  // set when gridId is defined
    // protected String gridContext;
    protected boolean isSolr;
    protected Permissionset permissions = Permissionset.ofTokens();
    protected SearchFilter fixedFilter = null;


    @Wire("#resultsGroup") protected Groupbox28 resultsGroup;
    @Wire("#filterGroup") protected Groupbox28 filterGroup;
    @Wire protected Filter28 filters;
    @Wire protected Grid28 main;

    protected Togglefilter28 toggleFilter;

    public TwoSections28() {
        super();
        this.setVflex("1");
        LOGGER.debug("new TwoSections28() created");
        Executions.createComponents("/component/twosections28.zul", this, null);
        Selectors.wireComponents(this, this, false);
        // Selectors.wireEventListeners(this, this);

        filters.addEventListener("onSearch", (Event ev) -> {
            Object o = ev.getData();
            LOGGER.debug("Got onSearch event from filter28! data is {}, fixedFilter is {}",
                    o == null ? "NULL" : o.getClass().getCanonicalName(),
                    fixedFilter == null ? "NULL" : fixedFilter
            );
            if (o == null || o instanceof SearchFilter) {
                main.setFilter1(SearchFilters.and(fixedFilter, (SearchFilter) o));
            }
        });
    }

    public void addDataSelectReceiver(IDataSelectReceiver recv) {
        main.addEventListener(EventDataSelect28.ON_DATA_SELECT, (Event ev) -> {
            recv.setSelectionData((EventDataSelect28)ev.getData());
        });
    }

    @Listen("onCreate")
    public void onCreate() {
        // if id is set, but no gridId, use the id as gridId
        LOGGER.debug("TwoSections28.onCreate({}, {})", getId(), gridId);
        if (gridId == null && getId() != null) {
            setGridId(getId());
        }
        GridIdTools.enforceGridId(this);  // a parent will rarely provide a gridId, because TwoSesctions is the outer component
//        if (gridContext != null) {
//            main.setGridContext(gridContext, getId() == null ? gridId : getId());
//        }

//        // examine args
//        Map<?, ?> args = Executions.getCurrent().getArg();
//        if (args == null) {
//            LOGGER.debug("args are null");
//        } else {
//            LOGGER.debug("arg map has {} entries", args.size());
//            for (Map.Entry<?, ?> me : args.entrySet()) {
//                LOGGER.debug("Map entry is {} -> {}", me.getKey(), me.getValue());
//            }
//        }
        // use own param passing
        SearchFilter f = as.getFilterForPresetSearch();
        if (f != null) {
            // run initial search with this
            //main.setFilter1(f);  // direct call may be too early - let's complete initialization first
            LOGGER.debug("Posting search event with filter {}", f);
            Events.postEvent(new Event("onSearch", filters, f));  // low prio event to be queued....
        }
    }

    public void refreshCurrentItem() {
        main.refreshCurrentItem();
    }

    public void refreshAllItems() {
         main.search();
    }

    @Override
    public String getGridId() {
        return gridId;
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
    public void setGridId(String gridId) {
        LOGGER.debug("TwoSections28() assigned grid ID {}", gridId);
        this.gridId = gridId;
        if (Strings.isNullOrEmpty(getId())) {
            // also use it for the id
            LOGGER.debug("    *** also assigning {} as id", gridId);
            setId(gridId);
        }
        permissions = as.getPermissions(gridId);
        LOGGER.debug("Grid ID {} has permissions {}", gridId, permissions);
        setViewModelId(GridIdTools.getViewModelIdByGridId(gridId));
    }

    @Override
    public void setViewModelId(String viewModelId) {
        this.viewModelId = viewModelId;
        crudViewModel = GridIdTools.getViewModelByViewModelId(viewModelId);
        isSolr = crudViewModel.searchClass.getProperty("isSolr") != null;
        if (isSolr) {
            LOGGER.debug("{} supports SOLR search", viewModelId);
            // create a tabbox and move the current filter into it
            toggleFilter = new Togglefilter28();
            toggleFilter.setStdFilter(filters);
            toggleFilter.setParent(filterGroup);

            filters.addEventListener("onToSOLR", ev -> toggleFilter.showSolr());
            toggleFilter.addEventListener("onSearch", ev -> {
                Object o = ev.getData();
                LOGGER.debug("Got onSearch event from SOLR! data is {}", o == null ? "NULL" : o.getClass().getCanonicalName());
                if (o == null || o instanceof String)
                    main.setFilter1((String) o);  // SOLR type search
            });
        }
    }

    /** A gridContext is forwarded to the contained grid. */
    public void setGridContext(String entries) {
        // gridContext = entries;
        main.setGridContext(entries, gridId != null ? gridId : getId());
    }

    @Override
    public Permissionset getPermissions() {
        return permissions;
    }

    @Override
    public ApplicationSession getSession() {
        return as;
    }

    public void setPageSize(int size) {
        main.setPageSize(size);
    }

    public int getPageSize() {
        return main.getPageSize();
    }

    // allow to use a nonstandard distribution of space
    public String getVflex2() {
        return resultsGroup.getVflex();
    }

    public void setVflex2(String vflex) {
        resultsGroup.setVflex(vflex);
    }

    /** Sets the fixed filter by qualifier. */
    public void setFixedFilter(String filterName) {
        fixedFilter = Jdp.getRequired(IFixedFilter.class, filterName).get();
    }

    /** Sets the fixed filter directly (via VM). */
    public void setAdditionalFilter(SearchFilter filter) {
        LOGGER.debug("Setting additional filter to {}", filter);
        fixedFilter = filter;
    }

    public void setVlayoutVisible(boolean visible) {
        filterGroup.setVisible(visible);
        resultsGroup.setVisible(visible);
    }

    public void setFilterGroupVisible(boolean visible) {
        filterGroup.getParent().setVisible(visible);
    }
    public void isFilterGroupVisible() {
        filterGroup.isVisible();
    }

    public void setListHeaders(List<String> listHeaders) {
         main.setListHeaders(listHeaders);
    }
    public Grid28 getGrid28() {
        return this.main;
    }
    // returns true if an item is selected in the grid listbox
    public boolean isItemSelected() {
        return main.isItemSelected();
    }

    public Filter28 getFilters() {
        return filters;
    }
}
