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
package com.arvatosystems.t9t.uiprefs;

import java.util.ArrayList;
import java.util.List;

import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;

import de.jpaw.bonaparte.api.ColumnCollector;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.ui.UIColumn;
import de.jpaw.bonaparte.pojos.ui.UIColumnConfiguration;

public class ColumnSelection {
    public static List<String> getListOfFieldNames(List<UIColumnConfiguration> columns, boolean doSmartPrefixes, boolean onlyVisible) {
        int num = columns.size();
        if (onlyVisible) {
            // count again, to make the array only as big as required
            num = 0;
            for (UIColumnConfiguration c : columns)
                if (c.getVisible())
                    ++num;
        }
        List<String> selectedFields = new ArrayList<String>(num);

        // optionally prepend a field prefix
        for (UIColumnConfiguration c : columns)
            if (!onlyVisible || c.getVisible())
                selectedFields.add(doSmartPrefixes ? FieldMappers.addPrefix(c.getFieldName()) : c.getFieldName());
        return selectedFields;
    }
//
//    public static int countFields(BonaPortableClass<? extends BonaPortable> cls) {
//        int count = 0;
//        while (cls != null) {
//            count += cls.getMetaData().getFields().size();
//            cls = cls.getParent();
//        }
//        return count;
//    }


    /** Creates some initial grid preferences, which show all items contained in the dto and tracking. The tracking parameter is optional. */
    public static UIGridPreferences guessInitialConfig(BonaPortableClass<? extends BonaPortable> dtoClass, BonaPortableClass<? extends TrackingBase> trackingClass) {
        UIGridPreferences cfg = new UIGridPreferences();
        // int num = countFields(dtoClass) + countFields(trackingClass);           // this is just a first guess...
        // ArrayList<UIColumnConfiguration> columns = new ArrayList<UIColumnConfiguration>(num);

        ColumnCollector cc = new ColumnCollector();

        // first, add all the tracking columns, in case they exist
        if (trackingClass != null)
            cc.addToColumns(trackingClass.getMetaData());
        // then the DTO
        if (dtoClass != null)
            cc.addToColumns(dtoClass.getMetaData());

        // xfer the columns
        List<UIColumnConfiguration> cols = new ArrayList<UIColumnConfiguration>(cc.columns.size());
        for (UIColumn c : cc.columns)
            cols.add(new UIColumnConfiguration(c.getFieldName(), c.getWidth(), c.getAlignment(), c.getLayoutHint(), true, false, false, null, null, null, null));
        cfg.setColumns(cols);

        return cfg;
    }
}
