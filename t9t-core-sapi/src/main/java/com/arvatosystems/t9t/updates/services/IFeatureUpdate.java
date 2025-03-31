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
package com.arvatosystems.t9t.updates.services;

import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;

/**
 * Provides an interface to run configuration data updates.
 *
 * Implementations are qualified by ticketId and also by applySequenceId.
 * The former is used for single ticket updates, the latter for full release updates.
 */
public interface IFeatureUpdate {
    /**
     * Performs the required updates for the specified ticket.
     *
     * @param ctx the request context of the current execution
     * @param executor a request executor, for subrequests
     * @return the return code (0 = OK) for the update request.
     */
    int performUpdate(RequestContext ctx, IExecutor executor);

    /** Returns the ticketId this implementation supports. */
    String getTicketId();

    /** Returns the sequenceId (version plus date). */
    String getApplySequenceId();

    /** Returns the description of the ticket. */
    String getDescription();

    /**
     * Returns true if the update allows to be restarted.
     * The default is true, because we usually roll back any failed update. Set it to false if intermediate commits are done.
     */
    default boolean getAllowRestartOfPending() {
        return true;
    }
}
