/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.ai.jpa.persistence.impl

import com.arvatosystems.t9t.ai.AiAssistantRef
import com.arvatosystems.t9t.ai.AiChatLogRef
import com.arvatosystems.t9t.ai.AiConversationRef
import com.arvatosystems.t9t.ai.AiUserStatusRef
import com.arvatosystems.t9t.ai.jpa.entities.AiAssistantEntity
import com.arvatosystems.t9t.ai.jpa.entities.AiChatLogEntity
import com.arvatosystems.t9t.ai.jpa.entities.AiConversationEntity
import com.arvatosystems.t9t.ai.jpa.entities.AiModuleCfgEntity
import com.arvatosystems.t9t.ai.jpa.entities.AiUserStatusEntity
import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.active.AutoResolver42
import java.util.List

@AutoResolver42
class AiResolvers {
    @AllCanAccessGlobalTenant
    def AiModuleCfgEntity              getAiModuleCfgEntity(String id, boolean onlyActive) { return null; }

    def AiUserStatusEntity             getAiUserStatusEntity(AiUserStatusRef entityRef, boolean onlyActive) { return null; }
    def AiAssistantEntity              getAiAssistantEntity(AiAssistantRef entityRef, boolean onlyActive) { return null; }
    def AiConversationEntity           getAiConversationEntity(AiConversationRef entityRef, boolean onlyActive) { return null; }
    def AiChatLogEntity                getAiChatLogEntity(AiChatLogRef entityRef, boolean onlyActive) { return null; }
    def List<AiConversationEntity>     findByUserIdAndSessionRef(boolean onlyActive, String userId, Long createdBySessionRef) { return null; }
    def List<AiUserStatusEntity>       findByUserId(boolean onlyActive, String userId) { return null; }
    def List<AiAssistantEntity>        findByAllAssistants(boolean onlyActive) { return null; }
}
