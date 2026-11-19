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

import java.time.Instant;

import com.arvatosystems.t9t.refs.PersistenceProvider;

/** Implementations provide information about the current request context, such as user, tenant, timestamp.
 * The implementing class is either some static information provider, some ThreadLocal, or some CDI injected instance. */
public interface RequestContext extends AutoCloseable {
    /** Returns some identification about the current tenant in multi-tenant environments, or 0 if not applicable. */
    Long getTenantRef();
    /** Returns some identification of the current user, or 0 if no user has been authenticated. */
    Long getUserRef();
    /** Returns some identification about the request being processed, or 0 if not applicable. */
    long getRequestRef();
    /** Returns cached information when the request processing has started (to avoid repeatedly querying the system clock, which is a costly operation). */
    Instant getExecutionStart();

    /** Retrieves the persistence provider of the given priority. */
    PersistenceProvider getPersistenceProvider(int priority);
    /** Adds a persistence provider to the list of involved transaction participants. */
    void addPersistenceContext(PersistenceProvider pprovider);

    /** Commits the current transaction. Issues a commit to all registered persistence providers, in order of ascending priority. */
    void commit() throws Exception;
    /** Rolls back the current transaction. Issues the rollback to all registered persistence providers, in order of ascending priority. */
    void rollback() throws Exception;
}
