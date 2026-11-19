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
package com.arvatosystems.t9t.refs;

/**
 * API provided by persistence implementations like JPA, in-memory storage, noSQL providers...
 *
 * PersistenceProviders register at the RequestContext in order to get their commit / rollback callback invoked at the end of the request.
 * */
public interface PersistenceProvider extends AutoCloseable {
    /** Returns some identification of the provider. (PersistenceProviders.name()) */
    String getId();

    /** Returns the numeric ID for the provider. (PersistenceProviders.ordinal()) */
    int getPriority();

    /** Starts a transaction. */
    void open();

    /** Rolls back the current transaction. */
    void rollback();

    /** Commits the current transaction. */
    void commit() throws Exception;

    /** Closes the context, should be preceeded by commit() or rollback(). */
    @Override
    void close() throws Exception;
}
