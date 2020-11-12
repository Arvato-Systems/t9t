package com.arvatosystems.t9t.tfi.services;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menubar;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

import com.arvatosystems.t9t.tfi.model.bean.Navi;
import com.arvatosystems.t9t.tfi.viewmodel.ApplicationViewModel;
import com.arvatosystems.t9t.tfi.viewmodel.navigation.NaviGroupingViewModel;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
@Fallback
public class DefaultNavBarCreator implements INavBarCreator {

    protected final int MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN = 13;
    protected final IApplicationDAO applicationDAO = Jdp.getRequired(IApplicationDAO.class);
    // protected Object selected;  // is set once, but never read... and cannot have instance fields per session in a singleton

    @Override
    public void createNavBar(final ApplicationViewModel viewModel, final Component container, final NaviGroupingViewModel naviGroups) {
        int groupCounts = naviGroups.getGroupCount();
        Menubar menubar = new Menubar("horizontal");
        menubar.setWidth("100%");
        menubar.setAutodrop(false);
        container.appendChild(menubar);

        for (int groupIndex = 0; groupIndex < groupCounts; groupIndex++) {
            String groupName = naviGroups.getGroup(groupIndex);
            Menu menu = createMenu(groupName, groupIndex);
            menubar.appendChild(menu);
            Menupopup menuPopup = new Menupopup();
            // the following works for ZK >= 8.6.1, but the latest open CE release of the 8.6 tree is 8.6.0.1
//            menuPopup.addSclass("nav-menupopup");
//            menuPopup.addSclass(getSubMenuClass(naviGroups.getChildCount(groupIndex)));
            addSClass(menuPopup, getSubMenuClass(naviGroups.getChildCount(groupIndex)));
            addSClass(menuPopup, "nav-menupopup");
            menu.appendChild(menuPopup);

            for (int childIndex = 0; childIndex < naviGroups.getChildCount(groupIndex); childIndex++) {
                Navi navi = naviGroups.getChild(groupIndex, childIndex);
                // Display grouped subtitle (non clickable)
                if (subtitleShouldDisplay(naviGroups, groupIndex, childIndex)) {
                    Menuitem menuItem = new Menuitem();
                    menuItem.setLabel(navi.getSubcategory());
                    menuItem.setDisabled(true);
                    menuItem.setZclass("header-nav-subtitle");
                    menuItem.setImage(navi.getImg());
                    menuPopup.appendChild(menuItem);
                }

                // Menu items
                if (navi.isMenuItemVisible()) {
                    Menuitem menuItem = new Menuitem();
                    menuItem.setLabel(navi.getName());
                    menuItem.setAttribute("navi", navi);
                    menuItem.addEventListener(Events.ON_CLICK, (ev) -> {
                        setSelected(viewModel, naviGroups, navi, null);
                    });
                    menuItem.setImage(navi.getImg());
                    menuItem.setClientAttribute("onClick", "collapseHeaderMenu(); setNavi('" + groupName + "','" + navi.getNaviId() + "');");
                    menuItem.setClientAttribute("data-navi", navi.getNaviId());
                    menuPopup.appendChild(menuItem);
                }
            }
        }
    }

    private void addSClass(Menupopup menuPopup, String toAdd) {
        if (toAdd == null || toAdd.length() == 0) {
            // nothing to do
            return;
        }
        final String current = menuPopup.getSclass();
        if (current == null || current.length() == 0) {
            // just set it
            menuPopup.setSclass(toAdd);
            return;  // also do not execute final statement
        } else if (current.indexOf(toAdd) >= 0) {
            // nothing to do, already there!
            return;
        }
         // both are not null, merge them. We assume the class to add is a single token
        menuPopup.setSclass(current + " " + toAdd);
    }

    protected void setSelected(final ApplicationViewModel viewModel, final NaviGroupingViewModel naviGroups, Navi selectedNavi, String selectedGroup) {
        if (selectedGroup != null) {
            setNaviGroup(naviGroups, selectedGroup, true);  // used in the EE implementation
        } else if (selectedNavi != null) {
            viewModel.createComponents(selectedNavi);
            // this.selected = selected;  // value is never read, skipping assignment
            setNaviGroup(naviGroups, (selectedNavi).getCategory(), false);
        }
    }

    protected void setNaviGroup(final NaviGroupingViewModel naviGroups, String category, boolean isClosePermitted) {
        Integer groupIndex = applicationDAO.getGroupIndexByCategory(category, naviGroups);
        if (groupIndex != null) {
            if (!naviGroups.isGroupOpened(groupIndex)) {
                naviGroups.addOpenGroup(groupIndex.intValue());
            } else if (isClosePermitted && naviGroups.isGroupOpened(groupIndex)) {
                naviGroups.removeOpenGroup(groupIndex.intValue());
            }
        }
    }

    private Menu createMenu(String groupName, int groupIndex) {
        // String sclass = "header-nav-submenu no-scrollbar ";
        Menu menu = new Menu();
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
    protected boolean subtitleShouldDisplay(final NaviGroupingViewModel naviGroups, int index, int childIndex) {

        if (childIndex != 0) {
            if (naviGroups.getChild(index, childIndex).getSubcategory() != null
                    && naviGroups.getChild(index, childIndex).getSubcategory() != naviGroups
                            .getChild(index, childIndex - 1).getSubcategory()) {
                return true;
            } else {
                return false;
            }
        } else {
            return naviGroups.getChild(index, childIndex).getSubcategory() != null;
        }
    }

    protected final String getSubMenuClass(int childCount) {

        if (childCount > MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN) {
            int i = childCount / MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN;
            if (i > 1)
                return "header-nav-submenu-" + i + "c";
        }

        return "";
    }
}
