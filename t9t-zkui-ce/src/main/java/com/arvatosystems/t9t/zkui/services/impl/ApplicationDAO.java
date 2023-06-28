/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.services.impl;

import java.util.Iterator;

import com.arvatosystems.t9t.zkui.services.IApplicationDAO;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.viewmodel.beans.Navi;
import com.arvatosystems.t9t.zkui.viewmodel.support.NaviGroupingViewModel;

import de.jpaw.dp.Singleton;

/**
 * Menu DAO builds up the Menu from the property file.
 *
 * @author INCI02
 *
 */
@Singleton
public class ApplicationDAO implements IApplicationDAO {

    /*************************************************************************************
     * MENU related
    *************************************************************************************/

    @Override
    public final Navi getNavigationByLink(ApplicationSession as, String link) {
        Navi somenavis = null;
        for (Iterator<Navi> i = as.getAllNavigations().iterator(); i.hasNext();) {
            Navi tmp = i.next();
            if (tmp.getLink().equals(link)) {
                somenavis = tmp;
                return somenavis;
            }
        }
        return somenavis;
    }

    @Override
    public final Integer getGroupIndex(String link, NaviGroupingViewModel naviGroupingViewModel) {
        Integer  groupIndex = null;
        for (int groupIndexFlag = 0; groupIndexFlag < naviGroupingViewModel.getGroupCount(); groupIndexFlag++) {
            int childCount = naviGroupingViewModel.getChildCount(groupIndexFlag);
            for (int childIndexFlag = 0; childIndexFlag < childCount; childIndexFlag++) {
                Navi navi = naviGroupingViewModel.getChild(groupIndexFlag, childIndexFlag);
                if (navi.getLink().equals(link)) {
                    groupIndex = groupIndexFlag;
                    return groupIndex;
                }
            }

        }
        return groupIndex;
    }

    @Override
    public final Integer getGroupIndexByCategory(String category, NaviGroupingViewModel naviGroupingViewModel) {
        Integer  groupIndex = null;
        for (int groupIndexFlag = 0; groupIndexFlag < naviGroupingViewModel.getGroupCount(); groupIndexFlag++) {
            int childCount = naviGroupingViewModel.getChildCount(groupIndexFlag);
            for (int childIndexFlag = 0; childIndexFlag < childCount; childIndexFlag++) {
                Navi navi = naviGroupingViewModel.getChild(groupIndexFlag, childIndexFlag);
                if (navi.getCategory().equals(category)) {
                    groupIndex = groupIndexFlag;
                    return groupIndex;
                }
            }

        }
        return groupIndex;
    }
}
