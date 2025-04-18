/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.zul.GroupsModelArray;

import com.arvatosystems.t9t.zkui.viewmodel.beans.Navi;

/**
 *
 * @author INCI02
 *
 */
public class NaviGroupingViewModel extends GroupsModelArray<Navi, String, String, Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NaviGroupingViewModel.class);


    private static final long serialVersionUID = 1L;
    private boolean showGroup;

    public NaviGroupingViewModel(List<Navi> data, Comparator<Navi> cmpr, boolean showGroup) {
        super(data.toArray(new Navi[0]), cmpr);
        this.showGroup = showGroup;
    }

    @Override
    protected final String createGroupHead(Navi[] groupdata, int index, int col) {
        String ret = "";
        if (groupdata.length > 0) {
            ret = groupdata[0].getCategory();
        }

        return ret;
    }


    @Override
    public final boolean hasGroupfoot(int groupIndex) {
        boolean retBool = false;

        if (showGroup) {
            retBool = super.hasGroupfoot(groupIndex);
        }

        return retBool;
    }

    /**
     * refresh testing
     */
    @GlobalCommand("refresh")
    public final void refresh() {
        LOGGER.debug("refresh");
    }
}
