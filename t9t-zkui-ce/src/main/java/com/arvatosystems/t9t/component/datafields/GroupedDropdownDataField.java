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
package com.arvatosystems.t9t.component.datafields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.tfi.component.dropdown.GroupedDropdown28Db;
import com.arvatosystems.t9t.tfi.component.dropdown.IGroupedDropdown28DbFactory;

import de.jpaw.bonaparte.enums.BonaEnum;
import de.jpaw.bonaparte.pojos.apiw.Ref;

public class GroupedDropdownDataField extends AbstractDataField<GroupedDropdown28Db<Ref>, Ref>{

    private final Logger LOGGER = LoggerFactory.getLogger(GroupedDropdownDataField.class);

    private final GroupedDropdown28Db<Ref> c;
    private final IGroupedDropdown28DbFactory<Ref> factory;
    private Ref retryRef;

    public GroupedDropdownDataField(DataFieldParameters params, String dropdownType, IGroupedDropdown28DbFactory<Ref> dbFactory) {
        super(params);
        factory = dbFactory;
        c = dbFactory.createInstance();
        setConstraints(c, null);
    }

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public GroupedDropdown28Db<Ref> getComponent() {
        return c;
    }

    @Override
    public Ref getValue() {
        String res1 = c.getValue();
        Comboitem res = c.getSelectedItem();

        LOGGER.debug("getValue({}) called, value is {}, item is {}: {}", getFieldName(), res1,
                res == null ? "NULL" : res.getClass().getCanonicalName(), res);
        if (res1 == null)
            return null;
        Description desc = c.lookupById(res1);
        return desc == null ? null : factory.createRef(desc.getObjectRef());
    }

    @Override
    public void setValue(Ref data) {
        Description desc = data == null ? null : c.lookupByRef(data.getObjectRef());
        retryRef = desc == null ? data : null;
        LOGGER.debug("{}.setValue(): setting {} results in {}", getFieldName(), data, desc);
        c.setValue(desc == null ? null : desc.getId());
    }

    public void setGroup(Object object) {
        if (object == null) {
            setGroupValue(null);
        } else if (object instanceof Ref) {
            Ref ref = (Ref) object;
            setGroupValue(ref.getObjectRef());
        } else if (object instanceof Long) {
            setGroupValue((Long) object);
        } else if (object instanceof Boolean) {
            setGroup(((Boolean) object).booleanValue());
        } else if (object instanceof Integer) {
            setGroup(((Integer) object).longValue());
        } else if (object instanceof BonaEnum) {
            setGroup(Long.valueOf(((BonaEnum) object).ordinal()));
        } else {
            LOGGER.debug("Unsupported type {} for group, only Integer, Long, Boolean or Ref is supported. ", object);
        }
    }

    public void setGroup(boolean data) {
        Long value = data ? 1l : 0l;
        setGroupValue(value);
    }

    public void setGroup(int data) {
        Long value = Long.valueOf(data);
        setGroupValue(value);
    }

    /**
     * Reload the component selections if the group has changed
     * @param group
     */
    private void setGroupValue(Long ref) {
        // only reload if group is changed
        if (ref == null && c.getGroup() != null || ref != null && !ref.equals(c.getGroup())) {
            c.setGroup(ref);
            c.reloadDropDownData();
            // check if retry required
            if (retryRef != null) {
                setValue(retryRef);
            }
        }
    }
}
