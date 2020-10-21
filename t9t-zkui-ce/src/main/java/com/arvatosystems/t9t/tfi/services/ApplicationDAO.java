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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arvatosystems.t9t.tfi.model.bean.Navi;
import com.arvatosystems.t9t.tfi.viewmodel.navigation.NaviGroupingViewModel;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

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

    /*
     * (non-Javadoc)
     * @see com.arvatosystems.t9t.tfi.model.services.IApplicationDAO#
     * getNavigationByhierarchy(int)
     */
    @Override
    public final List<Navi> getNavigationByHierarchy(ApplicationSession as, int hierarchy) {
        List<Navi> somenavis = new ArrayList<Navi>();
        for (Iterator<Navi> i = as.getAllNavigations().iterator(); i.hasNext();) {
            Navi tmp = i.next();
            if (tmp.getHierarchy() == hierarchy) {
                somenavis.add(tmp);
            }
        }
        return somenavis;
    }

    @Override
    public final Navi getNavigationByLink(ApplicationSession as, String link) {
        Navi somenavis = null;
        for (Iterator<Navi> i = as.getAllNavigations().iterator(); i.hasNext();) {
            Navi tmp = i.next();
            if (tmp.getLink().equals(link)) {
                somenavis=tmp;
                return somenavis;
            }
        }
        return somenavis;
    }

    @Override
    public final Integer getGroupIndex(String link, NaviGroupingViewModel naviGroupingViewModel) {
        Integer  groupIndex = null;
        for (int groupIndexFlag=0; groupIndexFlag<naviGroupingViewModel.getGroupCount(); groupIndexFlag++) {
            int childCount=naviGroupingViewModel.getChildCount(groupIndexFlag);
            for (int childIndexFlag=0; childIndexFlag<childCount; childIndexFlag++) {
                Navi navi =naviGroupingViewModel.getChild(groupIndexFlag, childIndexFlag);
                if (navi.getLink().equals(link)) {
                    groupIndex=groupIndexFlag;
                    return groupIndex;
                }
            }

        }
        return groupIndex;
    }

    @Override
    public final Integer getGroupIndexByCategory(String category, NaviGroupingViewModel naviGroupingViewModel) {
        Integer  groupIndex = null;
        for (int groupIndexFlag=0; groupIndexFlag<naviGroupingViewModel.getGroupCount(); groupIndexFlag++) {
            int childCount=naviGroupingViewModel.getChildCount(groupIndexFlag);
            for (int childIndexFlag=0; childIndexFlag<childCount; childIndexFlag++) {
                Navi navi =naviGroupingViewModel.getChild(groupIndexFlag, childIndexFlag);
                if (navi.getCategory().equals(category)) {
                    groupIndex=groupIndexFlag;
                    return groupIndex;
                }
            }

        }
        return groupIndex;
    }
}
