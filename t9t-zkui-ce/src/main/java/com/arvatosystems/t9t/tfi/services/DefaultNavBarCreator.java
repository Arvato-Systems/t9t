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
package com.arvatosystems.t9t.tfi.services;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menubar;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.auth.request.SetDefaultScreenRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.components.Context28;
import com.arvatosystems.t9t.services.T9TRemoteUtils;
import com.arvatosystems.t9t.tfi.model.bean.Navi;
import com.arvatosystems.t9t.tfi.viewmodel.ApplicationViewModel;
import com.arvatosystems.t9t.tfi.viewmodel.navigation.NaviGroupingViewModel;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Singleton
@Fallback
public class DefaultNavBarCreator implements INavBarCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNavBarCreator.class);

    protected static final int MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN = 13;
    protected static final String VM_ID = "navbarCreator";
    protected static final String CONTEXT_MENU_ID = "menu.ctx";
    protected static final String SET_AS_USER_DEFAULT_ID = "setAsDefaultScreen";
    protected static final String RESET_USER_DEFAULT_ID = "resetDefaultScreen";
    private static final String MENU_GROUP = "menu.group";
    private static final String DEFAULT = "defaults";

    protected final IApplicationDAO applicationDAO = Jdp.getRequired(IApplicationDAO.class);
    protected final T9TRemoteUtils t9tRemoteUtils = Jdp.getRequired(T9TRemoteUtils.class);

    @Override
    public void createNavBar(final ApplicationViewModel viewModel, final Component container,
            final NaviGroupingViewModel naviGroups) {
        final int groupCounts = naviGroups.getGroupCount();
        final Menubar menubar = new Menubar("horizontal");
        menubar.setWidth("100%");
        menubar.setAutodrop(false);
        container.appendChild(menubar);
        createContextMenu(container);
        final Map<String, Menupopup> folders = new HashMap<>(100);

        for (int groupIndex = 0; groupIndex < groupCounts; groupIndex++) {
            final String groupId = naviGroups.getChild(groupIndex, 0).getFolderCategoryId();
            final String groupName = naviGroups.getGroup(groupIndex);
            Menupopup menuPopup = folders.get(groupId);

            if (menuPopup == null) {
                final Menu menu = createMenu(groupName, groupIndex);
                menubar.appendChild(menu);
                menuPopup = new Menupopup();
                addSClass(menuPopup, "nav-menupopup");
                menu.appendChild(menuPopup);
                createContextMenuOnEachMenu(menuPopup);
                folders.put(groupId, menuPopup);
            }

            for (int childIndex = 0; childIndex < naviGroups.getChildCount(groupIndex); childIndex++) {
                final Navi navi = naviGroups.getChild(groupIndex, childIndex);
                final Menupopup currentPopup = getOrCreateFolderIfNotExists(folders, navi.getCategoryId());

                // Display grouped subtitle (non clickable)
                if (subtitleShouldDisplay(naviGroups, groupIndex, childIndex)) {
                    final Menuitem menuItem = new Menuitem();
                    menuItem.setLabel(navi.getSubcategory());
                    menuItem.setDisabled(true);
                    menuItem.setZclass("header-nav-subtitle");
                    menuItem.setImage(navi.getImg());
                    currentPopup.appendChild(menuItem);
                }

                // Menu items
                if (navi.isMenuItemVisible()) {
                    final Menuitem menuItem = new Menuitem();
                    menuItem.setLabel(navi.getName());
                    menuItem.setAttribute("navi", navi);
                    menuItem.addEventListener(Events.ON_CLICK, (ev) -> {
                        setSelected(viewModel, naviGroups, navi, null);
                    });
                    menuItem.setImage(navi.getImg());
                    menuItem.setClientAttribute("onClick",
                            "collapseHeaderMenu(); setNavi('" + groupName + "','" + navi.getNaviId() + "');");
                    menuItem.setClientAttribute("data-navi", navi.getNaviId());
                    currentPopup.appendChild(menuItem);
                }
            }
            // check if multi columns needed
            addSClass(menuPopup, getSubMenuClass(menuPopup.getChildren().size()));
        }
    }

    private void addSClass(final Menupopup menuPopup, final String toAdd) {
        if (toAdd == null || toAdd.length() == 0) {
            // nothing to do
            return;
        }
        final String current = menuPopup.getSclass();
        if (current == null || current.length() == 0) {
            // just set it
            menuPopup.setSclass(toAdd);
            return; // also do not execute final statement
        } else if (current.indexOf(toAdd) >= 0) {
            // nothing to do, already there!
            return;
        }
        // both are not null, merge them. We assume the class to add is a single token
        menuPopup.setSclass(current + " " + toAdd);
    }

    protected void setSelected(final ApplicationViewModel viewModel, final NaviGroupingViewModel naviGroups,
            final Navi selectedNavi, final String selectedGroup) {
        if (selectedGroup != null) {
            setNaviGroup(naviGroups, selectedGroup, true); // used in the EE implementation
        } else if (selectedNavi != null) {
            viewModel.createComponents(selectedNavi);
            // this.selected = selected; // value is never read, skipping assignment
            setNaviGroup(naviGroups, (selectedNavi).getCategory(), false);
        }
    }

    protected void setNaviGroup(final NaviGroupingViewModel naviGroups, final String category, final boolean isClosePermitted) {
        final Integer groupIndex = applicationDAO.getGroupIndexByCategory(category, naviGroups);
        if (groupIndex != null) {
            if (!naviGroups.isGroupOpened(groupIndex)) {
                naviGroups.addOpenGroup(groupIndex.intValue());
            } else if (isClosePermitted && naviGroups.isGroupOpened(groupIndex)) {
                naviGroups.removeOpenGroup(groupIndex.intValue());
            }
        }
    }

    private Menu createMenu(final String groupName, final int groupIndex) {
        // String sclass = "header-nav-submenu no-scrollbar ";
        final Menu menu = new Menu();
        menu.setLabel(groupName);
        menu.setSclass("header-nav-submenu no-scrollbar ");
        menu.setClientAttribute("data-navi", groupName);
        return menu;
    }

    /**
     * To check if subtitle should be display based on the menus iteration if there
     * is a new subtitle compared to the previous one, return true
     *
     * @param index
     * @param childIndex
     * @return
     */
    protected boolean subtitleShouldDisplay(final NaviGroupingViewModel naviGroups, final int index, final int childIndex) {

        if (childIndex != 0) {
            return naviGroups.getChild(index, childIndex).getSubcategory() != null
                    && naviGroups.getChild(index, childIndex).getSubcategory() != naviGroups.getChild(index, childIndex - 1).getSubcategory();
        } else {
            return naviGroups.getChild(index, childIndex).getSubcategory() != null;
        }
    }

    protected final String getSubMenuClass(final int childCount) {

        if (childCount > MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN) {
            int i = childCount / MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN;
            if (childCount > MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN) {
                final int r = childCount % MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN;
                if (r > MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN / 2)
                    i++;
            }

            if (i > 1) {
                return "header-nav-submenu-" + i + "c";
            }
        }

        return "";
    }

    /**
     * Create context menus for menu container
     *
     * @param component
     */
    protected final void createContextMenu(final Component container) {
        final Context28 contextMenu = new Context28();
        contextMenu.setId(CONTEXT_MENU_ID);
        contextMenu.setContextOptions(SET_AS_USER_DEFAULT_ID + ",," + RESET_USER_DEFAULT_ID);
        contextMenu.setParent(container);

        contextMenu.addEventListener(Events.ON_OPEN, ev -> {
            final Component comp = ((OpenEvent) ev).getReference();
            if (comp != null) {
                for (final Component comp2 : contextMenu.getChildren()) {
                    comp2.setAttribute("data-navi", comp.getClientAttribute("data-navi"));
                }
            }
        });

        for (final Component comp : contextMenu.getChildren()) {

            if (comp.getId().isEmpty()) {
                continue;
            }

            if (comp.getId().equals(CONTEXT_MENU_ID + "." + SET_AS_USER_DEFAULT_ID)) {
                // setAsUserDefault
                comp.addEventListener(Events.ON_CLICK, ev -> {
                    final SetDefaultScreenRequest request = new SetDefaultScreenRequest();
                    request.setDefaultScreenId((String) ev.getTarget().getAttribute("data-navi"));
                    final ServiceResponse response = t9tRemoteUtils.executeExpectOk(request, ServiceResponse.class);
                    if (response.getReturnCode() == ApplicationException.SUCCESS) {
                        Messagebox.show(ApplicationSession.get().translate(VM_ID, "defaultUserScreen.success"),
                                ApplicationSession.get().translate(VM_ID, "defaultScreen"), Messagebox.OK,
                                Messagebox.INFORMATION);
                    }
                });
            } else if (comp.getId().equals(CONTEXT_MENU_ID + "." + RESET_USER_DEFAULT_ID)) {
                // resetUserDefault
                comp.addEventListener(Events.ON_CLICK, ev -> {
                    final SetDefaultScreenRequest request = new SetDefaultScreenRequest();
                    request.setDefaultScreenId(null);
                    final ServiceResponse response = t9tRemoteUtils.executeExpectOk(request, ServiceResponse.class);
                    if (response.getReturnCode() == ApplicationException.SUCCESS) {
                        Messagebox.show(ApplicationSession.get().translate(VM_ID, "resetDefaultUserScreen.success"),
                                ApplicationSession.get().translate(VM_ID, "defaultScreen"), Messagebox.OK,
                                Messagebox.INFORMATION);
                    }
                });
            } else {
                LOGGER.debug("Context Menu with {} is not implemented. ", comp.getId());
                throw new UnsupportedOperationException();
            }
        }
    }

    private void createContextMenuOnEachMenu(final Menupopup menu) {
        menu.addEventListener(Events.ON_OPEN, ev -> {
            for (final Component comp2 : ev.getTarget().getChildren()) {
                if (comp2 instanceof Menuitem) {
                    ((Menuitem) comp2).setContext(CONTEXT_MENU_ID);
                }
            }
        });
    }

    /**
     * Get existing folder, create if not existed.
     */
    protected Menupopup getOrCreateFolderIfNotExists(final Map<String, Menupopup> folders, final String folderCategoryId) {

        final Menupopup folder = folders.get(folderCategoryId);
        if (folder == null) {
            // get parent, create if not existed recursively
            final String parentFolderCategoryId = Navi.getCategoryIdBeforeLastDot(folderCategoryId);
            final String lastPartCategoryId = Navi.getCategoryIdAfterLastDot(folderCategoryId);
            final Menupopup parentPopup = getOrCreateFolderIfNotExists(folders, parentFolderCategoryId);

            final Menu submenu = new Menu(getSubmenuFolderTranslated(ApplicationSession.get(), parentFolderCategoryId, lastPartCategoryId));
            submenu.addSclass("nav-submenu");
            submenu.setImage("/img/folder.png");
            parentPopup.appendChild(submenu);
            final Menupopup submenuPopup = new Menupopup();
            addSClass(submenuPopup, "nav-menupopup");
            submenuPopup.setId(folderCategoryId);
            submenu.appendChild(submenuPopup);
            createContextMenuOnEachMenu(submenuPopup);
            folders.put(folderCategoryId, submenuPopup);
            return submenuPopup;
        }

        return folder;
    }

    protected static String getSubmenuFolderTranslated(final ApplicationSession as, final String prefix, final String submenuId) {
        final String fieldname = prefix + "." + submenuId;
        final String fallback = DEFAULT + "." + submenuId;

        return as.translateWithFallback(MENU_GROUP, fieldname, fallback);
    }
}
