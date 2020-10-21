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
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.AsyncQueueKey;
import com.arvatosystems.t9t.io.AsyncQueueRef;
import com.arvatosystems.t9t.io.request.LeanAsyncQueueSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("asyncQueueId")
@Singleton
public class Dropdown28FactoryAsyncQueue implements IDropdown28DbFactory<AsyncQueueRef> {

    @Override
    public String getDropdownId() {
        return "asyncQueueId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanAsyncQueueSearchRequest();
    }

    @Override
    public AsyncQueueRef createRef(Long ref) {
        return new AsyncQueueRef(ref);
    }

    @Override
    public AsyncQueueRef createKey(String id) {
        return new AsyncQueueKey(id);
    }

    @Override
    public Dropdown28Db<AsyncQueueRef> createInstance() {
        return new Dropdown28Db<AsyncQueueRef>(this);
    }

    @Override
    public String getIdFromKey(AsyncQueueRef key) {
        if (key instanceof AsyncQueueKey)
            return ((AsyncQueueKey)key).getAsyncQueueId();
        if (key instanceof AsyncQueueDTO)
            return ((AsyncQueueDTO)key).getAsyncQueueId();
        return null;
    }
}
