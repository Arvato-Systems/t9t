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
package com.arvatosystems.t9t.init;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.BooleanUtil;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.base.IGridConfigContainer;
import com.arvatosystems.t9t.base.ILeanGridConfigContainer;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.entities.InternalTenantRef42;
import com.arvatosystems.t9t.base.types.TenantIsolationCategoryType;
import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import de.jpaw.bonaparte.api.ColumnCollector;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.pojos.ui.UIColumn;
import de.jpaw.bonaparte.pojos.ui.UIColumnConfiguration;
import de.jpaw.bonaparte.pojos.ui.UIDefaults;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIMeta;
import de.jpaw.json.JsonParser;
import de.jpaw.util.ExceptionUtil;

public class UiGridConfigPrefs {
    private static final Logger LOGGER = LoggerFactory.getLogger(UiGridConfigPrefs.class);
    private static final AtomicInteger errorCounter = new AtomicInteger(0);
    public static final UIDefaults MY_DEFAULTS = new UIDefaults(
        50,     // renderMaxArrayColumns: we have up to 7 address lines (and 6 tax levels) (but 50 in other applications)
        160,    // widthObject
        40,     // widthCheckbox
        160,    // widthEnum
        280,    // widthEnumset
        16,     // widthOffset
        8,      // widthPerCharacter
        400     // widthMax
    );  // default is (5, 32, 24, 80, 120, 10, 12, 200);

    private static void addUiMeta(CrudViewModel<?,?> vm, String viewModelId, UIGridPreferences ui, String gridId) {
        if (IViewModelContainer.VIEW_MODEL_BY_GRID_ID_REGISTRY.putIfAbsent(gridId, viewModelId) != null)
            LOGGER.error("view model by grid config {} defined multiple times", gridId);
        ColumnCollector cc = new ColumnCollector(MY_DEFAULTS);
        List<UIColumnConfiguration> cols = ui.getColumns();
        for (UIColumnConfiguration col : cols) {
            try {
                if (FieldMappers.isTenantRef(col.getFieldName()))
                    cc.createUIMeta(col, InternalTenantRef42.class$MetaData());
                else if (FieldMappers.isTrackingColumn(col.getFieldName()))
                    cc.createUIMeta(col, FullTrackingWithVersion.class$MetaData());
                else
                    cc.createUIMeta(col, vm.dtoClass.getMetaData());
                // workaround for ZK UI: the UI changes all empty fields to nulls, we have to avoid empty strings in the interaction with the UI for that reason
                UIMeta m = col.getMeta();
                if (m != null) {
                    m.setClassProperties(empty2minus(m.getClassProperties()));
                    m.setFieldProperties(empty2minus(m.getFieldProperties()));
                }
            } catch (Exception e1) {
                errorCounter.incrementAndGet();
                LOGGER.error("Cannot obtain meta data for grid config {}, field {}, maybe used a wrong prefix? [{}: {}]",
                        gridId, col.getFieldName(), e1.getClass().getSimpleName(), e1.getMessage());
            }
        }
        ui.validate();
        ui.freeze();
        if (IGridConfigContainer.GRID_CONFIG_REGISTRY.putIfAbsent(gridId, ui) != null) {
            errorCounter.incrementAndGet();
            LOGGER.error("grid config {} defined multiple times", gridId);
        }
    }

    public static void getGridConfigAsObject(String resourceId) {
        String gridId = resourceId.replace('$', '/');
        try {
            URL url = Resources.getResource("gridconfig/" + resourceId + ".json");
            String json = Resources.toString(url, Charsets.UTF_8);

            Map<String,Object> config = new JsonParser(json, false).parseObject();

            // add the "allowSorting" data, by default false
            List<Map<String,Object>> columns = (List<Map<String, Object>>) config.get("columns");
            for (Map<String,Object> column: columns) {
                column.put("allowSorting", true);
                column.put("negateFilter", false);
            }
            config.put("sortDescending", false);
            config.put("@PQON", "t9t.base.uiprefs.UIGridPreferences");      // avoid a warning
            UIGridPreferences ui = (UIGridPreferences) MapParser.asBonaPortable(config, UIGridPreferences.meta$$this);
            // enrich the UI meta data of the columns, if a viewModel is referenced
            String viewModelId = ui.getViewModel();
            if (viewModelId == null) {
                errorCounter.incrementAndGet();
                LOGGER.error("No view model reference defined for grid config {} - screens won't work", gridId);
                return;
            }
            CrudViewModel<?,?> vm = IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.get(viewModelId);
            if (vm == null) {
                errorCounter.incrementAndGet();
                LOGGER.error("No view model definition found for {}, as specified in grid config {}", viewModelId, gridId);
            } else {
                LOGGER.debug("Grid config {} maps to view model {} ({})", gridId, viewModelId, vm.dtoClass.getPqon());

                addUiMeta(vm, viewModelId, ui, gridId);
                // set root level class properties - remove "" entry!
                // ui.setClassProperties(vm.dtoClass.getMetaData().getProperties());
            }
        } catch (Exception e) {
            errorCounter.incrementAndGet();
            LOGGER.error("Parsing error for grid config {}: {}", gridId, ExceptionUtil.causeChain(e));
        }
    }

    static Map<String,String> empty2minus(Map<String, String> map) {
        if (map == null || map.isEmpty())
            return map;
        // the input is an ImmutableMap, we have to copy it to support changes to the (expected) empty components
        Map<String,String> r = new HashMap<String, String>(2 * map.size());
        for (Map.Entry<String, String> e : map.entrySet()) {
            String v = e.getValue();
            r.put(e.getKey(), v == null || v.length() == 0 ? T9tConstants.UI_META_NO_ASSIGNED_VALUE : v);
        }
        return r;
    }

