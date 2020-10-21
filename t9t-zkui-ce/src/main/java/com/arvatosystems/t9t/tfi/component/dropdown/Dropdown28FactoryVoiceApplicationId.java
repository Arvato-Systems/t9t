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
import com.arvatosystems.t9t.voice.VoiceApplicationDTO;
import com.arvatosystems.t9t.voice.VoiceApplicationKey;
import com.arvatosystems.t9t.voice.VoiceApplicationRef;
import com.arvatosystems.t9t.voice.request.LeanVoiceApplicationSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("applicationId")
@Singleton
public class Dropdown28FactoryVoiceApplicationId  implements IDropdown28DbFactory<VoiceApplicationRef> {

    @Override
    public String getDropdownId() {
        return "applicationId";
    }

    @Override
    public Dropdown28Db<VoiceApplicationRef> createInstance() {
        return new Dropdown28Db<VoiceApplicationRef>(this);
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanVoiceApplicationSearchRequest();
    }

    @Override
    public VoiceApplicationRef createRef(Long ref) {
        return new VoiceApplicationRef(ref);
    }

    @Override
    public VoiceApplicationRef createKey(String id) {
        return new VoiceApplicationKey(id);
    }

    @Override
    public String getIdFromKey(VoiceApplicationRef key) {
        if (key instanceof VoiceApplicationKey)
            return ((VoiceApplicationKey) key).getApplicationId();
        if (key instanceof VoiceApplicationDTO)
            return ((VoiceApplicationDTO) key).getApplicationId();
        return null;
    }

}
