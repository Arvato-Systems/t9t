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
package com.arvatosystems.t9t.zkui.viewmodel.keyProvider;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.api.DataWithTracking;
import com.arvatosystems.t9t.api.SearchFilter;
import com.arvatosystems.t9t.api.SearchFilters;
import com.arvatosystems.t9t.api.TrackingBase;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestDTO;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestExtendedDTO;
import com.arvatosystems.t9t.zkui.IKeyFromDataProvider;

@Singleton
@Named("dataChangeRequestExtended")
public class DataChangeRequestExtendedKeyFromDataProvider implements IKeyFromDataProvider<DataChangeRequestExtendedDTO, TrackingBase> {

    @Override
    public SearchFilter getFilterForKey(DataWithTracking<DataChangeRequestExtendedDTO, TrackingBase> dwt) {
        Long objectRef = dwt.getData().getChange().getObjectRef();
        return SearchFilters.equalsFilter(DataChangeRequestDTO.meta$$objectRef.getName(), objectRef);
    }
}
