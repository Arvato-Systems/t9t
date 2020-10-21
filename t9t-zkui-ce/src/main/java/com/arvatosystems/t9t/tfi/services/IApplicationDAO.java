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
package com.arvatosystems.t9t.tfi.services;

import java.util.List;

import com.arvatosystems.t9t.tfi.model.bean.Navi;
import com.arvatosystems.t9t.tfi.viewmodel.navigation.NaviGroupingViewModel;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

public interface IApplicationDAO {

    /**
     *Returns all MenuItems by hierarchy.
     * @param hierarchy .
     * @return List<Navi>
     */
    public abstract List<Navi> getNavigationByHierarchy(ApplicationSession as, int hierarchy);

    public abstract Navi getNavigationByLink(ApplicationSession as, String link);
    public abstract Integer getGroupIndex(String link, NaviGroupingViewModel naviGroupingViewModel);
    public abstract Integer getGroupIndexByCategory(String category, NaviGroupingViewModel naviGroupingViewModel);

}
