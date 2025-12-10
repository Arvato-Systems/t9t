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
package com.arvatosystems.t9t.zkui.converters.grid;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28DbFactory;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.util.ExceptionUtil;

/** Instances of this class are created via constructor. */
public class DropdownConverter implements IItemConverter<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropdownConverter.class);

    final String dropdown;
    final IDropdown28DbFactory<?> dropDownFactory;

    public DropdownConverter(final String dropdown, final IDropdown28DbFactory<?> dropDownFactory) {
        this.dropdown = dropdown;
        this.dropDownFactory = dropDownFactory;
    }

    @Override
    public String getFormattedLabel(String value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        try {
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
}
