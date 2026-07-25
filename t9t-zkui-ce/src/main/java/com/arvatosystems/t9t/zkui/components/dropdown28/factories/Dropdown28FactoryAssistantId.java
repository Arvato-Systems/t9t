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

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.ai.AiAssistantDTO;
import com.arvatosystems.t9t.ai.AiAssistantKey;
import com.arvatosystems.t9t.ai.AiAssistantRef;
import com.arvatosystems.t9t.ai.request.AiAssistantLeanSearchRequest;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.zkui.components.dropdown28.db.Dropdown28Db;

@Named("assistantId")
@Singleton
public class Dropdown28FactoryAssistantId implements IDropdown28DbFactory<AiAssistantRef> {

    @Override
    public String getDropdownId() {
        return "assistantId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new AiAssistantLeanSearchRequest();
    }

    @Override
    public AiAssistantRef createRef(Long ref) {
        return new AiAssistantRef(ref);
    }

    @Override
    public AiAssistantRef createKey(String id) {
        return new AiAssistantKey(id);
    }

    @Override
    public Dropdown28Db<AiAssistantRef> createInstance() {
        return new Dropdown28Db<AiAssistantRef>(this);
    }

    @Override
    public String getIdFromKey(AiAssistantRef key) {
        if (key instanceof AiAssistantKey aiAssistantKey)
            return aiAssistantKey.getAssistantId();
        if (key instanceof AiAssistantDTO aiAssistantDTO)
            return aiAssistantDTO.getAssistantId();
        return null;
    }
}
