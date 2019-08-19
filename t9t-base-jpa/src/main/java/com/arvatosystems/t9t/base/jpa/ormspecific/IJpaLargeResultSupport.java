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
package com.arvatosystems.t9t.base.jpa.ormspecific;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Interface to allow access to OR-mapper specific implementations for efficient access to large result sets. Unfortunately, the lack of a standardized /
 * portable way to do is is anoth major shortcoming of JPA 2.0. There are ways to perform this which are either EclipseLink specific or Hibernate specific.
 *
 * Methods which have to read through large result sets and want to avoid that all data is read into memory at the same time should @Inject a provider for this
 * interface and obtain an instance for every Query. The interface uses generics to support TypedQueries.
 *
 */
public interface IJpaLargeResultSupport<E> {

    /**
     * Provides hints to the OR-mapper that the query should be treated as a scrollable cursor, with the given number of result elements for every chunk. The
     * EntityManager must be the same which was used to create the query. It is required in order to clear the L1 cache from time to time.
     * */
    void start(EntityManager em, TypedQuery<E> query, int chunkSize);

    /**
     * Fetches the next single result from the cursor (with internal buffering). Should not be used in conjunction with the multirecord result getNext(n).
     * Returns null if no next record exists.
     * */
    E getNext();

    /**
     * Fetches the next chunkSize results from the cursor. Should not be used in conjunction with the single result getNext().
     * */
    List<E> getNextChunk();

    /**
     * Closes the cursor.
     * */
    void end(TypedQuery<E> query);
}
