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
package com.arvatosystems.t9t.zkui.components.grid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SortEvent;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.ColSizeEvent;
import org.zkoss.zul.event.ZulEvents;

import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.arvatosystems.t9t.zkui.components.basic.Filter28;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.components.basic.TwoSections28;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.T9tConfigConstants;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.Multiplicity;
import de.jpaw.bonaparte.util.FieldGetter;
import de.jpaw.util.ApplicationException;

public class ListHeadRenderer28 {
    private static final Logger LOGGER              = LoggerFactory.getLogger(ListHeadRenderer28.class);
    private static final String GRID_CHANGE_STYLE   = "border:1px solid #DD4B39;";
    public  static final String PREFIX_GRIDCONFIG28 = "com.gridconfig";
    private final ApplicationSession session = ApplicationSession.get();
    private final ListItemRenderer28<?> defaultListItemRenderer;
    private final ILeanGridConfigResolver gridConfigResolver;
    private final Listbox lb;
    private final Grid28 grid;
    private final Permissionset permissions;
    private final BonaPortableClass<?> bclass;
    private List<String> listHeaders;  // extra headers for Lists
    private boolean dynamicColumnSize;

    public ListHeadRenderer28(final ListItemRenderer28<?> defaultListItemRenderer,
            final ILeanGridConfigResolver gridConfigResolver,
            final Grid28 grid, final Listbox lb, final Permissionset permissions,
            final List<String> listHeaders,
            final BonaPortableClass<?> bclass, final boolean dynamicColumnSize) {
        this.defaultListItemRenderer = defaultListItemRenderer;
        this.gridConfigResolver = gridConfigResolver;
        this.grid = grid;
        this.lb = lb;
        this.permissions = permissions;
        this.listHeaders = listHeaders;
        this.bclass = bclass;
        this.dynamicColumnSize = dynamicColumnSize;
    }

    // single caller from AbstractListBox
    public void createListhead(final Listbox listbox) {
        final Listhead listhead = new Listhead();
        if (!grid.isHeaderConsistList())
            listhead.setMenupopup("auto");
        listhead.setColumnsgroup(false);
        listhead.setSizable(true);
        // listeners
        listhead.addEventListener(ZulEvents.ON_COL_SIZE, (final ColSizeEvent event) -> {
            LOGGER.atDebug().log("--> event:{} - col:{} - width:{} - id:{}", event.getName(), event.getColIndex(), event.getWidth(),
                    event.getColumn().getId());

            onColSizeListHeader(event);
        });
        createContextMenu(listhead);  // single entry into separate subclass
        listhead.setParent(listbox);
    }

    private void createAllListheaders(final Listhead listhead) {
        // loop over all configured columns in the gridConfig
        final UILeanGridPreferences gridPreferences = gridConfigResolver.getGridPreferences();
        defaultListItemRenderer.setGridPreferences(gridPreferences.getFields()); // TODO: move to the context menu object?

        final int nFields = gridPreferences.getFields().size();
        for (int i = 0; i < nFields; ++i) {
            final String fieldname = gridPreferences.getFields().get(i);
            boolean isUnsortable = gridPreferences.getUnsortableFields() != null && gridPreferences.getUnsortableFields().contains(fieldname);
            boolean isDynGridColumn = false;
            try {
                final FieldDefinition meta = FieldGetter.getFieldDefinitionForPathname(bclass.getMetaData(), fieldname);
                // check if this is a special dynamic width column
                isDynGridColumn = defaultListItemRenderer.isDynField(meta);
                final String fieldNoDdl = fieldname.concat(".noDDL");
                isUnsortable = (isUnsortable || bclass.getPropertyMap().containsKey(fieldNoDdl));
            } catch (final ApplicationException ue) {
                LOGGER.warn("Could not determine field definition for {}", fieldname);
            }
            if (isDynGridColumn) {
                LOGGER.debug("Determined that {} is a dynamic grid column {}", fieldname);
                for (final String header : listHeaders) {
                    createListheader(
                        listhead,
                        fieldname,
                        header,                                         // provided header text
                        gridConfigResolver.getWidths().get(i),          // always the same width
                        gridConfigResolver.getVisibleColumns().get(i),  // always the same
                        null,                                           // no sorting
                        false,
                        true);
                }
            } else {
                // regular column
                createListheader(
                        listhead,
                        fieldname,
                        gridConfigResolver.getHeaders().get(i),
                        gridConfigResolver.getWidths().get(i),
                        gridConfigResolver.getVisibleColumns().get(i),
                        gridPreferences.getSortColumn(),
                        gridPreferences.getSortDescending(),
                        isUnsortable);
            }
        }
    }



