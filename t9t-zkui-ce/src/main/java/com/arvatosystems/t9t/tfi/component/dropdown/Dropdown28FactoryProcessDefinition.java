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
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionKey;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionRef;
import com.arvatosystems.t9t.bpmn.request.LeanProcessDefinitionSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("processDefinitionId")
@Singleton
public class Dropdown28FactoryProcessDefinition implements IDropdown28DbFactory<ProcessDefinitionRef> {

    @Override
    public String getDropdownId() {
        return "processDefinitionId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanProcessDefinitionSearchRequest();
    }

    @Override
    public ProcessDefinitionRef createRef(Long ref) {
        return new ProcessDefinitionRef(ref);
    }

    @Override
    public ProcessDefinitionKey createKey(String id) {
        return new ProcessDefinitionKey(id);
    }

    @Override
    public Dropdown28Db<ProcessDefinitionRef> createInstance() {
        return new Dropdown28Db<ProcessDefinitionRef>(this);
    }

    @Override
    public String getIdFromKey(ProcessDefinitionRef key) {
        if (key instanceof ProcessDefinitionKey)
            return ((ProcessDefinitionKey)key).getProcessDefinitionId();
        if (key instanceof ProcessDefinitionDTO)
            return ((ProcessDefinitionDTO)key).getProcessDefinitionId();
        return null;
    }
}
