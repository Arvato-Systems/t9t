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
package com.arvatosystems.t9t.base.services;

import com.arvatosystems.t9t.base.event.EventParameters;

/** Describes methods of Event handlers (subscribers to events).
 * Implementations should be singletons and be annotated by a @Named annotation, by which the implementation is looked up. */
public interface IEventHandler {

    /** Perform activity for a trigger described by eventData.
     * Returns 0 is processing was successful, otherwise an error code. */
    public int execute(RequestContext ctx, EventParameters eventData);
}
