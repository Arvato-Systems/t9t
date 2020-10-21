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
package com.arvatosystems.t9t.tfi.viewmodel.navigation;

import java.io.Serializable;
import java.util.Comparator;
import org.zkoss.zul.GroupComparator;

import com.arvatosystems.t9t.tfi.model.bean.Navi;

/**
 * Menu related.
 * @author INCI02
 *
 */
public class NaviComparator implements Comparator<Navi>, GroupComparator<Navi>,
        Serializable {
    private static final long serialVersionUID = -5442923541968897269L;


    @Override
    public final int compare(Navi o1, Navi o2) {
        return o1.getCategory().compareTo(o2.getCategory().toString());
    }

    @Override
    public final int compareGroup(Navi o1, Navi o2) {
        if (o1.getCategory().equals(o2.getCategory())) {
            return 0;
        }
        else{
            return 1;
        }
    }

}
