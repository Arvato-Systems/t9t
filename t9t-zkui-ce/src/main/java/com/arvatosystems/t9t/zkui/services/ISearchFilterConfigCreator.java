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
package com.arvatosystems.t9t.zkui.services;

import java.util.List;

import org.zkoss.zul.Div;

import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.zkui.viewmodel.support.SearchFilterRowVM;

import de.jpaw.bonaparte.pojos.ui.UIFilter;

public interface ISearchFilterConfigCreator {

    void createComponent(Div parent, UIGridPreferences uiGridPreferences, List<UIFilter> selectedFilters);

    List<SearchFilterRowVM> getSelectedFilters();

}
