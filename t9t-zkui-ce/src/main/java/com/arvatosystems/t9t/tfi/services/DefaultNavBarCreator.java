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
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
@Fallback
public class DefaultNavBarCreator implements INavBarCreator {

    private final int MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN = 13;
    private final IApplicationDAO applicationDAO = Jdp.getRequired(IApplicationDAO.class);
    private final ApplicationSession as = ApplicationSession.get();
    protected NaviGroupingViewModel naviGroups = null;
    protected Object selected;
    protected ApplicationViewModel viewModel;

    @Override
    public void createNavBar(ApplicationViewModel viewModel, Component container, NaviGroupingViewModel naviGroups) {
        this.naviGroups = naviGroups;
        this.viewModel = viewModel;
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
            menuPopup.addSclass("nav-menupopup");
            menuPopup.addSclass(getSubMenuClass(naviGroups.getChildCount(groupIndex)));
            menu.appendChild(menuPopup);

            for (int childIndex = 0; childIndex < naviGroups.getChildCount(groupIndex); childIndex++) {
                Navi navi = naviGroups.getChild(groupIndex, childIndex);
                // Display grouped subtitle (non clickable)
                if (subtitleShouldDisplay(groupIndex, childIndex)) {
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
                        setSelected(navi);
                    });
                    menuItem.setImage(navi.getImg());
                    menuItem.setClientAttribute("onClick",
                            "collapseHeaderMenu(); setNavi('" + groupName + "','" + navi.getNaviId() + "');");
                    menuItem.setClientAttribute("data-navi", navi.getNaviId());
                    menuPopup.appendChild(menuItem);
                }
            }
        }
    }

    private void setSelected(Object selected) {
        if (selected instanceof String) {
            setNaviGroup(String.valueOf(selected), true);
        } else {
            viewModel.createComponents((Navi) selected);
            this.selected = selected;
            setNaviGroup(((Navi) selected).getCategory(), false);
        }
    }

    private void setNaviGroup(String category, boolean isClosePermitted) {
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
    public boolean subtitleShouldDisplay(int index, int childIndex) {

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

    public final String getSubMenuClass(int childCount) {

        if (childCount > MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN) {
            int i = childCount / MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN;
            if (i > 1)
                return "header-nav-submenu-" + i + "c";
        }

        return "";
    }
}
