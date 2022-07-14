/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.base.IGridConfigContainer;
import com.arvatosystems.t9t.base.ILeanGridConfigContainer;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
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

/**
 * Class which implements subroutines for initialization.
 * Default visibility (package) by intention, all accesses are done via <code>InitContainers</code>.
 */
final class UiGridConfigPrefs {
    private static final Logger LOGGER = LoggerFactory.getLogger(UiGridConfigPrefs.class);
    private static final AtomicInteger ERROR_COUNTER = new AtomicInteger(0);

    private static final Map<String, UIGridPreferences> GRID_OVERRIDES_MAP = new HashMap<>();
    private static final Map<String, UILeanGridPreferences> LEAN_GRID_OVERRIDES_MAP = new HashMap<>();
    private static final Map<String, UIGridPreferences> GRID_EXTENDS_MAP = new HashMap<>();
    private static final Map<String, UILeanGridPreferences> LEAN_GRID_EXTENDS_MAP = new HashMap<>();

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

    private UiGridConfigPrefs() { }

    private static void addUiMeta(final CrudViewModel<?, ?> vm, final String viewModelId, final UIGridPreferences ui, final String gridId) {
        final Map<String, UIGridPreferences> gridOverrides = GRID_OVERRIDES_MAP;
        final Map<String, UIGridPreferences> gridExtends = GRID_EXTENDS_MAP;

        final ColumnCollector cc = new ColumnCollector(MY_DEFAULTS);
        final List<UIColumnConfiguration> cols = ui.getColumns();
        for (final UIColumnConfiguration col : cols) {
            try {
                if (FieldMappers.isTenantRef(col.getFieldName()))
                    cc.createUIMeta(col, InternalTenantRef42.class$MetaData());
                else if (FieldMappers.isTrackingColumn(col.getFieldName()))
                    cc.createUIMeta(col, FullTrackingWithVersion.class$MetaData());
                else
                    cc.createUIMeta(col, vm.dtoClass.getMetaData());
                // workaround for ZK UI: the UI changes all empty fields to nulls, we have to avoid empty strings in the interaction with the UI for that reason
                final UIMeta m = col.getMeta();
                if (m != null) {
                    m.setClassProperties(empty2minus(m.getClassProperties()));
                    m.setFieldProperties(empty2minus(m.getFieldProperties()));
                }
            } catch (final Exception e1) {
                ERROR_COUNTER.incrementAndGet();
                LOGGER.error("Cannot obtain meta data for grid config {}, field {}, maybe used a wrong prefix? [{}: {}]",
                        gridId, col.getFieldName(), e1.getClass().getSimpleName(), e1.getMessage());
            }
        }

        final String overridesId = ui.getOverridesGridConfig();
        final String extendsId = ui.getExtendsGridConfig();
        if (overridesId != null && extendsId != null) {
            ERROR_COUNTER.incrementAndGet();
            LOGGER.error("grid config {} overrides and extends at the same time", gridId);
        }

        if (overridesId != null) {
            // postpone this (but check there is no duplicate override)
            if (gridOverrides.putIfAbsent(overridesId, ui) != null) {
                ERROR_COUNTER.incrementAndGet();
                LOGGER.error("grid config {} overridden multiple times", overridesId);
            }
            if (IViewModelContainer.VIEW_MODEL_BY_GRID_ID_REGISTRY.putIfAbsent(overridesId, viewModelId) != null) {
                LOGGER.error("view model by overrides grid config {} defined multiple times", overridesId);
            }
        } else if (extendsId != null) {
            // postpone this (but check there is no duplicate override)
            if (gridExtends.putIfAbsent(extendsId, ui) != null) {
                ERROR_COUNTER.incrementAndGet();
                LOGGER.error("grid config {} extended multiple times", extendsId);
            }
            if (IViewModelContainer.VIEW_MODEL_BY_GRID_ID_REGISTRY.putIfAbsent(extendsId, viewModelId) != null) {
                LOGGER.error("view model by extends grid config {} defined multiple times", extendsId);
            }
        } else {
            // store it in regular entry
            if (IGridConfigContainer.GRID_CONFIG_REGISTRY.putIfAbsent(gridId, ui) != null) {
                ERROR_COUNTER.incrementAndGet();
                LOGGER.error("grid config {} defined multiple times", gridId);
            }
            if (IViewModelContainer.VIEW_MODEL_BY_GRID_ID_REGISTRY.putIfAbsent(gridId, viewModelId) != null) {
                LOGGER.error("view model by grid config {} defined multiple times", gridId);
            }
        }
    }

