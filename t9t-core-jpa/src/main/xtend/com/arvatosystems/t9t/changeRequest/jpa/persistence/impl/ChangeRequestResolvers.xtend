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
package com.arvatosystems.t9t.changeRequest.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.active.AutoResolver42
import com.arvatosystems.t9t.changeRequest.DataChangeRequestRef
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigRef
import com.arvatosystems.t9t.changeRequest.jpa.entities.DataChangeRequestEntity
import com.arvatosystems.t9t.changeRequest.jpa.entities.ChangeWorkFlowConfigEntity

@AutoResolver42
class ChangeRequestResolvers {

    def DataChangeRequestEntity         getDataChangeRequestEntity(DataChangeRequestRef entityRef) { return null; }
    def ChangeWorkFlowConfigEntity      getChangeWorkFlowConfigEntity(ChangeWorkFlowConfigRef entityRef) { return null; }

    def ChangeWorkFlowConfigEntity      findByPqon(boolean onlyActive, String pqon) { return null; }
}
