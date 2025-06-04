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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import com.arvatosystems.t9t.base.IGridConfigContainer;
import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.arvatosystems.t9t.zkui.components.grid.ILeanGridConfigResolver;
import com.arvatosystems.t9t.zkui.components.grid.LeanGridConfigResolver;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;
import de.jpaw.bonaparte.pojos.api.AggregateColumn;
import de.jpaw.bonaparte.pojos.api.AggregateFunctionType;
import de.jpaw.bonaparte.pojos.meta.DataCategory;
import de.jpaw.bonaparte.pojos.ui.UIColumnConfiguration;
import de.jpaw.bonaparte.pojos.ui.UIMeta;
import de.jpaw.bonaparte.util.FreezeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnAggregationsViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColumnAggregationsViewModel.class);

    private static final List<String> ALLOWED_DATA_CATEGORIES = List.of(DataCategory.NUMERIC.name(), DataCategory.BASICNUMERIC.name(),
      DataCategory.MISC.name(), DataCategory.ENUM.name(), DataCategory.XENUM.name(), DataCategory.ENUMALPHA.name(), DataCategory.STRING.name(),
      DataCategory.TEMPORAL.name());

    private Window windowComponent = null;
    private final ApplicationSession session = ApplicationSession.get();
    private ILeanGridConfigResolver gridConfigResolver;
    private UIGridPreferences uiGridPreferences = null;
    private UILeanGridPreferences gridPrefs;
    private List<ColumnAggregationRowVM> columnAggregationsList;
    private String viewModelId;
    private boolean groupByEmpty = false;

    @Init(superclass = true)
    public void init(final @BindingParam("initParams") HashMap<String, Object> initParams, final @ContextParam(ContextType.COMPONENT) Component component) {
        windowComponent = (Window) component.getRoot();

        if (initParams != null && initParams.get("gridId") != null) {
            uiGridPreferences = IGridConfigContainer.GRID_CONFIG_REGISTRY.get(initParams.get("gridId"));
            if (initParams.get("gridConfigResolver") != null) {
                gridConfigResolver = (ILeanGridConfigResolver) initParams.get("gridConfigResolver");
            } else {
                gridConfigResolver = new LeanGridConfigResolver(initParams.get("gridId").toString(), session);
            }
        }
        gridPrefs = gridConfigResolver.getGridPreferences();
        viewModelId = uiGridPreferences.getViewModel();
        columnAggregationsList = getConfiguredAggregations();
    }

    @Command
    @NotifyChange({ "groupByEmpty"})
    public void saveConfig() {
        final List<AggregateColumn> aggregateColumns = new ArrayList<>(columnAggregationsList.size());
        final List<String> groupByColumns = new ArrayList<>(columnAggregationsList.size());
        for (ColumnAggregationRowVM row : columnAggregationsList) {
            if (row.isGroupBy()) {
                groupByColumns.add(row.getFieldName());
            } else if (row.getFunction() != null) {
                final AggregateColumn aggregateColumn = new AggregateColumn();
                aggregateColumn.setFieldName(row.getFieldName());
                aggregateColumn.setFunction(row.getFunction());
                aggregateColumns.add(aggregateColumn);
            }
        }
        if (groupByColumns.isEmpty() && !aggregateColumns.isEmpty()) {
            groupByEmpty = true;
            return;
        }
        groupByEmpty = false;
        gridConfigResolver.setAggregations(aggregateColumns, groupByColumns);
        Events.sendEvent("onClose", windowComponent, true);
        closeWindow();
    }

    @Command
    @NotifyChange({ "groupByEmpty", "columnAggregationsList"})
    public void resetConfig() {
        for (ColumnAggregationRowVM row : columnAggregationsList) {
            row.setFunction(null);
            row.setGroupBy(false);
        }
        groupByEmpty = false; // reset groupByEmpty flag
    }

    @Command
    public void closeWindow() {
        Events.sendEvent("onClose", windowComponent, null);
        windowComponent.onClose();
    }

    private List<ColumnAggregationRowVM> getConfiguredAggregations() {

        final Map<String, ColumnAggregationRowVM> aggregationConfigMap = new HashMap<>(FreezeTools.getInitialHashMapCapacity(uiGridPreferences.getColumns().size()));

        for (UIColumnConfiguration columnConfig : uiGridPreferences.getColumns()) {
            if (allowedColumn(columnConfig)) {
                final ColumnAggregationRowVM row = new ColumnAggregationRowVM(columnConfig.getFieldName(), getAllowedFunctions(columnConfig));
                aggregationConfigMap.put(columnConfig.getFieldName(), row);
            }
        }

        // populate configured group by columns
        if (gridPrefs.getGroupByColumns() != null) {
            for (String groupByColumn : gridPrefs.getGroupByColumns()) {
                final ColumnAggregationRowVM row = aggregationConfigMap.get(groupByColumn);
                if (row == null) {
                    LOGGER.warn("Group by column {} not found in UI column configuration", groupByColumn);
                    continue;
                }
                row.setGroupBy(true);
                row.setFunction(null);
            }
        }

        // populate configured aggregate columns
        if (gridPrefs.getAggregateColumns() != null) {
            for (AggregateColumn aggregateColumn : gridPrefs.getAggregateColumns()) {
                final ColumnAggregationRowVM row = aggregationConfigMap.get(aggregateColumn.getFieldName());
                if (row == null) {
                    LOGGER.warn("Aggregate column {} not found in UI column configuration", aggregateColumn.getFieldName());
                    continue;
                }
                row.setGroupBy(false);
                row.setFunction(aggregateColumn.getFunction());
            }
        }

        return new ArrayList<>(aggregationConfigMap.values());
    }

    private boolean allowedColumn(final UIColumnConfiguration column) {
        if (column.getMeta() == null || column.getFieldName().contains(".")) {
            return false;
        }
        final UIMeta meta = column.getMeta();
        if (!ALLOWED_DATA_CATEGORIES.contains(meta.getDataCategory())) {
            return false;
        }
        if (meta.getFieldProperties() != null
            && (meta.getFieldProperties().containsKey(Constants.UiFieldProperties.NO_JAVA)
            || meta.getFieldProperties().containsKey(Constants.UiFieldProperties.NO_DDL)
            || meta.getFieldProperties().containsKey(Constants.UiFieldProperties.NO_AUTO_MAP))) {
            return false;
        }
        return true;
    }

    private List<AggregateFunctionType> getAllowedFunctions(final UIColumnConfiguration column) {
        final List<AggregateFunctionType> allowedFunctions = new ArrayList<>(AggregateFunctionType.values().length);
        if (column.getMeta() != null) {
            final UIMeta meta = column.getMeta();
            final boolean isBoolean = meta.getDataType() != null && meta.getDataType().equalsIgnoreCase("boolean");
            if (!isBoolean) {
                allowedFunctions.add(AggregateFunctionType.MIN);
                allowedFunctions.add(AggregateFunctionType.MAX);
                final String dataCategory = column.getMeta().getDataCategory();
                final String dataType = column.getMeta().getDataType();
                if (dataCategory.equals(DataCategory.NUMERIC.name()) || dataCategory.equals(DataCategory.BASICNUMERIC.name())) {
                    allowedFunctions.add(AggregateFunctionType.SUM);
                }
                if (dataType.equals("long")) {
                    allowedFunctions.add(AggregateFunctionType.COUNT);
                    allowedFunctions.add(AggregateFunctionType.COUNT_DISTINCT);
                } else if (dataType.equals("double")) {
                    allowedFunctions.add(AggregateFunctionType.AVG);
                }
            }
        }
        return allowedFunctions;
    }

    public List<ColumnAggregationRowVM> getColumnAggregationsList() {
        return columnAggregationsList;
    }

    public String getViewModelId() {
        return viewModelId;
    }

    public boolean isGroupByEmpty() {
        return groupByEmpty;
    }

    public String translateFunction(AggregateFunctionType functionType) {
        return session.translateEnum(functionType);
    }
}
