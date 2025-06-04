/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.ee.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.zkui.components.dropdown28.SimpleListModelExt;
import com.arvatosystems.t9t.zkui.services.impl.DefaultSearchFilterConfigCreator;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.viewmodel.support.SearchFilterRowVM;

import de.jpaw.bonaparte.pojos.ui.UIColumnConfiguration;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.util.FreezeTools;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Specializes;

@Dependent
@Specializes
public class SearchFilterConfigCreatorEE extends DefaultSearchFilterConfigCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchFilterConfigCreatorEE.class);

    private final Map<String, List<SearchFilterUiModel>> columnsByKey = new HashMap<>();

    @Override
    public void createComponent(final Div parent, final UIGridPreferences uiGridPreferences, final List<UIFilter> selectedUiFilters) {
        final List<UIColumnConfiguration> uiColumns = uiGridPreferences.getColumns();
        final List<SearchFilterUiModel> firstLevelColumns = new ArrayList<>();
        rows = new ArrayList<>(uiColumns.size());
        selectedFilters = new ArrayList<>(selectedUiFilters.size() * 2);
        viewModelId = uiGridPreferences.getViewModel();
        initUiFilters(uiColumns, selectedUiFilters, firstLevelColumns);
        final Grid grid = new Grid();
        parent.setVflex("1");
        grid.setVflex("1");
        grid.setParent(parent);
        grid.setSclass("nestedGrid grid no-padding");
        createGrid(grid, firstLevelColumns);
    }

    public void initUiFilters(final List<UIColumnConfiguration> uiColumns, final List<UIFilter> selectedUiFilters,
            final List<SearchFilterUiModel> firstLevelColumns) {
        final Map<String, SearchFilterRowVM> activeUIFilterMap = new HashMap<>(FreezeTools.getInitialHashMapCapacity(uiColumns.size()));

        for (final UIFilter uiFilter : selectedUiFilters) {
            final SearchFilterRowVM row = new SearchFilterRowVM(uiFilter);
            activeUIFilterMap.put(uiFilter.getFieldName(), row);
            selectedFilters.add(row);
        }

        List<String> excludedUiColumns = new ArrayList<>();
        for (final UIColumnConfiguration uiColumn : uiColumns) {

            final String fullPath = uiColumn.getFieldName();
            if (hasExcludedProperties(uiColumn) || excludedUiColumns.contains(getParentPath(fullPath))) {
                excludedUiColumns.add(fullPath);
                continue;
            }

            if (fullPath.indexOf(".") != -1) {
                final String[] splitted = fullPath.split("\\.");
                String fieldName = "";

                for (int i = 0; i < splitted.length; i++) {

                    if (i == 0) {
                        fieldName = splitted[i];
                        final SearchFilterUiModel searchFilterModel = new SearchFilterUiModel();
                        searchFilterModel.setFieldName(fieldName);
                        if (!firstLevelColumns.contains(searchFilterModel)) {
                            firstLevelColumns.add(searchFilterModel);
                        }
                    } else {
                        final String upperLevel = fieldName;
                        fieldName += "." + splitted[i];
                        final SearchFilterUiModel fnm = new SearchFilterUiModel();
                        fnm.setFieldName(fieldName);
                        if (i == splitted.length - 1) {
                            fnm.setFullPath(fullPath);
                            final SearchFilterRowVM selectedSearchFilter = activeUIFilterMap.get(fullPath);
                            if (selectedSearchFilter == null) {
                                final SearchFilterRowVM row = new SearchFilterRowVM(uiColumn.getFieldName());
                                addIfQualifierExists(row, uiColumn);
                                fnm.setSearchFilter(row);
                            } else {
                                fnm.setSearchFilter(selectedSearchFilter);
                            }
                            fnm.getSearchFilter().setFilterTypes(getAvailableFilterType(uiColumn));
                        }
                        final List<SearchFilterUiModel> fnms = columnsByKey.computeIfAbsent(upperLevel,
                                (k) -> new ArrayList<>());
                        if (!fnms.contains(fnm)) {
                            fnms.add(fnm);
                        }
                    }
                }
            } else {
                final SearchFilterUiModel fnm = new SearchFilterUiModel();
                fnm.setFieldName(fullPath);
                fnm.setFullPath(fullPath);
                final SearchFilterRowVM selectedSearchFilter = activeUIFilterMap.get(fullPath);
                if (selectedSearchFilter == null) {
                    final SearchFilterRowVM row = new SearchFilterRowVM(uiColumn.getFieldName());
                    addIfQualifierExists(row, uiColumn);
                    fnm.setSearchFilter(row);
                } else {
                    fnm.setSearchFilter(selectedSearchFilter);
                }
                fnm.getSearchFilter().setFilterTypes(getAvailableFilterType(uiColumn));
                if (!firstLevelColumns.contains(fnm)) {
                    firstLevelColumns.add(fnm);
                }
            }
        }
    }

    private void addIfQualifierExists(final SearchFilterRowVM row, final UIColumnConfiguration uiColumn) {
        if (uiColumn.getMeta() != null && uiColumn.getMeta().getFieldProperties() != null) {
            String filterQualifier = uiColumn.getMeta().getFieldProperties().get(Constants.UiFieldProperties.FILTER_QUALIFIER);
            row.setQualifier(filterQualifier);
        }
    }

    private void createGrid(final Grid grid, final List<SearchFilterUiModel> data) {
        final Columns columns = new Columns();
        final Column detail = new Column();
        detail.setWidth("40px");
        columns.appendChild(detail);
        columns.appendChild(new Column(as.translate("editSearchFilters", "title")));
        columns.appendChild(new Column(as.translate("editSearchFilters", "filterType")));
        columns.appendChild(new Column(as.translate("editSearchFilters", "filterNegate")));

        grid.appendChild(columns);
        grid.setModel(new ListModelList<>(data));
        grid.setVflex("1");
        grid.setRowRenderer(new RowRenderer<SearchFilterUiModel>() {
            @Override
            public void render(final Row row, final SearchFilterUiModel data, final int index) throws Exception {
                final Combobox combobox = new Combobox();
                final Checkbox negateCb = new Checkbox();
                final String translatedLabel = as.translate(viewModelId, data.getFieldName());
                final SearchFilterRowVM searchFilter = data.searchFilter;
                if (columnsByKey.containsKey(data.getFieldName())) {
                    final Detail detail = new Detail();
                    detail.setAttribute("data", data);
                    detail.addEventListener(Events.ON_OPEN, (e) -> {
                        final SearchFilterUiModel searchFilterModel = (SearchFilterUiModel) e.getTarget()
                                .getAttribute("data");
                        e.getTarget().getChildren().clear();
                        final Grid grid = new Grid();
                        grid.setParent(e.getTarget());
                        createGrid(grid, columnsByKey.get(searchFilterModel.getFieldName()));
                        LOGGER.debug("EVENT: {} {}", searchFilterModel.getFieldName(), e.getTarget());
                    });
                    detail.setParent(row);
                } else {
                    final Checkbox cb = new Checkbox();
                    cb.setChecked(searchFilter.getSelected());
                    cb.addEventListener(Events.ON_CHECK, (e) -> {
                        final boolean selected = ((Checkbox) e.getTarget()).isChecked();
                        searchFilter.setSelected(selected);
                        if (selected) {
                            selectedFilters.add(searchFilter);
                            combobox.setDisabled(false);
                            negateCb.setDisabled(false);
                        } else {
                            selectedFilters.remove(searchFilter);
                            combobox.setDisabled(true);
                            negateCb.setDisabled(true);
                        }
                    });
                    cb.setParent(row);
                }

                // translate the label
                final Div labelWrapper = new Div();
                labelWrapper.setParent(row);
                final Label label = new Label(translatedLabel);
                label.setParent(labelWrapper);
                final Popup tooltip = new Popup();
                tooltip.setParent(labelWrapper);
                label.setTooltip(tooltip);

                final Label tooltipLabel = new Label(data.fieldName);
                tooltipLabel.setParent(tooltip);

                combobox.addEventListener(Events.ON_SELECT, (e) -> {
                    searchFilter.setCurrentSelection(((Combobox) e.getTarget()).getValue());
                });
                combobox.setVflex("1");
                combobox.setDisabled(!searchFilter.getSelected());
                combobox.setModel(new SimpleListModelExt<>(searchFilter.getFilterTypes()));
                combobox.setValue(searchFilter.getCurrentSelection());
                combobox.setParent(row);

                negateCb.setChecked(searchFilter.getNegate() == null ? false : searchFilter.getNegate());
                negateCb.addEventListener(Events.ON_CHECK, (e) -> {
                    searchFilter.setNegate(((Checkbox) e.getTarget()).isChecked());
                });
                negateCb.setDisabled(!searchFilter.getSelected());
                negateCb.setParent(row);
            }
        });
    }

    public class SearchFilterUiModel {
        private String fieldName;
        private String fullPath;
        private SearchFilterRowVM searchFilter;

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(final String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFullPath() {
            return fullPath;
        }

        public void setFullPath(final String fullPath) {
            this.fullPath = fullPath;
        }

        public SearchFilterRowVM getSearchFilter() {
            return searchFilter;
        }

        public void setSearchFilter(final SearchFilterRowVM searchFilter) {
            this.searchFilter = searchFilter;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final SearchFilterUiModel other = (SearchFilterUiModel) obj;
            if (fieldName == null) {
                if (other.fieldName != null)
                    return false;
            } else if (!fieldName.equals(other.fieldName))
                return false;
            return true;
        }
    }
}
