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
package com.arvatosystems.t9t.zkui.components.dropdown28.factories;

import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.doc.MailingGroupDTO;
import com.arvatosystems.t9t.doc.MailingGroupKey;
import com.arvatosystems.t9t.doc.MailingGroupRef;
import com.arvatosystems.t9t.doc.request.LeanMailingGroupSearchRequest;
import com.arvatosystems.t9t.zkui.components.dropdown28.db.Dropdown28Db;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("mailGroupId")
@Singleton
public class Dropdown28FactoryMailingGroup implements IDropdown28DbFactory<MailingGroupRef> {
    @Override
    public String getDropdownId() {
        return "mailGroupId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanMailingGroupSearchRequest();
    }

    @Override
    public MailingGroupRef createRef(Long ref) {
        return new MailingGroupRef(ref);
    }

    @Override
    public MailingGroupKey createKey(String id) {
        return new MailingGroupKey(id);
    }

    @Override
    public Dropdown28Db<MailingGroupRef> createInstance() {
        return new Dropdown28Db<MailingGroupRef>(this);
    }

    @Override
    public String getIdFromKey(MailingGroupRef key) {
        if (key instanceof MailingGroupKey)
            return ((MailingGroupKey)key).getMailingGroupId();
        if (key instanceof MailingGroupDTO)
            return ((MailingGroupDTO)key).getMailingGroupId();
        return null;
    }
}