    private static Map<String, String> empty2minus(final Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return map;
        }
        // the input is an ImmutableMap, we have to copy it to support changes to the (expected) empty components
        final Map<String, String> r = new HashMap<>(map.size());
        for (final Map.Entry<String, String> e : map.entrySet()) {
            final String v = e.getValue();
            r.put(e.getKey(), v == null || v.length() == 0 ? T9tConstants.UI_META_NO_ASSIGNED_VALUE : v);
        }
        return r;
    }

    static void getLeanGridConfigAsObject(final String resourceId) {
        final Map<String, UILeanGridPreferences> leanGridOverrides = LEAN_GRID_OVERRIDES_MAP;
        final Map<String, UILeanGridPreferences> leanGridExtends = LEAN_GRID_EXTENDS_MAP;

        final String gridId = resourceId.replace('$', '/');
        try {
            final URL url = Resources.getResource("gridconfig/" + gridId + ".json");
            final String json = Resources.toString(url, Charsets.UTF_8);

            final Map<String, Object> config = new JsonParser(json, false).parseObject();
            final UILeanGridPreferences prefs = new UILeanGridPreferences();
            MapParser.populateFrom(prefs, config);

            final String overridesId = prefs.getOverridesGridConfig();
            final String extendsId = prefs.getExtendsGridConfig();
            if (overridesId != null && extendsId != null) {
                ERROR_COUNTER.incrementAndGet();
                LOGGER.error("lean grid config {} overrides and extends at the same time", gridId);
            }

            if (overridesId != null) {
                // overriding configuration
                if (leanGridOverrides.putIfAbsent(overridesId, prefs) != null) {
                    ERROR_COUNTER.incrementAndGet();
                    LOGGER.error("lean grid config {} overridden multiple times", overridesId);
                }
            } else if (extendsId != null) {
                // extending configuration
                if (leanGridExtends.putIfAbsent(extendsId, prefs) != null) {
                    ERROR_COUNTER.incrementAndGet();
                    LOGGER.error("lean grid config {} extended multiple times", extendsId);
                }
            } else {
                // original configuration
                if (ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.putIfAbsent(gridId, prefs) != null) {
                    ERROR_COUNTER.incrementAndGet();
                    LOGGER.error("lean grid config {} defined multiple times", gridId);
                }
            }

            // obtain the viewModel
            final String viewModelId = prefs.getViewModel();
            final CrudViewModel<?, ?> vm = IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.get(viewModelId);
            if (vm == null) {
                ERROR_COUNTER.incrementAndGet();
                LOGGER.error("No view model definition found for {}, as specified in grid config {}", viewModelId, gridId);
                return;
            }
            LOGGER.debug("Lean grid config {} maps to view model {} ({})", gridId, viewModelId, vm.dtoClass.getPqon());

            // also convert it to old style...
            // must determine available columns...
            final ColumnCollector cc = new ColumnCollector(MY_DEFAULTS, true);

            if (vm.trackingClass != null)
                cc.addToColumns(vm.trackingClass.getMetaData());

            final String tenantCategory = vm.dtoClass.getProperty("tenantCategory");
            if (!TenantIsolationCategoryType.GLOBAL.getToken().equals(tenantCategory))
                cc.addToColumns(InternalTenantRef42.class$MetaData());

            cc.addToColumns(vm.dtoClass.getMetaData());

            // convert into new format, build a fast access map
            final UIGridPreferences ui = new UIGridPreferences();
            final Map<String, UIColumnConfiguration> colMap = new HashMap<>(cc.columns.size());
            final List<UIColumnConfiguration> cols = new ArrayList<>(cc.columns.size());
            for (final UIColumn c : cc.columns) {
                final UIColumnConfiguration d = convertColumn(c);
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
            ui.setSortDescending(T9tUtil.isTrue(prefs.getSortDescending()));
            ui.setViewModel(viewModelId);
            ui.setWasLean(Boolean.TRUE);
            ui.setIsSolrSearch(vm.dtoClass.getProperty("isSolr") != null);
            ui.setOverridesGridConfig(overridesId);
            ui.setExtendsGridConfig(extendsId);

            // now set any filters
            if (prefs.getFilters() != null) {
                for (final UIFilter f : prefs.getFilters()) {
                    final String s = FieldMappers.stripIndexes(f.getFieldName());
                    final UIColumnConfiguration d = colMap.get(s);
                    if (d == null) {
                        ERROR_COUNTER.incrementAndGet();
                        LOGGER.error("cannot find field {} ({}) referenced as a filter by grid ID {}", f.getFieldName(), s, gridId);
                    } else {
                        d.setFilterType(f.getFilterType());
                        d.setFilterQualifier(f.getQualifier());
                    }
                }
            }

            // now set column visibility
            for (final String fld : prefs.getFields()) {
                final String s = FieldMappers.stripIndexes(fld);
                final UIColumnConfiguration d = colMap.get(s);
                if (d == null) {
                    ERROR_COUNTER.incrementAndGet();
                    LOGGER.error("cannot find field {} ({}) referenced as visible by grid ID {}", fld, s, gridId);
                } else {
                    d.setVisible(true);
                }
            }

            // now set fields which are not sortable (in the backend)
            if (prefs.getUnsortableFields() != null) {
                for (final String fld : prefs.getUnsortableFields()) {
                    final String s = FieldMappers.stripIndexes(fld);
                    final UIColumnConfiguration d = colMap.get(s);
                    if (d == null) {
                        ERROR_COUNTER.incrementAndGet();
                        LOGGER.error("cannot find field {} ({}) referenced as unsortable by grid ID {}", fld, s, gridId);
                    } else {
                        d.setAllowSorting(false);
                    }
                }
            }

            if (prefs.getMapColumns() != null) {
                ui.setMapColumns(prefs.getMapColumns());
            }

            // enrich UI meta, validate and store it
            addUiMeta(vm, viewModelId, ui, gridId);
        } catch (final Exception e) {
            ERROR_COUNTER.incrementAndGet();
            LOGGER.error("Parsing error for lean grid config {}: {}", gridId, ExceptionUtil.causeChain(e));
        }
    }

    private static UIColumnConfiguration convertColumn(final UIColumn src) {
        final UIColumnConfiguration dst = new UIColumnConfiguration();
        dst.setAllowSorting(true);
        dst.setNegateFilter(false);
        dst.setVisible(false);
        dst.setFieldName(src.getFieldName());
        dst.setAlignment(src.getAlignment());
        dst.setLayoutHint(src.getLayoutHint());
        dst.setWidth(src.getWidth());
        return dst;
    }

    static int getErrorCount() {
        return ERROR_COUNTER.get();
    }

    // only visible from InitContainers
    static void reset() {
        ERROR_COUNTER.set(0);
        IViewModelContainer.VIEW_MODEL_BY_GRID_ID_REGISTRY.clear();
        IGridConfigContainer.GRID_CONFIG_REGISTRY.clear();
        ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.clear();
    }

    static void postProcess() {
        processGridOverrides();
        processGridExtends();
        freezeAllGrids();
    }

    private static void processGridOverrides() {
        ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.putAll(LEAN_GRID_OVERRIDES_MAP);
        IGridConfigContainer.GRID_CONFIG_REGISTRY.putAll(GRID_OVERRIDES_MAP);
        LEAN_GRID_OVERRIDES_MAP.forEach((leanGridId, leanGrid) -> {
            LOGGER.debug("lean grid config {} has been overridden.", leanGridId);
        });
        GRID_OVERRIDES_MAP.forEach((gridId, grid) -> {
            LOGGER.debug("grid config {} has been overridden.", gridId);
        });
    }

    private static void processGridExtends() {
        // Extends Lean Grid Config
        final Set<String> overriddenLeanGridIds = LEAN_GRID_OVERRIDES_MAP.keySet();
        final Map<String, UILeanGridPreferences> leanGridExtends = LEAN_GRID_EXTENDS_MAP;
        for (final String leanGridId: overriddenLeanGridIds) {
            final UILeanGridPreferences removedLeanGrid = leanGridExtends.remove(leanGridId);
            if (removedLeanGrid != null) {
                LOGGER.debug("lean grid config {} has been overridden, skip extending.", removedLeanGrid);
            }
        }
        leanGridExtends.forEach((key, extendedLeanGrid) -> {
            final UILeanGridPreferences leanGrid = ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.get(key);
            setOrAddAllCollection(leanGrid.getMapColumns(), extendedLeanGrid.getMapColumns(), c -> leanGrid.setMapColumns(c));
            setOrAddAllCollection(leanGrid.getFilters(), extendedLeanGrid.getFilters(), c -> leanGrid.setFilters(c));
            setOrAddAllCollection(leanGrid.getUnsortableFields(), extendedLeanGrid.getUnsortableFields(), c -> leanGrid.setUnsortableFields(c));
            setOrAddAllCollection(leanGrid.getFields(), extendedLeanGrid.getFields(), c -> leanGrid.setFields(c));
            setOrAddAllCollection(leanGrid.getFieldWidths(), extendedLeanGrid.getFieldWidths(), c -> leanGrid.setFieldWidths(c));
            LOGGER.debug("lean grid config {} has been extended.", key);
        });

        // Extends Grid Config
        final Set<String> overriddenGridIds = GRID_OVERRIDES_MAP.keySet();
        final Map<String, UIGridPreferences> gridExtends = GRID_EXTENDS_MAP;
        for (final String gridId: overriddenGridIds) {
            final UIGridPreferences removedGrid = gridExtends.remove(gridId);
            if (removedGrid != null) {
                LOGGER.debug("grid config {} has been overridden, skip extending.", gridId);
            }
        }
        gridExtends.forEach((key, extendedGrid) -> {
            final UIGridPreferences grid = IGridConfigContainer.GRID_CONFIG_REGISTRY.get(key);
            setOrAddAllCollection(grid.getMapColumns(), extendedGrid.getMapColumns(), c -> grid.setMapColumns(c));
            setOrPutAllMap(grid.getClassProperties(), extendedGrid.getClassProperties(), m -> grid.setClassProperties(m));
            LOGGER.debug("grid config {} has been extended.", key);
        });
    }

    private static <C extends Collection> void setOrAddAllCollection(C oldValue, C newValue, Consumer<C> consumer) {
        if (newValue == null) {
            return;
        }
        if (oldValue == null) {
            consumer.accept(newValue);
        } else {
            oldValue.addAll(newValue);
        }
    }

    private static <M extends Map> void setOrPutAllMap(M oldValue, M newValue, Consumer<M> consumer) {
        if (newValue == null) {
            return;
        }
        if (oldValue == null) {
            consumer.accept(newValue);
        } else {
            oldValue.putAll(newValue);
        }
    }

    private static void freezeAllGrids() {
        ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.values().forEach(pref -> pref.freeze());
        IGridConfigContainer.GRID_CONFIG_REGISTRY.values().forEach(pref -> pref.freeze());
        LOGGER.debug("All grid & lean grid configs are frozen.");
    }
}
