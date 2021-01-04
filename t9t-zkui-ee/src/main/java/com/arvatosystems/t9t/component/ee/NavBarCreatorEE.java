package com.arvatosystems.t9t.component.ee;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkmax.zul.Nav;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menuitem;

import com.arvatosystems.t9t.tfi.model.bean.Navi;
import com.arvatosystems.t9t.tfi.services.DefaultNavBarCreator;
import com.arvatosystems.t9t.tfi.services.INavBarCreator;
import com.arvatosystems.t9t.tfi.viewmodel.ApplicationViewModel;
import com.arvatosystems.t9t.tfi.viewmodel.navigation.NaviGroupingViewModel;

import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;

@Singleton
@Specializes
public class NavBarCreatorEE extends DefaultNavBarCreator implements INavBarCreator {

    @Override
    public void createNavBar(ApplicationViewModel viewModel, Component container, NaviGroupingViewModel naviGroups) {
        int groupCounts = naviGroups.getGroupCount();
        Navbar navbar = new Navbar("horizontal");
        navbar.setCollapsed(false);
        container.appendChild(navbar);
        createContextMenu(container);

        for (int groupIndex = 0; groupIndex < groupCounts; groupIndex++) {
            String groupName = naviGroups.getGroup(groupIndex);
            final Nav nav = new Nav();
            int childCounts = naviGroups.getChildCount(groupIndex);
            nav.setLabel(groupName);
            nav.addSclass("header-nav-submenu no-scrollbar ");
            nav.addSclass(getSubMenuClass(childCounts));
            createContextMenuOnEachMenu(nav);
            navbar.appendChild(nav);

            for (int childIndex = 0; childIndex < childCounts; childIndex++) {
                Navi navi = naviGroups.getChild(groupIndex, childIndex);
                // Display grouped subtitle (non clickable)
                if (subtitleShouldDisplay(naviGroups, groupIndex, childIndex)) {
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
                    nav.appendChild(navitem);
                }
            }
        }
    }

    private final void createContextMenuOnEachMenu(Nav nav) {
        nav.addEventListener(Events.ON_OPEN, ev -> {
            for (Component comp2 : ev.getTarget().getChildren()) {
                if (comp2 instanceof Menuitem) {
                    ((Navitem) comp2).setContext(CONTEXT_MENU_ID);
                }
            }
        });
    }
}
