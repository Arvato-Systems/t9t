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
package com.arvatosystems.t9t.components.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;

import com.arvatosystems.t9t.base.IGridConfigContainer;
import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.ui.UIColumnConfiguration;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;
import de.jpaw.bonaparte.pojos.ui.UIMeta;

public class EditSearchFiltersViewModel {
    private int isSelectedCounter = 0;

    private Window windowComponent = null;
    private final ApplicationSession session = ApplicationSession.get();
    private ILeanGridConfigResolver gridConfigResolver;


    private List<SearchFilterRowVM> searchFiltersVM;

    private boolean selectionEmpty = false;
    private boolean dropDownMissing = false;
    private boolean disableSaveButton = false;

    @Init(superclass = true)
    public void init(@BindingParam("initParams") HashMap<String, Object> initParams, @ContextParam(ContextType.COMPONENT) Component component) {
        windowComponent = (Window) component.getRoot();

        if (initParams != null && initParams.get("gridId") != null) {
            UIGridPreferences uiGridPreferences = IGridConfigContainer.GRID_CONFIG_REGISTRY.get(initParams.get("gridId"));

            gridConfigResolver = new LeanGridConfigResolver(initParams.get("gridId").toString(), session);

            // determine active search filters
            List<UIFilter> activeUIFilters = gridConfigResolver.getFilters();
            Map<String, SearchFilterRowVM> activeUIFilterMap = new HashMap<>(activeUIFilters.size());

            List<UIColumnConfiguration> columns = uiGridPreferences.getColumns();
            List<SearchFilterRowVM> rows = new ArrayList<>(columns.size());

            for (UIFilter uiFilter : activeUIFilters) {
                SearchFilterRowVM row = new SearchFilterRowVM(uiFilter);
                activeUIFilterMap.put(uiFilter.getFieldName(), row);
                rows.add(row);
            }

            for (UIColumnConfiguration column : columns) {
                //only allow root level fields of main dto && binary: not allowed at all
                if ((!isFieldWithinLevelOfMainDTO(column, 0) && activeUIFilterMap.get(column.getFieldName()) == null) ||
                        column.getMeta() == null || column.getMeta().getDataType().equals("binary") ||
                        column.getMeta().getDataCategory().equals("OBJECT") && !isDropdownOrBandbox(column)) {
                    continue;
                }

                SearchFilterRowVM row = activeUIFilterMap.get(column.getFieldName());

                if (row == null) {
                    row = new SearchFilterRowVM(column.getFieldName());
                    rows.add(row);
                }
                row.setFilterTypes(getAvailableFilterType(column));
            }

            this.searchFiltersVM = rows;
        }
    }

    /**
     * Method to get the filter types that based on the metadata of the field.
     *
     * @param column
     * @return
     */
    private List<String> getAvailableFilterType(UIColumnConfiguration column) {
        List<String> allowedTypes = new ArrayList<>(UIFilterType.values().length);

        //if the field is marked as "dropdown" or "bandbox", or is an object (= implicit bandbox), or the field is "tenantRef":
        if (isEqualityDataTypes(column)) {
            allowedTypes.add(UIFilterType.EQUALITY.name());
            return allowedTypes;
        }

        for (UIFilterType type : UIFilterType.values()) {
            //all other types except String: "LIKE" filter is not allowed
            if (type.equals(UIFilterType.LIKE) && !isStringType(column)) {
                continue;
            }

            allowedTypes.add(type.name());
        }

        return allowedTypes;
    }

    /**
     * Check if the field has attributes that has to
     *
     * @param column
     * @return
     */
    private boolean isEqualityDataTypes(UIColumnConfiguration column) {
        final String dataType = column.getMeta().getDataType();

        return
            dataType.equals("boolean") ||
            dataType.equals("enum") ||
            dataType.equals("xenum") ||
            dataType.equals("uuid") ||
            hasProperty(column, "dropdown") ||
            hasProperty(column, "bandbox") ||
            column.getFieldName().equals("tenantRef");
    }

    /**
     * Check if the field has the given property name
     *
     * @param column
     * @param property
     * @return
     */
    private boolean hasProperty(UIColumnConfiguration column, String property) {
        final UIMeta m = column.getMeta();
        return m != null && m.getFieldProperties() != null && m.getFieldProperties().get(property) != null;
    }

