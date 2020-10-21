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
import com.arvatosystems.t9t.core.CannedRequestDTO;
import com.arvatosystems.t9t.core.CannedRequestKey;
import com.arvatosystems.t9t.core.CannedRequestRef;
import com.arvatosystems.t9t.core.request.LeanCannedRequestSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("cannedRequestId")
@Singleton
public class Dropdown28FactoryCannedRequest implements IDropdown28DbFactory<CannedRequestRef> {

    @Override
    public String getDropdownId() {
        return "cannedRequestId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanCannedRequestSearchRequest();
    }

    @Override
    public CannedRequestRef createRef(Long ref) {
        return new CannedRequestRef(ref);
    }

    @Override
    public CannedRequestKey createKey(String id) {
        return new CannedRequestKey(id);
    }

    @Override
    public Dropdown28Db<CannedRequestRef> createInstance() {
        return new Dropdown28Db<CannedRequestRef>(this);
    }

    @Override
    public String getIdFromKey(CannedRequestRef key) {
        if (key instanceof CannedRequestKey)
            return ((CannedRequestKey)key).getRequestId();
        if (key instanceof CannedRequestDTO)
            return ((CannedRequestDTO)key).getRequestId();
        return null;
    }
}