    public static void getLeanGridConfigAsObject(String resourceId) {
        String gridId = resourceId.replace('$', '/');
        try {
            URL url = Resources.getResource("gridconfig/" + gridId + ".json");
            String json = Resources.toString(url, Charsets.UTF_8);

            Map<String,Object> config = new JsonParser(json, false).parseObject();
            UILeanGridPreferences prefs = new UILeanGridPreferences();
            MapParser.populateFrom(prefs, config);
            if (ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.putIfAbsent(gridId, prefs) != null) {
                errorCounter.incrementAndGet();
                LOGGER.error("lean grid config {} defined multiple times", gridId);
            }

            // obtain the viewModel
            String viewModelId = prefs.getViewModel();
            CrudViewModel<?, ?> vm = IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.get(viewModelId);
            if (vm == null) {
                errorCounter.incrementAndGet();
                LOGGER.error("No view model definition found for {}, as specified in grid config {}", viewModelId, gridId);
                return;
            }
            LOGGER.debug("Lean grid config {} maps to view model {} ({})", gridId, viewModelId, vm.dtoClass.getPqon());

            // also convert it to old style...
            // must determine available columns...
            ColumnCollector cc = new ColumnCollector(MY_DEFAULTS, true);

            if (vm.trackingClass != null)
                cc.addToColumns(vm.trackingClass.getMetaData());

            String tenantCategory = vm.dtoClass.getProperty("tenantCategory");
            if (!TenantIsolationCategoryType.GLOBAL.getToken().equals(tenantCategory))
                cc.addToColumns(InternalTenantRef42.class$MetaData());

            cc.addToColumns(vm.dtoClass.getMetaData());

            // convert into new format, build a fast access map
            UIGridPreferences ui = new UIGridPreferences();
            Map<String, UIColumnConfiguration> colMap = new HashMap<String, UIColumnConfiguration>(2 * cc.columns.size());
            List<UIColumnConfiguration> cols = new ArrayList<UIColumnConfiguration>(cc.columns.size());
            for (UIColumn c : cc.columns) {
                UIColumnConfiguration d = convertColumn(c);
                colMap.put(c.getFieldName(), d);
                cols.add(d);
                if (c.getFieldName().indexOf('[') >= 0) {
                    // also index stripped field
                    colMap.put(FieldMappers.stripIndexes(c.getFieldName()), d);
                }
            }
            ui.setColumns(cols);
            ui.setDynamicWidths(false);
            ui.setSortColumn(prefs.getSortColumn());
            ui.setSortDescending(BooleanUtil.isTrue(prefs.getSortDescending()));
            ui.setViewModel(viewModelId);
            ui.setWasLean(Boolean.TRUE);
            ui.setIsSolrSearch(vm.dtoClass.getProperty("isSolr") != null);

            // now set any filters
            if (prefs.getFilters() != null) {
                for (UIFilter f : prefs.getFilters()) {
                    String s = FieldMappers.stripIndexes(f.getFieldName());
                    UIColumnConfiguration d = colMap.get(s);
                    if (d == null) {
                        errorCounter.incrementAndGet();
                        LOGGER.error("cannot find field {} ({}) referenced as a filter by grid ID {}", f.getFieldName(), s, gridId);
                    } else {
                        d.setFilterType(f.getFilterType());
                        d.setFilterQualifier(f.getQualifier());
                    }
                }
            }

            // now set column visibility
            for (String fld : prefs.getFields()) {
                String s = FieldMappers.stripIndexes(fld);
                UIColumnConfiguration d = colMap.get(s);
                if (d == null) {
                    errorCounter.incrementAndGet();
                    LOGGER.error("cannot find field {} ({}) referenced as visible by grid ID {}", fld, s, gridId);
                } else {
                    d.setVisible(true);
                }
            }

            // now set fields which are not sortable (in the backend)
            if (prefs.getUnsortableFields() != null) {
                for (String fld : prefs.getUnsortableFields()) {
                    String s = FieldMappers.stripIndexes(fld);
                    UIColumnConfiguration d = colMap.get(s);
                    if (d == null) {
                        errorCounter.incrementAndGet();
                        LOGGER.error("cannot find field {} ({}) referenced as unsortable by grid ID {}", fld, s, gridId);
                    } else {
                        d.setAllowSorting(false);
                    }
                }
            }

            // enrich UI meta, validate and store it
            addUiMeta(vm, viewModelId, ui, gridId);
        } catch (Exception e) {
            errorCounter.incrementAndGet();
            LOGGER.error("Parsing error for lean grid config {}: {}", gridId, ExceptionUtil.causeChain(e));
        }
    }

    private static UIColumnConfiguration convertColumn(UIColumn src) {
        UIColumnConfiguration dst = new UIColumnConfiguration();
        dst.setAllowSorting(true);
        dst.setNegateFilter(false);
        dst.setVisible(false);
        dst.setFieldName(src.getFieldName());
        dst.setAlignment(src.getAlignment());
        dst.setLayoutHint(src.getLayoutHint());
        dst.setWidth(src.getWidth());
        return dst;
    }

    public static int getErrorCount() {
        return errorCounter.get();
    }

    // only visible from InitContainers
    static void reset() {
        errorCounter.set(0);
        IViewModelContainer.VIEW_MODEL_BY_GRID_ID_REGISTRY.clear();
        IGridConfigContainer.GRID_CONFIG_REGISTRY.clear();
        ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.clear();
    }
}
