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
package com.arvatosystems.t9t.zkui.converters.grid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28DbFactory;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

@Singleton
@Named("dropdown")
public class DropdownConverter implements IItemConverter<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropdownConverter.class);
    @SuppressWarnings("rawtypes")
    private static final Map<String, IDropdown28DbFactory> FACTORIES = new HashMap<String, IDropdown28DbFactory>();

    private String getLabelByValue(String value, String dropdown) {
        if (value == null)
            return "";
        try {
            // Explicit store null results in order avoid jdp call repeatedly for same name
            if (!FACTORIES.containsKey(dropdown))
                FACTORIES.put(dropdown, Jdp.getOptional(IDropdown28DbFactory.class, dropdown));

            @SuppressWarnings("rawtypes")
            IDropdown28DbFactory dropDownFactory = FACTORIES.get(dropdown);
            if (dropDownFactory == null)
                return value;

            // the below call caches dropdowns, we do not do a separate backend call per row.
            List<Description> data = ApplicationSession.get().getDropDownData(dropdown, dropDownFactory.getSearchRequest());
            for (Description d : data) {
                if (value.equals(d.getId()) && d.getIsActive())
                    return d.getId() + " - " + d.getName(); // replace id by label (id name)
            }
            // not found
            return "(" + value + ")";
        } catch (Exception e) {
            LOGGER.error("Could not retrieve dropdown list: {}", ExceptionUtil.causeChain(e));
            return ">" + value + "<";
        }
    }

    @Override
    public String getFormattedLabel(String value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        if (meta.getProperties() != null && meta.getProperties().containsKey("dropdown")) {
            return getLabelByValue(value, meta.getProperties().get("dropdown"));
        }
        return value;
    }
}
