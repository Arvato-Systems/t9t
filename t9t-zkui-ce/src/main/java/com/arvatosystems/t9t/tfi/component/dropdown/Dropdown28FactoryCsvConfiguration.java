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
import com.arvatosystems.t9t.io.CsvConfigurationDTO;
import com.arvatosystems.t9t.io.CsvConfigurationKey;
import com.arvatosystems.t9t.io.CsvConfigurationRef;
import com.arvatosystems.t9t.io.request.LeanCsvConfigurationSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("csvConfigurationId")
@Singleton
public class Dropdown28FactoryCsvConfiguration implements IDropdown28DbFactory<CsvConfigurationRef> {

    @Override
    public String getDropdownId() {
        return "csvConfigurationId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanCsvConfigurationSearchRequest();
    }

    @Override
    public CsvConfigurationRef createRef(Long ref) {
        return new CsvConfigurationRef(ref);
    }

    @Override
    public CsvConfigurationRef createKey(String id) {
        return new CsvConfigurationKey(id);
    }

    @Override
    public Dropdown28Db<CsvConfigurationRef> createInstance() {
        return new Dropdown28Db<CsvConfigurationRef>(this);
    }

    @Override
    public String getIdFromKey(CsvConfigurationRef key) {
        if (key instanceof CsvConfigurationKey)
            return ((CsvConfigurationKey)key).getCsvConfigurationId();
        if (key instanceof CsvConfigurationDTO)
            return ((CsvConfigurationDTO)key).getCsvConfigurationId();
        return null;
    }
}
