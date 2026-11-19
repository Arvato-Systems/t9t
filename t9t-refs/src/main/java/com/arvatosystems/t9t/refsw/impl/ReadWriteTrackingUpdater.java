/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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
package com.arvatosystems.t9t.refsw.impl;

import com.arvatosystems.t9t.api.ReadWriteTracking;
import com.arvatosystems.t9t.refsw.RequestContext;
import com.arvatosystems.t9t.refsw.TrackingUpdater;

public class ReadWriteTrackingUpdater implements TrackingUpdater<ReadWriteTracking> {

    @Override
    public void preCreate(RequestContext ctx, ReadWriteTracking tr) {
        tr.setWhenCreated(ctx.getExecutionStart());
    }

    @Override
    public void preUpdate(RequestContext ctx, ReadWriteTracking tr) {
        tr.setWhenUpdated(ctx.getExecutionStart());
    }
}
