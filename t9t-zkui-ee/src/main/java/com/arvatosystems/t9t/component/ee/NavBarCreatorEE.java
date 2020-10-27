package com.arvatosystems.t9t.component.ee;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkmax.zul.Nav;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;

import com.arvatosystems.t9t.tfi.model.bean.Navi;
import com.arvatosystems.t9t.tfi.services.IApplicationDAO;
import com.arvatosystems.t9t.tfi.services.INavBarCreator;
import com.arvatosystems.t9t.tfi.viewmodel.ApplicationViewModel;
import com.arvatosystems.t9t.tfi.viewmodel.navigation.NaviGroupingViewModel;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;

@Singleton
@Specializes
public class NavBarCreatorEE implements INavBarCreator {
    private final int MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN = 13;
    private final IApplicationDAO applicationDAO = Jdp.getRequired(IApplicationDAO.class);
    protected NaviGroupingViewModel naviGroups = null;
    protected Object selected;
    protected ApplicationViewModel viewModel;

    @Override
    public void createNavBar(ApplicationViewModel viewModel, Component container, NaviGroupingViewModel naviGroups) {
        this.naviGroups = naviGroups;
        this.viewModel = viewModel;
        int groupCounts = naviGroups.getGroupCount();
        Navbar navbar = new Navbar("horizontal");
        navbar.setCollapsed(false);
        container.appendChild(navbar);

        for (int groupIndex = 0; groupIndex < groupCounts; groupIndex++) {
            String groupName = naviGroups.getGroup(groupIndex);
            Nav nav = createNav(groupName, groupIndex);
            navbar.appendChild(nav);

            for (int childIndex = 0; childIndex < naviGroups.getChildCount(groupIndex); childIndex++) {
                Navi navi = naviGroups.getChild(groupIndex, childIndex);
                // Display grouped subtitle (non clickable)
                if (subtitleShouldDisplay(groupIndex, childIndex)) {
                    Navitem navitem = new Navitem();
                    navitem.setLabel(navi.getSubcategory());
                    navitem.setDisabled(true);
                    navitem.setZclass("header-nav-subtitle");
                    navitem.setImage(navi.getImg());
                    nav.appendChild(navitem);
                }

                // Menu items
                if (navi.isMenuItemVisible()) {
                    Navitem navitem = new Navitem();
                    navitem.setLabel(navi.getName());
                    navitem.setSelected(selected == navi);
                    navitem.setAttribute("navi", navi);
                    navitem.addEventListener(Events.ON_CLICK, (ev) -> {
                        setSelected(navi);
                        navbar.setSelectedItem(navitem);
                    });
                    navitem.setImage(navi.getImg());
                    navitem.setClientAttribute("onClick", "collapseHeaderMenu();");
                    navitem.setClientAttribute("data-navi", navi.getNaviId());
                    nav.appendChild(navitem);
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

    private Nav createNav(String groupName, int groupIndex) {
        Nav nav = new Nav();
        nav.setLabel(groupName);
        nav.addSclass("header-nav-submenu no-scrollbar ");
        return nav;
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