    private void createListheader(
            final Listhead listhead,
            final String fieldName,
            final String columnTranslation,
            final Integer width,
            final FieldDefinition columnDescriptor,
            final String defaultSortFieldName, final Boolean isDescending, final boolean isUnsortable) {
        // AUTO_SORT     <listheader label="columnTranslation" sort="auto(fieldName)"/>
        // BACKEND_SORT  <listheader label="columnTranslation" sort="auto" onSort="@command('sortBackend', col='fieldName')"  />

        final boolean isDotted = fieldName.indexOf('.') >= 0;
        final boolean isIndexed = fieldName.indexOf('[') >= 0;

        final Listheader listheader = new Listheader();
        listheader.setVisible(width > 0);
        listheader.setValue(fieldName);
        listheader.setLabel(columnTranslation);

        if (dynamicColumnSize) {
            listheader.setHflex("min");
        } else {
            // setting width does not allow automatic distribution of non used space in list header after upgrade to ZK8
            listheader.setWidth(width + "px");
        }
        // this criteria is a bit too pessimistic, but want to be on the safe side initially.
        if (!isDotted && !isIndexed && !isUnsortable && Multiplicity.LIST != columnDescriptor.getMultiplicity())
            listheader.setSort("auto");
        else
            listheader.setSort("none");
        if (fieldName.equals(defaultSortFieldName)) {
            // sortDir one of "ascending", "descending" and "natural"
            listheader.setSortDirection(isDescending ? "descending" : "ascending");
        } else {
            listheader.setSortDirection("natural");
        }
        listheader.setDraggable("true");
        listheader.setDroppable("true");
        listheader.setParent(listhead);

        // listeners
        listheader.addEventListener(Events.ON_DROP, (final DropEvent event) -> onDropListheader(event));
        listheader.addEventListener(Events.ON_SORT, (final SortEvent event) -> onSortEvent(event));
        listheader.addEventListener("onColCheck", e -> {
            onColumnVisibilityChange(e);
        });

        buildHeaderTooltip(listheader, fieldName, columnTranslation);
    }

    private void buildHeaderTooltip(final Listheader listheader, final String fieldName, final String columnTranslation) {
        final String toolTipId = grid.getGridId() + "." + fieldName + ".infoTooltip";

        try {
            final Popup tooltip = new Popup();
            tooltip.setParent(listheader);
            tooltip.setId(toolTipId);

            final Label label = new Label();
            label.setValue(columnTranslation);
            label.setParent(tooltip);

            listheader.setTooltipAttributes(tooltip, "before_start", null, null, null);
        } catch (final UiException uie) {
            LOGGER.debug("Tooltip {} already existed in the IdSpaces, do not need to register again.");
        }
    }

    /**
     * This event (onColCheck) is triggered when client modify the visibility of grid columns
     * @param event
     */
    private void onColumnVisibilityChange(final Event event) {
        final Listheader listHeader = ((Listheader)event.getTarget());
        final String colName = listHeader.getLabel();
        final Boolean isChecked = (Boolean) event.getData();
        final int colIndex = gridConfigResolver.getHeaders().indexOf(colName);

        LOGGER.debug("--> event: {}, column name: {}, value: {}", event.getName(), colName, isChecked);
        gridConfigResolver.setVisibility(colIndex, isChecked);
        if (isChecked) {
            //set the value to the listHeader as the component will not automatically does it
            //but the component will automatically hide the column if unchecked so uncheck does not require this
            listHeader.setWidth(gridConfigResolver.getWidths().get(colIndex) + "px");
        }
    }

