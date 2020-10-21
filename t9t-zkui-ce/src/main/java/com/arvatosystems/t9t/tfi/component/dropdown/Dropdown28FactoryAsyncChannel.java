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
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncChannelKey;
import com.arvatosystems.t9t.io.AsyncChannelRef;
import com.arvatosystems.t9t.io.request.LeanAsyncChannelSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("asyncChannelId")
@Singleton
public class Dropdown28FactoryAsyncChannel implements IDropdown28DbFactory<AsyncChannelRef> {

    @Override
    public String getDropdownId() {
        return "asyncChannelId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanAsyncChannelSearchRequest();
    }

    @Override
    public AsyncChannelRef createRef(Long ref) {
        return new AsyncChannelRef(ref);
    }

    @Override
    public AsyncChannelRef createKey(String id) {
        return new AsyncChannelKey(id);
    }

    @Override
    public Dropdown28Db<AsyncChannelRef> createInstance() {
        return new Dropdown28Db<AsyncChannelRef>(this);
    }

    @Override
    public String getIdFromKey(AsyncChannelRef key) {
        if (key instanceof AsyncChannelKey)
            return ((AsyncChannelKey)key).getAsyncChannelId();
        if (key instanceof AsyncChannelDTO)
            return ((AsyncChannelDTO)key).getAsyncChannelId();
        return null;
    }
}
