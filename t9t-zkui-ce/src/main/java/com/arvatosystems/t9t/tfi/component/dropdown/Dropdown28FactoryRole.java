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
package com.arvatosystems.t9t.tfi.component.dropdown;

import com.arvatosystems.t9t.auth.RoleDTO;
import com.arvatosystems.t9t.auth.RoleKey;
import com.arvatosystems.t9t.auth.RoleRef;
import com.arvatosystems.t9t.auth.request.LeanRoleSearchRequest;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("roleId")
@Singleton
public class Dropdown28FactoryRole implements IDropdown28DbFactory<RoleRef> {

    @Override
    public String getDropdownId() {
        return "roleId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanRoleSearchRequest();
    }

    @Override
    public RoleRef createRef(Long ref) {
        return new RoleRef(ref);
    }

    @Override
    public RoleRef createKey(String id) {
        return new RoleKey(id);
    }

    @Override
    public Dropdown28Db<RoleRef> createInstance() {
        return new Dropdown28Db<RoleRef>(this);
    }

    @Override
    public String getIdFromKey(RoleRef key) {
        if (key instanceof RoleKey)
            return ((RoleKey)key).getRoleId();
        if (key instanceof RoleDTO)
            return ((RoleDTO)key).getRoleId();
        return null;
    }
}
