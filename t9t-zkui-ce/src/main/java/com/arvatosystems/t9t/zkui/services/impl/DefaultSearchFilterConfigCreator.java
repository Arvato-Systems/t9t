/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Popup;

import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.zkui.components.dropdown28.SimpleListModelExt;
import com.arvatosystems.t9t.zkui.services.ISearchFilterConfigCreator;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.viewmodel.support.SearchFilterRowVM;

import de.jpaw.bonaparte.pojos.ui.UIColumnConfiguration;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;
import de.jpaw.bonaparte.pojos.ui.UIMeta;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Fallback;

@Dependent
@Fallback
public class DefaultSearchFilterConfigCreator implements ISearchFilterConfigCreator {

    protected List<SearchFilterRowVM> rows            = null;
    protected List<SearchFilterRowVM> selectedFilters = null;
    protected final ApplicationSession      as              = ApplicationSession.get();
    protected String                  viewModelId     = null;

    @Override
    public void createComponent(Div parent, UIGridPreferences uiGridPreferences, List<UIFilter> selectedUiFilters) {
        List<UIColumnConfiguration> uiColumns = uiGridPreferences.getColumns();
        viewModelId = uiGridPreferences.getViewModel();
        rows = new ArrayList<>(uiColumns.size());
        selectedFilters = new ArrayList<>(selectedUiFilters.size() * 2);
        Map<String, SearchFilterRowVM> activeUIFilterMap = new HashMap<>(uiColumns.size());

        for (UIFilter uiFilter : selectedUiFilters) {
            SearchFilterRowVM row = new SearchFilterRowVM(uiFilter);
            activeUIFilterMap.put(uiFilter.getFieldName(), row);
            selectedFilters.add(row);
            rows.add(row);
        }

        List<String> excludedUiColumns = new ArrayList<>();
        for (UIColumnConfiguration column : uiColumns) {
            // only allow root level fields of main dto && binary: not allowed at all
            if ((!isFieldWithinLevelOfMainDTO(column, 0) && activeUIFilterMap.get(column.getFieldName()) == null)
                    || column.getMeta() == null || column.getMeta().getDataType().equals("binary")
                    || column.getMeta().getDataCategory().equals("OBJECT") && !isDropdownOrBandbox(column)) {
                continue;
            }

            if (hasExcludedProperties(column) || excludedUiColumns.contains(getParentPath(column.getFieldName()))) {
                excludedUiColumns.add(column.getFieldName());
                continue;
            }

            SearchFilterRowVM row = activeUIFilterMap.get(column.getFieldName());

            if (row == null) {
                row = new SearchFilterRowVM(column.getFieldName());
                if (column.getMeta() != null && column.getMeta().getFieldProperties() != null) {
                    String filterQualifier = column.getMeta().getFieldProperties().get(Constants.UiFieldProperties.FILTER_QUALIFIER);
                    row.setQualifier(filterQualifier);
                }
                rows.add(row);
            }
            row.setFilterTypes(getAvailableFilterType(column));
        }

        createListbox(parent, rows);
    }

    private void createListbox(Div parent, List<SearchFilterRowVM> xrows) {
        Listbox listbox = new Listbox();
        listbox.setVflex("1");
        listbox.setParent(parent);
        Listhead head = new Listhead();
        head.setParent(listbox);
        createHeader(head);
        listbox.setModel(new SimpleListModelExt<SearchFilterRowVM>(xrows));
        listbox.setItemRenderer(new ListitemRenderer<SearchFilterRowVM>() {
            @Override
            public void render(Listitem item, SearchFilterRowVM data, int index) throws Exception {
                Combobox combobox = new Combobox();
                Checkbox negateCb = new Checkbox();
                Listcell cell1 = new Listcell();
                cell1.setParent(item);
                Checkbox cb = new Checkbox();
                cb.setParent(cell1);
                cb.addEventListener(Events.ON_CHECK, (e) -> {
                    boolean selected = ((Checkbox) e.getTarget()).isChecked();
                    data.setSelected(selected);
                    if (selected) {
                        selectedFilters.add(data);
                        combobox.setDisabled(false);
                        negateCb.setDisabled(false);
                    } else {
                        selectedFilters.remove(data);
                        combobox.setDisabled(true);
                        negateCb.setDisabled(true);
                    }
                });
                cb.setChecked(data.getSelected());

                Listcell cell2 = new Listcell();
                cell2.setParent(item);
                Div labelWrapper = new Div();
                labelWrapper.setParent(cell2);
                Popup tooltip = new Popup();
                Label labelForTooltip = new Label(data.getFilterName());
                labelForTooltip.setParent(tooltip);
                Label label = new Label(as.translate(viewModelId, data.getFilterName()));
                label.setParent(labelWrapper);
                label.setTooltip(tooltip);
                tooltip.setParent(labelWrapper);

                Listcell cell3 = new Listcell();
                cell3.setParent(item);
                combobox.addEventListener(Events.ON_SELECT, (e) -> {
                    data.setCurrentSelection(((Combobox) e.getTarget()).getValue());
                });
                combobox.setVflex("1");
                combobox.setDisabled(!data.getSelected());
                combobox.setModel(new SimpleListModelExt<String>(data.getFilterTypes()));
                combobox.setValue(data.getCurrentSelection());
                combobox.setParent(cell3);

                Listcell cell4 = new Listcell();
                cell4.setParent(item);
                negateCb.setParent(cell4);
                negateCb.setChecked(data.getNegate() == null ? false : data.getNegate());
                negateCb.addEventListener(Events.ON_CHECK, (e) -> {
                    data.setNegate(((Checkbox) e.getTarget()).isChecked());
                });
                negateCb.setDisabled(!data.getSelected());
            }
        });

    }

