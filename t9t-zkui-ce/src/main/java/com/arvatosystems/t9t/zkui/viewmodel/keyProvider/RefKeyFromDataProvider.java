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

import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.api.DataWithTracking;
import com.arvatosystems.t9t.api.SearchFilter;
import com.arvatosystems.t9t.api.SearchFilters;
import com.arvatosystems.t9t.api.TrackingBase;
import com.arvatosystems.t9t.apiw.Ref;
import com.arvatosystems.t9t.zkui.IKeyFromDataProvider;

/** The default implementation (which is valid for most viewModels) implements the SurrogateKey filter. */
@Singleton
@Any
@Fallback
public class RefKeyFromDataProvider implements IKeyFromDataProvider<Ref, TrackingBase> {

    @Override
    public SearchFilter getFilterForKey(DataWithTracking<Ref, TrackingBase> dwt) {
        Long objectRef = dwt.getData().getObjectRef();
        return SearchFilters.equalsFilter("objectRef", objectRef);
    }
}
