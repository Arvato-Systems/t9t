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
package com.arvatosystems.t9t.component.ext;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;

/** Data sent for onDataSelect event. */
public class EventDataSelect28 {
    public static final String ON_DATA_SELECT = "onDataSelect";  // the event name

    private final DataWithTracking<BonaPortable, TrackingBase> dwt;
    private final int keys;             // as used in onSelect event...
    private final String contextId;     // if fired by context menu (right click)

    public EventDataSelect28(DataWithTracking<BonaPortable, TrackingBase> dwt, int keys, String contextId) {
        this.dwt = dwt;
        this.keys = keys;
        this.contextId = contextId;
    }

    public String getContextId() {
        return contextId;
    }

    public int getKeys() {
        return keys;
    }

    public DataWithTracking<BonaPortable, TrackingBase> getDwt() {
        return dwt;
    }
}
