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
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO;
import com.arvatosystems.t9t.ssm.SchedulerSetupKey;
import com.arvatosystems.t9t.ssm.SchedulerSetupRef;
import com.arvatosystems.t9t.ssm.request.LeanSchedulerSetupSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("schedulerSetupId")
@Singleton
public class Dropdown28FactorySchedulerSetup implements IDropdown28DbFactory<SchedulerSetupRef> {

    @Override
    public String getDropdownId() {
        return "schedulerSetupId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanSchedulerSetupSearchRequest();
    }

    @Override
    public SchedulerSetupRef createRef(Long ref) {
        return new SchedulerSetupRef(ref);
    }

    @Override
    public SchedulerSetupRef createKey(String id) {
        return new SchedulerSetupKey(id);
    }

    @Override
    public Dropdown28Db<SchedulerSetupRef> createInstance() {
        return new Dropdown28Db<SchedulerSetupRef>(this);
    }

    @Override
    public String getIdFromKey(SchedulerSetupRef key) {
        if (key instanceof SchedulerSetupKey)
            return ((SchedulerSetupKey)key).getSchedulerId();
        if (key instanceof SchedulerSetupDTO)
            return ((SchedulerSetupDTO)key).getSchedulerId();
        return null;
    }
}
