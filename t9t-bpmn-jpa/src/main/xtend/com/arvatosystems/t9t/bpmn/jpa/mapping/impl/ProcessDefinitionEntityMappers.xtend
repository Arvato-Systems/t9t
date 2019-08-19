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
package com.arvatosystems.t9t.bpmn.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO
import com.arvatosystems.t9t.bpmn.ProcessDefinitionKey
import com.arvatosystems.t9t.bpmn.jpa.entities.ProcessDefinitionEntity
import com.arvatosystems.t9t.bpmn.jpa.persistence.IProcessDefinitionEntityResolver

@AutoMap42
public class ProcessDefinitionEntityMappers {
    IProcessDefinitionEntityResolver processDefinitionResolver

    @AutoHandler("SP42")
    def void e2dProcessDefinitionDTO(ProcessDefinitionEntity entity, ProcessDefinitionDTO dto) {}
    def void e2dProcessDefinitionKey(ProcessDefinitionEntity entity, ProcessDefinitionKey dto) {}
}
