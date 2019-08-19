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
package com.arvatosystems.t9t.base.services.impl;

import org.joda.time.Instant;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.entities.FullTracking;

import de.jpaw.bonaparte.refsw.RequestContext;
import de.jpaw.bonaparte.refsw.TrackingUpdater;

public class UpdaterFullTracking42 implements TrackingUpdater<FullTracking> {

    @Override
    public void preCreate(RequestContext ctx, FullTracking tr) {
        tr.setCTechUserId("-");
        if (ctx != null) {
            tr.setCTimestamp(ctx.getExecutionStart());
            tr.setCAppUserId(ctx.getUserRef().toString());  // HACK
            tr.setCProcessRef(ctx.getRequestRef());
        } else {
            Instant now = Instant.now();
            tr.setCTimestamp(now);
            tr.setCAppUserId(T9tConstants.ANONYMOUS_USER_ID);
            tr.setCProcessRef(0L);
        }
    }

    @Override
    public void preUpdate(RequestContext ctx, FullTracking tr) {
        tr.setMTechUserId("-");
        if (ctx != null) {
            tr.setMTimestamp(ctx.getExecutionStart());
            tr.setMAppUserId(ctx.getUserRef().toString());  // HACK (we do not have the t9t Request context here)
            tr.setMProcessRef(ctx.getRequestRef());
        } else {
            Instant now = Instant.now();
            tr.setMTimestamp(now);
            tr.setMAppUserId(T9tConstants.ANONYMOUS_USER_ID);
            tr.setMProcessRef(0L);
        }
    }
}
