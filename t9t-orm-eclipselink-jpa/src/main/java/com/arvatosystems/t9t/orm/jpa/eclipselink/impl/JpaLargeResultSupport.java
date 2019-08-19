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
package com.arvatosystems.t9t.orm.jpa.eclipselink.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.queries.ScrollableCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jpa.ormspecific.IJpaLargeResultSupport;

import de.jpaw.dp.Dependent;

@Dependent
public class JpaLargeResultSupport<E> implements IJpaLargeResultSupport<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaLargeResultSupport.class);
    private static final boolean NEED_CLEAR = false;
    private EntityManager em;
    private ScrollableCursor scrollableCursor = null;
    private int chunkSize = 25;
    private int currentIndex = 25;
    private List<E> currentSet = null;

    JpaLargeResultSupport() {
        LOGGER.info("Large result set support has been instantiated for OR mapper EclipseLink");
    }


    @Override
    public void start(EntityManager em, TypedQuery<E> query, int chunkSize) {
        this.em = em;
        this.chunkSize = chunkSize;
        currentIndex = chunkSize;
        query.setHint("eclipselink.cursor.scrollable", true);
        scrollableCursor = (ScrollableCursor) query; // Is there a typed<E> ScrollableCursor? I did not find one.
    }

    @Override
    public E getNext() {
        // check if there is a result from the current set to return
        if ((currentSet == null) || (currentIndex >= currentSet.size())) {
            // no local data available, read the next chunk
            currentIndex = 0;
            currentSet = getNextChunk();
            if ((currentSet == null) || currentSet.isEmpty()) {
                return null;
            }
        }
        return currentSet.get(currentIndex++);
    }

    @Override
    public List<E> getNextChunk() {
        if (NEED_CLEAR) {
            em.clear();
        }
        return (List<E>) scrollableCursor.next(chunkSize);
    }

    @Override
    public void end(TypedQuery<E> query) {
        scrollableCursor = null;
        currentSet = null;
        em = null;
    }

}
