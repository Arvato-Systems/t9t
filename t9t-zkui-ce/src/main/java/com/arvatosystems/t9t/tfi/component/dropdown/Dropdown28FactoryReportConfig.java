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
import com.arvatosystems.t9t.rep.ReportConfigDTO;
import com.arvatosystems.t9t.rep.ReportConfigKey;
import com.arvatosystems.t9t.rep.ReportConfigRef;
import com.arvatosystems.t9t.rep.request.LeanReportConfigSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("reportConfigId")
@Singleton
public class Dropdown28FactoryReportConfig implements IDropdown28DbFactory<ReportConfigRef> {

    @Override
    public String getDropdownId() {
        return "reportConfigId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanReportConfigSearchRequest();
    }

    @Override
    public ReportConfigRef createRef(Long ref) {
        return new ReportConfigRef(ref);
    }

    @Override
    public ReportConfigRef createKey(String id) {
        return new ReportConfigKey(id);
    }

    @Override
    public Dropdown28Db<ReportConfigRef> createInstance() {
        return new Dropdown28Db<ReportConfigRef>(this);
    }

    @Override
    public String getIdFromKey(ReportConfigRef key) {
        if (key instanceof ReportConfigKey)
            return ((ReportConfigKey) key).getReportConfigId();
        if (key instanceof ReportConfigDTO)
            return ((ReportConfigDTO) key).getReportConfigId();
        return null;
    }
}
