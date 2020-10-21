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

import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.rep.ReportMailingDTO;
import com.arvatosystems.t9t.rep.ReportMailingKey;
import com.arvatosystems.t9t.rep.ReportMailingRef;
import com.arvatosystems.t9t.rep.request.LeanReportMailingSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("mailGroupId")
@Singleton
public class Dropdown28FactoryReportMailing implements IDropdown28DbFactory<ReportMailingRef> {

    @Override
    public String getDropdownId() {
        return "mailGroupId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanReportMailingSearchRequest();
    }

    @Override
    public ReportMailingRef createRef(Long ref) {
        return new ReportMailingRef(ref);
    }

    @Override
    public ReportMailingKey createKey(String id) {
        return new ReportMailingKey(id);
    }

    @Override
    public Dropdown28Db<ReportMailingRef> createInstance() {
        return new Dropdown28Db<ReportMailingRef>(this);
    }

    @Override
    public String getIdFromKey(ReportMailingRef key) {
        if (key instanceof ReportMailingKey)
            return ((ReportMailingKey)key).getMailingGroupId();
        if (key instanceof ReportMailingDTO)
            return ((ReportMailingDTO)key).getMailingGroupId();
        return null;
    }
}