    private void createHeader(Listhead head) {
        createListHeader(head, null, "40px");
        createListHeader(head, as.translate("editSearchFilters", "title"));
        createListHeader(head, as.translate("editSearchFilters", "filterType"));
        createListHeader(head, as.translate("editSearchFilters", "filterNegate", "40px"));
    }

    private void createListHeader(Listhead parent, String label) {
        createListHeader(parent, label, null);
    }

    private void createListHeader(Listhead parent, String label, String size) {
        Listheader header = new Listheader(label);
        header.setParent(parent);
        if (size != null)
            header.setWidth(size);
    }

    /**
     * Method to get the filter types that based on the metadata of the field.
     *
     * @param column
     * @return
     */
    protected List<String> getAvailableFilterType(UIColumnConfiguration column) {
        List<String> allowedTypes = new ArrayList<>(UIFilterType.values().length);

        // if the field is marked as "dropdown" or "bandbox", or is an object (=
        // implicit bandbox), or the field is "tenantId":
        if (isEqualityDataTypes(column)) {
            allowedTypes.add(UIFilterType.EQUALITY.name());
            return allowedTypes;
        }

        // LIKE is only allowed for multi dropdown
        if (hasProperty(column, Constants.UiFieldProperties.MULTI_DROPDOWN)) {
            allowedTypes.add(UIFilterType.LIKE.name());
            return allowedTypes;
        }

        for (UIFilterType type : UIFilterType.values()) {
            // all other types except String: "LIKE" filter is not allowed
            if (type.equals(UIFilterType.LIKE) && !isStringType(column)) {
                continue;
            }

            allowedTypes.add(type.name());
        }

        return allowedTypes;
    }

    /**
     * Define a list of properties to be excluded in the search filter configuration.
     */
    protected boolean hasExcludedProperties(UIColumnConfiguration column) {
        return hasProperty(column, Constants.UiFieldProperties.NO_JAVA)
                || hasProperty(column, Constants.UiFieldProperties.NO_DDL)
                || hasProperty(column, Constants.UiFieldProperties.NO_AUTO_MAP);
    }

    /**
     * Get parent of the given path delimited by '.'
     *
     * @param fullPath
     * @return parent path
     */
    protected String getParentPath(String fullPath) {
        int pos = fullPath.lastIndexOf(".");
        if (pos == -1) {
            return "";
        } else {
            return fullPath.substring(0, pos);
        }
    }

    /**
     * Check if the field has attributes that has to
     *
     * @param column
     * @return
     */
    protected boolean isEqualityDataTypes(UIColumnConfiguration column) {
        final String dataType = column.getMeta().getDataType();

        return dataType.equals("boolean") || dataType.equals("enum") || dataType.equals("xenum") || dataType.equals("uuid")
                || hasProperty(column, Constants.UiFieldProperties.DROPDOWN) || hasProperty(column, Constants.UiFieldProperties.BANDBOX)
                || column.getFieldName().equals("tenantId");
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
     * Check if the data type of the column is considered as string type, for
     * example: ascii, unicode, uppercase, lowercase, etc
     *
     * @param column
     * @return
     */
    private boolean isStringType(UIColumnConfiguration column) {
        final String dataType = column.getMeta().getDataType();

        return dataType.equals("ascii") || dataType.equals("unicode") || dataType.equals("uppercase")
                || dataType.equals("lowercase");
    }

    /**
     * Method to check if the given field is within the given level range from the
     * MainDTO
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

    /**
     * Check if the field has any attribute like dropdown or bandbox
     *
     * @param column
     * @return
     */
    private boolean isDropdownOrBandbox(UIColumnConfiguration column) {
        return hasProperty(column, Constants.UiFieldProperties.DROPDOWN) || hasProperty(column, Constants.UiFieldProperties.BANDBOX);
    }

    @Override
    public List<SearchFilterRowVM> getSelectedFilters() {
        return selectedFilters;
    }
}
