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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SortEvent;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.ColSizeEvent;
import org.zkoss.zul.event.ZulEvents;

import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.arvatosystems.t9t.components.Filter28;
import com.arvatosystems.t9t.components.Grid28;
import com.arvatosystems.t9t.components.TwoSections28;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

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

    public ListHeadRenderer28(ListItemRenderer28<?> defaultListItemRenderer,
            ILeanGridConfigResolver gridConfigResolver,
            Grid28 grid, Listbox lb, Permissionset permissions,
            List<String> listHeaders,
            BonaPortableClass<?> bclass) {
        this.defaultListItemRenderer = defaultListItemRenderer;
        this.gridConfigResolver = gridConfigResolver;
        this.grid = grid;
        this.lb = lb;
        this.permissions = permissions;
        this.listHeaders = listHeaders;
        this.bclass = bclass;
    }

    // single caller from AbstractListBox
    public void createListhead(Listbox listbox) {
        Listhead listhead = new Listhead();
        if (!grid.isHeaderConsistList())
            listhead.setMenupopup("auto");
        listhead.setColumnsgroup(false);
        listhead.setSizable(true);
        // listeners
        listhead.addEventListener(ZulEvents.ON_COL_SIZE, (ColSizeEvent event) -> {
            LOGGER.debug(String.format("--> event:%s - col:%s - width:%s - id:%s", event.getName(), event.getColIndex(), event.getWidth(), event.getColumn().getId()));
            onColSizeListHeader(event);
        });
        createContextMenu(listhead);  // single entry into separate subclass
        listhead.setParent(listbox);
    }

    private void createAllListheaders(Listhead listhead) {
        // loop over all configured columns in the gridConfig
        UILeanGridPreferences gridPreferences = gridConfigResolver.getGridPreferences();
        defaultListItemRenderer.setGridPreferences(gridPreferences.getFields()); // TODO: move to the context menu object?

        final int nFields = gridPreferences.getFields().size();
        for (int i = 0; i < nFields; ++i) {
            final String fieldname = gridPreferences.getFields().get(i);
            boolean isUnsortable = gridPreferences.getUnsortableFields() != null && gridPreferences.getUnsortableFields().contains(fieldname);
            boolean isDynGridColumn = false;
            try {
                FieldDefinition meta = FieldGetter.getFieldDefinitionForPathname(bclass.getMetaData(), fieldname);
                // check if this is a special dynamic width column
                isDynGridColumn = defaultListItemRenderer.isDynField(meta);
                String fieldNoDdl = fieldname.concat(".noDDL");
                isUnsortable = (isUnsortable || bclass.getPropertyMap().containsKey(fieldNoDdl));
            } catch (ApplicationException ue) {
                LOGGER.warn("Could not determine field definition for {}", fieldname);
            }
            if (isDynGridColumn) {
                LOGGER.debug("Determined that {} is a dynamic grid column {}", fieldname);
                for (String header : listHeaders) {
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
            Listhead listhead,
            String fieldName,
            String columnTranslation,
            Integer width,
            FieldDefinition columnDescriptor,
            String defaultSortFieldName, Boolean isDescending, boolean isUnsortable) {
        // AUTO_SORT     <listheader label="columnTranslation" sort="auto(fieldName)"/>
        // BACKEND_SORT  <listheader label="columnTranslation" sort="auto" onSort="@command('sortBackend', col='fieldName')"  />

        boolean isDotted = fieldName.indexOf('.') >= 0;
        boolean isIndexed = fieldName.indexOf('[') >= 0;

        Listheader listheader = new Listheader();
        listheader.setVisible(width > 0);
        listheader.setValue(fieldName);
        listheader.setLabel(columnTranslation);
        listheader.setWidth(String.format("%spx", width)); // setting width does not allow automatic distribution of non used space in list header after upgrade to ZK8
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
        listheader.addEventListener(Events.ON_DROP, (DropEvent event) -> onDropListheader(event));
        listheader.addEventListener(Events.ON_SORT, (SortEvent event) -> onSortEvent(event));
        listheader.addEventListener("onColCheck", e -> {
            onColumnVisibilityChange(e);
        });
    }

    /**
     * This event (onColCheck) is triggered when client modify the visibility of grid columns
     * @param event
     */
    private void onColumnVisibilityChange(Event event) {
        Listheader listHeader = ((Listheader)event.getTarget());
        String colName = listHeader.getLabel();
        Boolean isChecked = (Boolean) event.getData();
        int colIndex = gridConfigResolver.getHeaders().indexOf(colName);

        LOGGER.debug("--> event: {}, column name: {}, value: {}", event.getName(), colName, isChecked);
        gridConfigResolver.setVisibility(colIndex, isChecked);
        if (isChecked) {
            //set the value to the listHeader as the component will not automatically does it
            //but the component will automatically hide the column if unchecked so uncheck does not require this
            listHeader.setWidth(String.format("%spx", gridConfigResolver.getWidths().get(colIndex)));
        }
    }

    private void onColSizeListHeader(ColSizeEvent ev) {
        LOGGER.debug("Column resize of col {}: from {} to {}", ev.getColIndex(), ev.getPreviousWidth(), ev.getWidth());
        gridConfigResolver.changeWidth(ev.getColIndex(), parseWidth(ev.getWidth()), parseWidth(ev.getPreviousWidth()));
        //gridHasChanged(); // not sure why we need the red border here
        //redrawListbox(); //remove it as it will caused the event 'onColCheck' not being fired and i dont see the need of redraw on size change.
    }

    private int parseWidth(String width) {
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
        } catch (NumberFormatException e) {
            LOGGER.warn("The number {} can't be parsed...", width);
            return 80;
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~
    // LISTENER: ON_DROP
    // ~~~~~~~~~~~~~~~~~~~~~~~~
    private void onDropListheader(DropEvent ev) {
        // get the dragged Listheader and the one it is dropped on.
        Listheader dragged = (Listheader) ev.getDragged();
        Listheader droppedOn = (Listheader) ev.getTarget();
        // then get their indexes.
        int from = lb.getListhead().getChildren().indexOf(dragged);
        int to = lb.getListhead().getChildren().indexOf(droppedOn);
        LOGGER.debug(" swap columns from {} to {}", from, to);
        gridConfigResolver.changeColumnOrder(from, to);

        gridHasChanged();
        grid.search();
        redrawListbox();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~
    // LISTENER: ON_SORT
    // ~~~~~~~~~~~~~~~~~~~~~~~~
    private void onSortEvent(SortEvent ev) {
        String fieldname = ((Listheader) ev.getTarget()).getValue();
        boolean isAscending = ev.isAscending();
        LOGGER.debug("Sort event for {}", fieldname);

        gridConfigResolver.newSort(fieldname, !isAscending);
        gridHasChanged();
        grid.search();
        redrawListbox();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~
    // Context Menu
    // ~~~~~~~~~~~~~~~~~~~~~~~~
    final Menuitem menuitemDefault = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "menuDefault"));
    final Menuitem menuitemA       = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "menuA"));
    final Menuitem menuitemB       = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "menuB"));
    final Menuitem menuitemC       = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "menuC"));

    private void configMenuItem(final Menuitem item, Menupopup parent, final int variantToSet) {
        item.setAutocheck(true);
        item.setCheckmark(true);
        item.addEventListener(Events.ON_CHECK, (CheckEvent event) -> {
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

    private void createContextMenu(Listhead listhead) {
        Menupopup menupopup = new Menupopup();
        menupopup.setId(lb.getId() + "_popup");
        menupopup.setParent(lb.getParent());
        listhead.setContext(menupopup);


        //Switch configuration
        Menu switchConfigurationMenu = new Menu(session.translate(PREFIX_GRIDCONFIG28, "switch"));
        switchConfigurationMenu.setParent(menupopup);

        Menupopup switchConfigurationMenupopup = new Menupopup();
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
            Menuitem menuitemSave = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "save"));
            menuitemSave.addEventListener(Events.ON_CLICK, (Event ev) -> {
                gridConfigResolver.save(false);
                gridHasChangedDisable();
                redrawListbox();
            });
            menuitemSave.setParent(menupopup);

            // SAVE TENANT (if allowed)
            if (permissions.contains(OperationType.ADMIN)) {
                Menuitem menuitemSaveTenant = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "saveTenant"));
                menuitemSaveTenant.addEventListener(Events.ON_CLICK, (Event ev) -> {
                    gridConfigResolver.save(true);
                    gridHasChangedDisable();
                    redrawListbox();
                });
                menuitemSaveTenant.setParent(menupopup);
            }
        }

        new Menuseparator().setParent(menupopup);

        //RELOAD
        Menuitem menuitemReload = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "reload"));
        menuitemReload.addEventListener(Events.ON_CLICK, (Event ev) -> {
            gridHasChangedDisable();
            if (gridConfigResolver.reload())
                grid.search();
            redrawListbox();
        });

        menuitemReload.setParent(menupopup);

        new Menuseparator().setParent(menupopup);

        if (permissions.contains(OperationType.CONFIGURE)) {
            // RESET USER configuration ==> delete user configuration
            Menuitem menuitemResetUser = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "delete"));
            //menuitemResetUser.setId(menupopup.getId() + "_menuitemResetUser");
            menuitemResetUser.addEventListener(Events.ON_CLICK, (Event ev) -> {
                gridConfigResolver.deleteConfig(false);
                gridHasChangedDisable();
                redrawListbox();
            });
            menuitemResetUser.setParent(menupopup);

            // RESET TENANT configuration ==> delete tenant configuration (if allowed)
            if (permissions.contains(OperationType.ADMIN)) {
                Menuitem menuitemResetTenant = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "deleteTenant"));
                //menuitemResetTenant.setId(menupopup.getId() + "menuitemResetTenant");
                menuitemResetTenant.addEventListener(Events.ON_CLICK, (Event ev) -> {
                    gridConfigResolver.deleteConfig(true);
                    gridHasChangedDisable();
                    redrawListbox();
                });
                menuitemResetTenant.setParent(menupopup);
            }

            Menuitem editGridMenu = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "editGrid"));
            editGridMenu.setParent(menupopup);

            editGridMenu.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    updateGridHeaders();
                }
            });
            //only create the menu item, if search filters are available.
            if (gridConfigResolver.getFilters().size() > 0) {
                Menuitem editSearchFiltersMenu = new Menuitem(session.translate(PREFIX_GRIDCONFIG28, "editSearchFilters"));
                editSearchFiltersMenu.setParent(menupopup);

                editSearchFiltersMenu.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        updateSearchFilters();
                    }
                });
            }

        }
    }

    private void updateSearchFilters() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gridId", grid.getGridId());

        Window win = (Window) Executions.createComponents("/screens/common/editSearchFilters.zul", null, paramMap);
        win.addEventListener("onClose", new EventListener<Event>() {
            @Override
            public void onEvent(Event closeEvent) throws Exception {
                if (closeEvent.getData() != null) {
                    findAndUpdateSearchFiltersComponent();
                }
            }
        });

    }

    private void findAndUpdateSearchFiltersComponent() {
        //Looking for filter28 and update
          Filter28 filter28 = findFilter28Component(grid.getRoot());
          if (filter28 == null) {
              LOGGER.error("Unable to get Filter28 from windowComponent {} ", grid.getRoot().getId());
              return;
          }

          filter28.resetSearchFilters(gridConfigResolver.getVariant());
      }

    private Filter28 findFilter28Component(Component parent) {
        Filter28 f = null;

        for (Component c : parent.getChildren()) {
            if (c instanceof TwoSections28) {
                f = ((TwoSections28) c).getFilters();
                break;
            } else {
                Filter28 nf = findFilter28Component(c);
                if (nf != null) {
                    return nf;
                }
            }
        }

        return f;
    }

    private void updateGridHeaders() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gridId", grid.getGridId());
        paramMap.put("currentGridList", gridConfigResolver.getGridPreferences().getFields());
        Window win = (Window) Executions.createComponents("/screens/common/editGrid.zul", null, paramMap);
        win.addEventListener("onClose", new EventListener<Event>() {
            @Override
            public void onEvent(Event closeEvent) throws Exception {
                if (closeEvent.getData() != null && closeEvent.getData() instanceof Pair) {
                    @SuppressWarnings("unchecked")
                    Pair<List<String>, List<String>> addRemovePair = (Pair<List<String>, List<String>>) closeEvent
                            .getData();
                    for (String toRemove : addRemovePair.getY()) {
                        int removeIndex = gridConfigResolver.getGridPreferences().getFields().indexOf(toRemove);
                        gridConfigResolver.deleteField(removeIndex);
                    }
                    for (String toAdd : addRemovePair.getX()) {
                        if (!gridConfigResolver.getGridPreferences().getFields().contains(toAdd)) {
                            gridConfigResolver.addField(toAdd);
                        }
                    }
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
        String style = StringUtils.trimToEmpty(lb.getStyle());
        if (style.indexOf(GRID_CHANGE_STYLE) == -1) { //not found
            lb.setStyle(GRID_CHANGE_STYLE);
        }
    }

    void gridHasChangedDisable() {
        String style = StringUtils.trimToEmpty(lb.getStyle());
        if (style.indexOf(GRID_CHANGE_STYLE) > -1) { //found
            lb.setStyle(style.replace(GRID_CHANGE_STYLE, ""));
        }
    }

    private void disableMenuItems(boolean disabled) {
        menuitemDefault.setDisabled(disabled);
        menuitemA.setDisabled(disabled);
        menuitemB.setDisabled(disabled);
        menuitemC.setDisabled(disabled);
    }
    private void checkedMenuItems(boolean checked) {
        menuitemDefault.setChecked(checked);
        menuitemA.setChecked(checked);
        menuitemB.setChecked(checked);
        menuitemC.setChecked(checked);
    }

    public List<String> getListHeaders() {
        return listHeaders;
    }

    public void setListHeaders(List<String> listHeaders) {
        this.listHeaders = listHeaders;
        redrawListbox();
    }
}
