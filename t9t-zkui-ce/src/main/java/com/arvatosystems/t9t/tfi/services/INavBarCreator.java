package com.arvatosystems.t9t.tfi.services;

import org.zkoss.zk.ui.Component;

import com.arvatosystems.t9t.tfi.viewmodel.ApplicationViewModel;
import com.arvatosystems.t9t.tfi.viewmodel.navigation.NaviGroupingViewModel;

/** Provides a method to create the navigation bar in the main menu (home). */
public interface INavBarCreator {
    void createNavBar(ApplicationViewModel viewModel, Component container, NaviGroupingViewModel naviGroups);
}
