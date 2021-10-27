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
package com.arvatosystems.t9t.component.ee;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkmax.zul.Nav;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zul.Menuitem;

import com.arvatosystems.t9t.tfi.model.bean.Navi;
import com.arvatosystems.t9t.tfi.services.DefaultNavBarCreator;
import com.arvatosystems.t9t.tfi.services.INavBarCreator;
import com.arvatosystems.t9t.tfi.viewmodel.ApplicationViewModel;
import com.arvatosystems.t9t.tfi.viewmodel.navigation.NaviGroupingViewModel;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;

@Singleton
@Specializes
public class NavBarCreatorEE extends DefaultNavBarCreator implements INavBarCreator {

    @Override
    public void createNavBar(final ApplicationViewModel viewModel, final Component container, final NaviGroupingViewModel naviGroups) {
        final int groupCounts = naviGroups.getGroupCount();
        final Navbar navbar = new Navbar("horizontal");
        navbar.setCollapsed(false);
        container.appendChild(navbar);
        createContextMenu(container);

        final Map<String, Nav> folders = new HashMap<>(100);
        for (int groupIndex = 0; groupIndex < groupCounts; groupIndex++) {
            final String groupId = naviGroups.getChild(groupIndex, 0).getFolderCategoryId();
            Nav mainNav = folders.get(groupId);
            final int childCounts = naviGroups.getChildCount(groupIndex);
            if (mainNav == null) {
                final String groupName = naviGroups.getGroup(groupIndex);
                mainNav = new Nav();
                mainNav.setLabel(groupName);
                mainNav.addSclass("header-nav-submenu no-scrollbar ");
                createContextMenuOnEachMenu(mainNav);
                navbar.appendChild(mainNav);
                folders.put(groupId, mainNav);
            }

            for (int childIndex = 0; childIndex < childCounts; childIndex++) {
                final Navi navi = naviGroups.getChild(groupIndex, childIndex);
                final Nav currentNav = getOrCreateNavFolderIfNotExists(folders, navi.getCategoryId());

                // Display grouped subtitle (non clickable)
                if (subtitleShouldDisplay(naviGroups, groupIndex, childIndex)) {
                    final Navitem navitem = new Navitem();
                    navitem.setLabel(navi.getSubcategory());
                    navitem.setDisabled(true);
                    navitem.setZclass("header-nav-subtitle");
                    navitem.setImage(navi.getImg());
                    currentNav.appendChild(navitem);
                }

                // Menu items
                if (navi.isMenuItemVisible()) {
                    final Navitem navitem = new Navitem();
                    navitem.setLabel(navi.getName());
                    // navitem.setSelected(selected == navi);
                    navitem.setAttribute("navi", navi);
                    navitem.addEventListener(Events.ON_CLICK, (ev) -> {
                        setSelected(viewModel, naviGroups, navi, null);
                        navbar.setSelectedItem(navitem);
                    });
                    navitem.setImage(navi.getImg());
                    navitem.setClientAttribute("onClick", "collapseHeaderMenu();");
                    navitem.setClientAttribute("data-navi", navi.getNaviId());
                    navitem.setContext(CONTEXT_MENU_ID);
                    currentNav.appendChild(navitem);
                }
            }

            // check if multi columns needed
            mainNav.addSclass(getSubMenuClass(mainNav.getChildren().size()));
        }
    }

    private void createContextMenuOnEachMenu(final Nav nav) {
        nav.addEventListener(Events.ON_OPEN, ev -> {
            for (final Component comp2 : ev.getTarget().getChildren()) {
                if (comp2 instanceof Menuitem) {
                    ((Navitem) comp2).setContext(CONTEXT_MENU_ID);
                }
            }
        });
    }

    /**
     * Get existing folder, create if not existed.
     */
    protected Nav getOrCreateNavFolderIfNotExists(final Map<String, Nav> folders, final String folderCategoryId) {

        Nav folder = folders.get(folderCategoryId);

        if (folder == null) {
            // get parent, create if not existed recursively
            final String parentFolderCategoryId = Navi.getCategoryIdBeforeLastDot(folderCategoryId);
            final String lastPartCategoryId = Navi.getCategoryIdAfterLastDot(folderCategoryId);
            final Nav parentNav = getOrCreateNavFolderIfNotExists(folders, parentFolderCategoryId);

            folder = new Nav(
                    getSubmenuFolderTranslated(ApplicationSession.get(), parentFolderCategoryId, lastPartCategoryId));
            folder.setSclass("header-nav-nested-menu no-highlight");
            folder.setImage("/img/folder.png");
            folder.setId(folderCategoryId);
            parentNav.appendChild(folder);
            folders.put(folderCategoryId, folder);
        }

        return folder;
    }
}
