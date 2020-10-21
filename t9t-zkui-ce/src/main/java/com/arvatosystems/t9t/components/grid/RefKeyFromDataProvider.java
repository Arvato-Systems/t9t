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
package com.arvatosystems.t9t.components.grid;

import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

/** The default implementation (which is valid for most viewModels) implements the SurrogateKey filter. */
@Singleton
@Any
@Fallback
public class RefKeyFromDataProvider implements IKeyFromDataProvider<Ref, TrackingBase> {

    @Override
    public SearchFilter getFilterForKey(DataWithTracking<Ref, TrackingBase> dwt) {
        Long objectRef = dwt.getData().getObjectRef();
        final LongFilter byId = new LongFilter("objectRef");
        byId.setEqualsValue(objectRef);
        return byId;
    }
}
