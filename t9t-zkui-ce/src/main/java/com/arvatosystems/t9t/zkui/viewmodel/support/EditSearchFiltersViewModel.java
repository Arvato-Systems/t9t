/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Window;

import com.arvatosystems.t9t.base.IGridConfigContainer;
import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.zkui.components.grid.ILeanGridConfigResolver;
import com.arvatosystems.t9t.zkui.components.grid.LeanGridConfigResolver;
import com.arvatosystems.t9t.zkui.services.ISearchFilterConfigCreator;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;

import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;
import de.jpaw.dp.Jdp;

public class EditSearchFiltersViewModel {
    private int isSelectedCounter = 0;

    private Window windowComponent = null;
    private final ApplicationSession session = ApplicationSession.get();
    private ILeanGridConfigResolver gridConfigResolver;
    private final ISearchFilterConfigCreator searchFilterConfigCreator = Jdp.getRequired(ISearchFilterConfigCreator.class);
    private UIGridPreferences uiGridPreferences = null;
    private boolean selectionEmpty = false;
    private boolean dropDownMissing = false;

    private List<SearchFilterRowVM> searchFiltersVM;

    @Wire("#component")
    private Div div;

    @Init(superclass = true)
    public void init(@BindingParam("initParams") HashMap<String, Object> initParams, @ContextParam(ContextType.COMPONENT) Component component) {
        windowComponent = (Window) component.getRoot();

        if (initParams != null && initParams.get("gridId") != null) {
            uiGridPreferences = IGridConfigContainer.GRID_CONFIG_REGISTRY.get(initParams.get("gridId"));
            gridConfigResolver = new LeanGridConfigResolver(initParams.get("gridId").toString(), session);
        }
    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
        div.setVflex("1");
        searchFilterConfigCreator.createComponent(div, uiGridPreferences, gridConfigResolver.getFilters());
    }


    @Command
    public void closeWindow() {
        Events.sendEvent("onClose", windowComponent, null);
        windowComponent.onClose();
    }

    @Command
    @NotifyChange({ "selectionEmpty", "dropDownMissing" })
    public void updateSearchFilters() {
        List<SearchFilterRowVM> selectedFilters = searchFilterConfigCreator.getSelectedFilters();
        if (validate(selectedFilters)) {
            List<UIFilter> filters = new ArrayList<>();
            for (SearchFilterRowVM row : selectedFilters) {
                if (row.getSelected()) {
                    UIFilter filter = new UIFilter();
                    filter.setFieldName(row.getFilterName());
                    filter.setFilterType(UIFilterType.valueOf(row.getCurrentSelection()));
                    filter.setNegate(row.getNegate());
                    filters.add(filter);
                }
            }
            gridConfigResolver.setFilters(filters);
            gridConfigResolver.save(false);

            // Set a true flag to indicate update required.
            Events.sendEvent("onClose", windowComponent, true);
            closeWindow();
        }
    }

    @NotifyChange("searchFiltersVM")
    @Command
    public void onDrop(@BindingParam("draggedId") String draggedId, @BindingParam("droppedId") String droppedId) {
        Integer draId = extractTemplateId(draggedId);
        Integer droId = extractTemplateId(droppedId);
        if (droId == null) {
            // dragged element needs to be removed and put at the end of the
            // list
            SearchFilterRowVM ele = this.searchFiltersVM.remove(draId.intValue());
            this.searchFiltersVM.add(this.isSelectedCounter - 1, ele);
        } else {
            // dragged element needs to be placed to the middle of the list
            SearchFilterRowVM ele = this.searchFiltersVM.remove(draId.intValue());
            this.searchFiltersVM.add(droId, ele);
        }
    }

    /*
     * Returns false, if there is a selected row which doesn't contain a defined
     * filter type. Returns false, if less than one search filter is active.
     */
    private boolean validate(List<SearchFilterRowVM> selectedFilters) {
        dropDownMissing = false;
        selectionEmpty = selectedFilters.isEmpty();

        for (SearchFilterRowVM f : selectedFilters) {
            if (f.getSelected() && (f.getCurrentSelection() == null || f.getCurrentSelection().equals(""))) {
                dropDownMissing = true;
                break;
            }
        }

        return !(this.dropDownMissing || this.selectionEmpty);
    }

    private static Integer extractTemplateId(String id) {
        String[] arr = id.split("-");
        if (arr.length == 2) {
            return Integer.valueOf(arr[1]);
        } else {
            return null;
        }
    }

    public List<SearchFilterRowVM> getSearchFiltersVM() {
        return searchFiltersVM;
    }

    public void setSearchFiltersVM(List<SearchFilterRowVM> searchFiltersVM) {
        this.searchFiltersVM = searchFiltersVM;
    }

    public boolean isSelectionEmpty() {
        return selectionEmpty;
    }

    public void setSelectionEmpty(boolean selectionEmpty) {
        this.selectionEmpty = selectionEmpty;
    }

    public boolean isDropDownMissing() {
        return dropDownMissing;
    }

    public void setDropDownMissing(boolean dropDownMissing) {
        this.dropDownMissing = dropDownMissing;
    }
}