    private void onColSizeListHeader(final ColSizeEvent ev) {
        LOGGER.debug("Column resize of col {}: from {} to {}", ev.getColIndex(), ev.getPreviousWidth(), ev.getWidth());
        gridConfigResolver.changeWidth(ev.getColIndex(), parseWidth(ev.getWidth()), parseWidth(ev.getPreviousWidth()));
        gridHasChanged();
        //redrawListbox(); //remove it as it will caused the event 'onColCheck' not being fired and i dont see the need of redraw on size change.
    }

    private int parseWidth(final String width) {
        if (width == null) {
            return 0;
        }
        // why the replaceAll of everything expect numbers?? --> ZK gives back "90px" :-(
        try {
            Integer intWidth = Integer.parseInt(width.replaceAll("[^0-9]", ""));

            if (width.contains("-")) {
                intWidth = intWidth * -1;
            }

            return intWidth;
        } catch (final NumberFormatException e) {
            LOGGER.warn("The number {} can't be parsed...", width);
            return 80;
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~
    // LISTENER: ON_DROP
    // ~~~~~~~~~~~~~~~~~~~~~~~~
    private void onDropListheader(final DropEvent ev) {
        // get the dragged Listheader and the one it is dropped on.
        final Listheader dragged = (Listheader) ev.getDragged();
        final Listheader droppedOn = (Listheader) ev.getTarget();
        // then get their indexes.
        final int from = lb.getListhead().getChildren().indexOf(dragged);
        final int to = lb.getListhead().getChildren().indexOf(droppedOn);
        LOGGER.debug(" swap columns from {} to {}", from, to);
        gridConfigResolver.changeColumnOrder(from, to);

        gridHasChanged();
        grid.search();
        redrawListbox();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~
    // LISTENER: ON_SORT
    // ~~~~~~~~~~~~~~~~~~~~~~~~
    private void onSortEvent(final SortEvent ev) {
        final String fieldname = ((Listheader) ev.getTarget()).getValue();
        final boolean isAscending = ev.isAscending();
        LOGGER.debug("Sort event for {}", fieldname);

        gridConfigResolver.newSort(fieldname, !isAscending);
        grid.search();
        final boolean markRedOnSort = ZulUtils.readBooleanConfig(T9tConfigConstants.GRID_MARK_RED_ON_SORT);
        if (markRedOnSort) {
            gridHasChanged();
        }
        redrawListbox();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~
    // Context Menu
    // ~~~~~~~~~~~~~~~~~~~~~~~~
    final Menuitem menuitemDefault = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "menuDefault"));
    final Menuitem menuitemA       = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "menuA"));
    final Menuitem menuitemB       = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "menuB"));
    final Menuitem menuitemC       = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "menuC"));

    private void configMenuItem(final Menuitem item, final Menupopup parent, final int variantToSet) {
        item.setAutocheck(true);
        item.setCheckmark(true);
        item.addEventListener(Events.ON_CHECK, (final CheckEvent event) -> {
            checkedMenuItems(false);
            disableMenuItems(false);
            item.setChecked(true);
            item.setDisabled(true);
            gridConfigResolver.setVariant(variantToSet);
            gridHasChangedDisable();
            redrawListbox();
            // getGridConfigBySelectedSwitchConfig();
        });
        item.setParent(parent);
    }

    private void createContextMenu(final Listhead listhead) {
        final Menupopup menupopup = new Menupopup();
        menupopup.setId(lb.getId() + "_popup");
        menupopup.setParent(lb.getParent());
        listhead.setContext(menupopup);


        //Switch configuration
        final Menu switchConfigurationMenu = new Menu(session.translate(PREFIX_GRIDCONFIG28, "switch"));
        switchConfigurationMenu.setParent(menupopup);

        final Menupopup switchConfigurationMenupopup = new Menupopup();
        switchConfigurationMenupopup.setParent(switchConfigurationMenu);
        //switchConfigurationMenu.setContext(switchConfigurationMenupopup);

        configMenuItem(menuitemDefault, switchConfigurationMenupopup, 0);
        menuitemDefault.setChecked(true);
        menuitemDefault.setDisabled(true);
        configMenuItem(menuitemA, switchConfigurationMenupopup, 1);
        configMenuItem(menuitemB, switchConfigurationMenupopup, 2);
        configMenuItem(menuitemC, switchConfigurationMenupopup, 3);

        new Menuseparator().setParent(menupopup);

        if (permissions.contains(OperationType.CONFIGURE)) {
            // SAVE ==> User specific
            final Menuitem menuitemSave = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "save"));
            menuitemSave.addEventListener(Events.ON_CLICK, (final Event ev) -> {
                gridConfigResolver.save(false);
                gridHasChangedDisable();
                redrawListbox();
            });
            menuitemSave.setParent(menupopup);

            // SAVE TENANT (if allowed)
            if (permissions.contains(OperationType.ADMIN)) {
                final Menuitem menuitemSaveTenant = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "saveTenant"));
                menuitemSaveTenant.addEventListener(Events.ON_CLICK, (final Event ev) -> {
                    gridConfigResolver.save(true);
                    gridHasChangedDisable();
                    redrawListbox();
                });
                menuitemSaveTenant.setParent(menupopup);
            }
        }

        new Menuseparator().setParent(menupopup);

        //RELOAD
        final Menuitem menuitemReload = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "reload"));
        menuitemReload.addEventListener(Events.ON_CLICK, (final Event ev) -> {
            gridHasChangedDisable();
            if (gridConfigResolver.reload())
                grid.search();
            redrawListbox();
        });

        menuitemReload.setParent(menupopup);

        new Menuseparator().setParent(menupopup);

        if (permissions.contains(OperationType.CONFIGURE)) {
            // RESET USER configuration ==> delete user configuration
            final Menuitem menuitemResetUser = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "delete"));
            //menuitemResetUser.setId(menupopup.getId() + "_menuitemResetUser");
            menuitemResetUser.addEventListener(Events.ON_CLICK, (final Event ev) -> {
                gridConfigResolver.deleteConfig(false);
                gridHasChangedDisable();
                redrawListbox();
            });
            menuitemResetUser.setParent(menupopup);

            // RESET TENANT configuration ==> delete tenant configuration (if allowed)
            if (permissions.contains(OperationType.ADMIN)) {
                final Menuitem menuitemResetTenant = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "deleteTenant"));
                //menuitemResetTenant.setId(menupopup.getId() + "menuitemResetTenant");
                menuitemResetTenant.addEventListener(Events.ON_CLICK, (final Event ev) -> {
                    gridConfigResolver.deleteConfig(true);
                    gridHasChangedDisable();
                    redrawListbox();
                });
                menuitemResetTenant.setParent(menupopup);
            }

            final Menuitem editGridMenu = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "editGrid"));
            editGridMenu.setParent(menupopup);

            editGridMenu.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
                @Override
                public void onEvent(final Event event) throws Exception {
                    updateGridHeaders();
                }
            });
            //only create the menu item, if search filters are available.
            if (gridConfigResolver.getFilters().size() > 0) {
                final Menuitem editSearchFiltersMenu = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "editSearchFilters"));
                editSearchFiltersMenu.setParent(menupopup);

                editSearchFiltersMenu.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
                    @Override
                    public void onEvent(final Event event) throws Exception {
                        updateSearchFilters();
                    }
                });
            }
            if (grid.isColumnAggregationAllowed()) {
                final Menuitem columnAggregationMenu = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "columnAggregations"));
                columnAggregationMenu.setParent(menupopup);
                columnAggregationMenu.addEventListener(Events.ON_CLICK, (event) -> configureColumnAggregations());
            }
        }
    }

    private void configureColumnAggregations() {
        final Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gridId", grid.getGridId());
        paramMap.put("gridConfigResolver", gridConfigResolver);
        final Window win = (Window) Executions.createComponents("/screens/common/columnAggregations.zul", null, paramMap);
        win.addEventListener("onClose", (closeEvent) -> {
            if (closeEvent.getData() != null && closeEvent.getData() instanceof Boolean) {
                gridHasChanged();
                grid.search();
            }
        });
    }

    private void updateSearchFilters() {
        final Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gridId", grid.getGridId());
        paramMap.put("gridConfigResolver", gridConfigResolver);

        final Window win = (Window) Executions.createComponents("/screens/common/editSearchFilters.zul", null, paramMap);
        win.addEventListener("onClose", new EventListener<Event>() {
            @Override
            public void onEvent(final Event closeEvent) throws Exception {
                if (closeEvent.getData() != null) {
                    findAndUpdateSearchFiltersComponent();
                    gridHasChanged();
                }
            }
        });

    }

    private void findAndUpdateSearchFiltersComponent() {
        //Looking for filter28 and update
          final Filter28 filter28 = findFilter28Component(grid.getRoot());
          if (filter28 == null) {
              LOGGER.error("Unable to get Filter28 from windowComponent {} ", grid.getRoot().getId());
              return;
          }

          filter28.resetSearchFilters(gridConfigResolver.getVariant());
      }

    private Filter28 findFilter28Component(final Component parent) {
        Filter28 f = null;

        for (final Component c : parent.getChildren()) {
            if (c instanceof TwoSections28) {
                f = ((TwoSections28) c).getFilters();
                break;
            } else {
                final Filter28 nf = findFilter28Component(c);
                if (nf != null) {
                    return nf;
                }
            }
        }

        return f;
    }

    private void updateGridHeaders() {
        final Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gridId", grid.getGridId());
        paramMap.put("currentGridList", gridConfigResolver.getGridPreferences().getFields());
        final Window win = (Window) Executions.createComponents("/screens/common/editGrid.zul", null, paramMap);
        win.addEventListener("onClose", new EventListener<Event>() {
            @Override
            public void onEvent(final Event closeEvent) throws Exception {
                if (closeEvent.getData() != null && closeEvent.getData() instanceof Pair) {
                    @SuppressWarnings("unchecked")
                    final
                    Pair<List<String>, List<String>> addRemovePair = (Pair<List<String>, List<String>>) closeEvent
                            .getData();
                    for (final String toRemove : addRemovePair.getY()) {
                        final int removeIndex = gridConfigResolver.getGridPreferences().getFields().indexOf(toRemove);
                        gridConfigResolver.deleteField(removeIndex);
                    }
                    for (final String toAdd : addRemovePair.getX()) {
                        if (!gridConfigResolver.getGridPreferences().getFields().contains(toAdd)) {
                            gridConfigResolver.addField(toAdd);
                        }
                    }
                    gridHasChanged();
                    redrawListbox();
                    grid.search();
                }
            }
        });
    }

    public void redrawListbox() {
        lb.getListhead().getChildren().clear();
        createAllListheaders(lb.getListhead());
    }

    public void gridHasChanged() {
        final String style = StringUtils.trimToEmpty(lb.getStyle());
        if (style.indexOf(GRID_CHANGE_STYLE) == -1) { //not found
            lb.setStyle(GRID_CHANGE_STYLE);
        }
    }

    void gridHasChangedDisable() {
        final String style = StringUtils.trimToEmpty(lb.getStyle());
        if (style.indexOf(GRID_CHANGE_STYLE) > -1) { //found
            lb.setStyle(style.replace(GRID_CHANGE_STYLE, ""));
        }
    }

    private void disableMenuItems(final boolean disabled) {
        menuitemDefault.setDisabled(disabled);
        menuitemA.setDisabled(disabled);
        menuitemB.setDisabled(disabled);
        menuitemC.setDisabled(disabled);
    }
    private void checkedMenuItems(final boolean checked) {
        menuitemDefault.setChecked(checked);
        menuitemA.setChecked(checked);
        menuitemB.setChecked(checked);
        menuitemC.setChecked(checked);
    }

    public List<String> getListHeaders() {
        return listHeaders;
    }

    public void setListHeaders(final List<String> listHeaders) {
        this.listHeaders = listHeaders;
        redrawListbox();
    }

    public void setDynamicColumnSize(final boolean dynamicColumnSize) {
        this.dynamicColumnSize = dynamicColumnSize;
    }
}
