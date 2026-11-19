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
package com.arvatosystems.t9t.refsw;

import com.arvatosystems.t9t.api.TrackingBase;

/** Defines the methods to implement for update of the tracking columns. */
public interface TrackingUpdater<TRACKING extends TrackingBase> {
    /** Updates the relevant columns before a record is persisted the first time (i.e. all fields should be initialized). */
    void preCreate(RequestContext ctx, TRACKING tr);

    /** Updates columns before some record is updated. Only modification timestamps should be altered. */
    void preUpdate(RequestContext ctx, TRACKING tr);
}
