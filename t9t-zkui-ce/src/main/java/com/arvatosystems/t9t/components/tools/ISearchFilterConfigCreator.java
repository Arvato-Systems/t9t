package com.arvatosystems.t9t.components.tools;

import java.util.List;

import org.zkoss.zul.Div;

import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;

import de.jpaw.bonaparte.pojos.ui.UIFilter;

public interface ISearchFilterConfigCreator {

    void createComponent(Div parent, UIGridPreferences uiGridPreferences, List<UIFilter> selectedFilters);

    List<SearchFilterRowVM> getSelectedFilters();

}
