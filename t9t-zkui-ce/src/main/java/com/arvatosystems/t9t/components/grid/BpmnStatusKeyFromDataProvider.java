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
package com.arvatosystems.t9t.components.grid;

import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("bpmnStatus")
public class BpmnStatusKeyFromDataProvider implements IKeyFromDataProvider<ProcessExecutionStatusDTO, TrackingBase> {

    @Override
    public SearchFilter getFilterForKey(DataWithTracking<ProcessExecutionStatusDTO, TrackingBase> dwt) {
        final LongFilter targetFilter = new LongFilter("targetObjectRef");
        targetFilter.setEqualsValue(dwt.getData().getTargetObjectRef());

        final UnicodeFilter processIdFilter = new UnicodeFilter("processDefinitionId");
        processIdFilter.setEqualsValue(dwt.getData().getProcessDefinitionId());

        return SearchFilters.and(targetFilter, processIdFilter);
    }
}
