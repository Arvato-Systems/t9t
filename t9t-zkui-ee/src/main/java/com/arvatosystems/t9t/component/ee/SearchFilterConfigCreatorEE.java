package com.arvatosystems.t9t.component.ee;

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
import com.arvatosystems.t9t.components.tools.DefaultSearchFilterConfigCreator;
import com.arvatosystems.t9t.components.tools.SearchFilterRowVM;
import com.arvatosystems.t9t.tfi.component.SimpleListModelExt;

import de.jpaw.bonaparte.pojos.ui.UIColumnConfiguration;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Specializes;

@Dependent
@Specializes
public class SearchFilterConfigCreatorEE extends DefaultSearchFilterConfigCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchFilterConfigCreatorEE.class);

    private final Map<String, List<SearchFilterUiModel>> columnsByKey = new HashMap<>();

    @Override
    public void createComponent(Div parent, UIGridPreferences uiGridPreferences, List<UIFilter> selectedUiFilters) {
        List<UIColumnConfiguration> uiColumns = uiGridPreferences.getColumns();
        List<SearchFilterUiModel> firstLevelColumns = new ArrayList<>();
        rows = new ArrayList<>(uiColumns.size());
        selectedFilters = new ArrayList<>(selectedUiFilters.size() * 2);
        viewModelId = uiGridPreferences.getViewModel();
        initUiFilters(uiColumns, selectedUiFilters, firstLevelColumns);
        Grid grid = new Grid();
        parent.setVflex("1");
        grid.setVflex("1");
        grid.setParent(parent);
        grid.setSclass("nestedGrid grid no-padding no-scrollbar");
        createGrid(grid, firstLevelColumns);
    }

    public void initUiFilters(List<UIColumnConfiguration> uiColumns, List<UIFilter> selectedUiFilters,
            List<SearchFilterUiModel> firstLevelColumns) {
        Map<String, SearchFilterRowVM> activeUIFilterMap = new HashMap<>(uiColumns.size());

        for (UIFilter uiFilter : selectedUiFilters) {
            SearchFilterRowVM row = new SearchFilterRowVM(uiFilter);
            activeUIFilterMap.put(uiFilter.getFieldName(), row);
            selectedFilters.add(row);
        }

        for (UIColumnConfiguration uiColumn : uiColumns) {
            String fullPath = uiColumn.getFieldName();
            if (fullPath.indexOf(".") != -1) {
                String[] splitted = fullPath.split("\\.");
                String fieldName = "";

                for (int i = 0; i < splitted.length; i++) {

                    if (i == 0) {
                        fieldName = splitted[i];
                        SearchFilterUiModel searchFilterModel = new SearchFilterUiModel();
                        searchFilterModel.setFieldName(fieldName);
                        if (!firstLevelColumns.contains(searchFilterModel)) {
                            firstLevelColumns.add(searchFilterModel);
                        }
                    } else {
                        String upperLevel = fieldName;
                        fieldName += "." + splitted[i];
                        SearchFilterUiModel fnm = new SearchFilterUiModel();
                        fnm.setFieldName(fieldName);
                        if (i == splitted.length - 1) {
                            fnm.setFullPath(fullPath);
                            SearchFilterRowVM selectedSearchFilter = activeUIFilterMap.get(fullPath);
                            if (selectedSearchFilter == null) {
                                fnm.setSearchFilter(new SearchFilterRowVM(uiColumn.getFieldName()));
                            } else {
                                fnm.setSearchFilter(selectedSearchFilter);
                            }
                            fnm.getSearchFilter().setFilterTypes(getAvailableFilterType(uiColumn));
                        }
                        List<SearchFilterUiModel> fnms = columnsByKey.computeIfAbsent(upperLevel,
                                (k) -> new ArrayList<>());
                        if (!fnms.contains(fnm)) {
                            fnms.add(fnm);
                        }
                    }
                }
            } else {
                SearchFilterUiModel fnm = new SearchFilterUiModel();
                fnm.setFieldName(fullPath);
                fnm.setFullPath(fullPath);
                SearchFilterRowVM selectedSearchFilter = activeUIFilterMap.get(fullPath);
                if (selectedSearchFilter == null) {
                    fnm.setSearchFilter(new SearchFilterRowVM(uiColumn.getFieldName()));
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

    private void createGrid(Grid grid, List<SearchFilterUiModel> data) {
        Columns columns = new Columns();
        Column detail = new Column();
        detail.setWidth("40px");
        columns.appendChild(detail);
        columns.appendChild(new Column(as.translate("editSearchFilters", "title")));
        columns.appendChild(new Column(as.translate("editSearchFilters", "filterType")));
        columns.appendChild(new Column(as.translate("editSearchFilters", "filterNegate")));

        grid.appendChild(columns);
        grid.setModel(new ListModelList<SearchFilterUiModel>(data));
        grid.setVflex("1");
        grid.setRowRenderer(new RowRenderer<SearchFilterUiModel>() {
            @Override
            public void render(Row row, SearchFilterUiModel data, int index) throws Exception {
                Combobox combobox = new Combobox();
                Checkbox negateCb = new Checkbox();
                String translatedLabel = as.translate(viewModelId, data.getFieldName());
                SearchFilterRowVM searchFilter = data.searchFilter;
                if (columnsByKey.containsKey(data.getFieldName())) {
                    Detail detail = new Detail();
                    detail.setAttribute("data", data);
                    detail.addEventListener(Events.ON_OPEN, (e) -> {
                        SearchFilterUiModel searchFilterModel = (SearchFilterUiModel) e.getTarget()
                                .getAttribute("data");
                        e.getTarget().getChildren().clear();
                        Grid grid = new Grid();
                        grid.setParent(e.getTarget());
                        createGrid(grid, columnsByKey.get(searchFilterModel.getFieldName()));
                        LOGGER.debug("EVENT: {} {}", searchFilterModel.getFieldName(), e.getTarget());
                    });
                    detail.setParent(row);
                } else {
                    Checkbox cb = new Checkbox();
                    cb.setChecked(searchFilter.getSelected());
                    cb.addEventListener(Events.ON_CHECK, (e) -> {
                        boolean selected = ((Checkbox) e.getTarget()).isChecked();
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
                Div labelWrapper = new Div();
                labelWrapper.setParent(row);
                Label label = new Label(translatedLabel);
                label.setParent(labelWrapper);
                Popup tooltip = new Popup();
                tooltip.setParent(labelWrapper);
                label.setTooltip(tooltip);

                Label tooltipLabel = new Label(data.fieldName);
                tooltipLabel.setParent(tooltip);

                combobox.addEventListener(Events.ON_SELECT, (e) -> {
                    searchFilter.setCurrentSelection(((Combobox) e.getTarget()).getValue());
                });
                combobox.setVflex("1");
                combobox.setDisabled(!searchFilter.getSelected());
                combobox.setModel(new SimpleListModelExt<String>(searchFilter.getFilterTypes()));
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

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFullPath() {
            return fullPath;
        }

        public void setFullPath(String fullPath) {
            this.fullPath = fullPath;
        }

        public SearchFilterRowVM getSearchFilter() {
            return searchFilter;
        }

        public void setSearchFilter(SearchFilterRowVM searchFilter) {
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
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SearchFilterUiModel other = (SearchFilterUiModel) obj;
            if (fieldName == null) {
                if (other.fieldName != null)
                    return false;
            } else if (!fieldName.equals(other.fieldName))
                return false;
            return true;
        }
    }
}
