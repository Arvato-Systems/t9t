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
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.DataSinkKey;
import com.arvatosystems.t9t.io.DataSinkRef;
import com.arvatosystems.t9t.io.request.LeanDataSinkSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("dataSinkId")
@Singleton
public class Dropdown28FactoryDataSink implements IDropdown28DbFactory<DataSinkRef> {

    @Override
    public String getDropdownId() {
        return "dataSinkId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanDataSinkSearchRequest();
    }

    @Override
    public DataSinkRef createRef(Long ref) {
        return new DataSinkRef(ref);
    }

    @Override
    public DataSinkRef createKey(String id) {
        return new DataSinkKey(id);
    }

    @Override
    public Dropdown28Db<DataSinkRef> createInstance() {
        return new Dropdown28Db<DataSinkRef>(this);
    }

    @Override
    public String getIdFromKey(DataSinkRef key) {
        if (key instanceof DataSinkKey)
            return ((DataSinkKey)key).getDataSinkId();
        if (key instanceof DataSinkDTO)
            return ((DataSinkDTO)key).getDataSinkId();
        return null;
    }
}
