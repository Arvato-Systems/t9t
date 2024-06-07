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
package com.arvatosystems.t9t.ai;

import com.arvatosystems.t9t.ai.request.AiAssistantCrudRequest;
import com.arvatosystems.t9t.ai.request.AiAssistantSearchRequest;
import com.arvatosystems.t9t.ai.request.AiConversationCrudRequest;
import com.arvatosystems.t9t.ai.request.AiConversationSearchRequest;
import com.arvatosystems.t9t.ai.request.AiUserStatusCrudRequest;
import com.arvatosystems.t9t.ai.request.AiUserStatusSearchRequest;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;

public final class T9tAIModels implements IViewModelContainer {

    private static final CrudViewModel<AiAssistantDTO, FullTrackingWithVersion> AI_ASSISTANT_VIEW_MODEL = new CrudViewModel<>(
        AiAssistantDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        AiAssistantSearchRequest.BClass.INSTANCE,
        AiAssistantCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<AiConversationDTO, FullTrackingWithVersion> AI_CONVERSATION_VIEW_MODEL = new CrudViewModel<>(
        AiConversationDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        AiConversationSearchRequest.BClass.INSTANCE,
        AiConversationCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<AiUserStatusDTO, FullTrackingWithVersion> AI_USER_STATUS_VIEW_MODEL = new CrudViewModel<>(
        AiUserStatusDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        AiUserStatusSearchRequest.BClass.INSTANCE,
        AiUserStatusCrudRequest.BClass.INSTANCE);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("aiAssistant",       AI_ASSISTANT_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("aiConversation",    AI_CONVERSATION_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("aiUserStatus",      AI_USER_STATUS_VIEW_MODEL);
    }
}