    /**
     * Check if the data type of the column is considered as string type,
     * for example: ascii, unicode, uppercase, lowercase, etc
     *
     * @param column
     * @return
     */
    private boolean isStringType(UIColumnConfiguration column) {
        final String dataType = column.getMeta().getDataType();

        return dataType.equals("ascii") || dataType.equals("unicode") || dataType.equals("uppercase") || dataType.equals("lowercase");
    }

    /**
     * Method to check if the given field is within the given level range from the MainDTO
     *
     * @param column
     * @param level
     * @return
     */
    private boolean isFieldWithinLevelOfMainDTO(UIColumnConfiguration column, int level) {

        String fieldName = column.getFieldName();
        int occurrenceCount = 0;

        for (int i = 0; i < fieldName.length(); i++) {
            if (fieldName.charAt(i) == '.') {
                occurrenceCount++;
                if (occurrenceCount > level) {
                    return false;
                }
            }
        }

        return true;
    }

    /*
     * Returns false, if there is a selected row which doesn't contain a defined
     * filter type. Returns false, if less than one search filter is active.
     */
    private boolean validate() {
        //reset the counter and recalculate isSelectedCounter at validation
        isSelectedCounter = 0;
        this.setDropDownMissing(false);
        this.setSelectionEmpty(false);

        for (SearchFilterRowVM row : this.searchFiltersVM) {
            if (row.getSelected() && (row.getCurrentSelection() == null || row.getCurrentSelection().equals(""))) {
                this.setDropDownMissing(true);
            }
            isSelectedCounter++;
        }

        if (this.isSelectedCounter == 0) {
            this.setSelectionEmpty(true);
        }

        return !(this.dropDownMissing || this.selectionEmpty);
    }

    /**
     * Check if the field has any attribute like dropdown or bandbox
     *
     * @param column
     * @return
     */
    private boolean isDropdownOrBandbox(UIColumnConfiguration column) {
        return hasProperty(column, "dropdown") || hasProperty(column, "bandbox");
    }

    @Command
    public void closeWindow() {
        Events.sendEvent("onClose", windowComponent, null);
        windowComponent.onClose();
    }

    @Command
    public void updateSearchFilters() {
        List<UIFilter> filters = new ArrayList<>();
        for (SearchFilterRowVM row : this.searchFiltersVM) {
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

        //Set a true flag to indicate update required.
        Events.sendEvent("onClose", windowComponent, true);
        closeWindow();
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

    @NotifyChange({ "disableSaveButton", "selectionEmpty", "dropDownMissing" })
    @Command
    public void onChecked(@BindingParam("isChecked") boolean isChecked) {
        this.setDisableSaveButton(!this.validate());
    }

    @NotifyChange({ "disableSaveButton", "selectionEmpty", "dropDownMissing" })
    @Command
    public void onComboboxChange() {
        this.setDisableSaveButton(!this.validate());
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

    public boolean isDisableSaveButton() {
        return disableSaveButton;
    }

    public void setDisableSaveButton(boolean disableSaveButton) {
        this.disableSaveButton = disableSaveButton;
    }

    public class SearchFilterRowVM {
        private boolean selected;
        private String filterName;
        private String currentSelection;
        private Boolean negate;
        private List<String> filterTypes;

        public SearchFilterRowVM(String name) {
            selected = false;
            filterName = name;
            currentSelection = "";
            negate = false;
            filterTypes = new ArrayList<>(UIFilterType.values().length);
        }

        public SearchFilterRowVM(UIFilter filter) {
            filterName = filter.getFieldName();
            if (filter.getFilterType() != null) {
                currentSelection = filter.getFilterType().name();
            }
           negate = filter.getNegate();
           selected = true;
        }

        public boolean getSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getFilterName() {
            return filterName;
        }

        public void setFilterName(String filterName) {
            this.filterName = filterName;
        }

        public Boolean getNegate() {
            return negate;
        }

        public void setNegate(Boolean negate) {
            this.negate = negate;
        }

        public String getCurrentSelection() {
            return currentSelection;
        }

        public void setCurrentSelection(String currentSelection) {
            this.currentSelection = currentSelection;
        }

        public List<String> getFilterTypes() {
            return filterTypes;
        }

        public void setFilterTypes(List<String> filterTypes) {
            this.filterTypes = filterTypes;
        }
    }
}
