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
package com.arvatosystems.t9t.orm.jpa.hibernate.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.jpa.HibernateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jpa.ormspecific.IJpaLargeResultSupport;

import de.jpaw.dp.Dependent;

// import javax.persistence.Query;  // can be casted, but does not have the required methods to set FetchSize

/******************************************************************
 *
 * Large Result Sets + Hibernate = not working. This can be improved with JPA 2.2 / Hibernate 5.3 with the new streams API.
 *
 ******************************************************************/
@Dependent
public class JpaLargeResultSupport<E> implements IJpaLargeResultSupport<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaLargeResultSupport.class);
    private static final boolean NEED_CLEAR = false;
    private Query untypedHibernateQuery = null;
    private ScrollableResults results = null;
    private EntityManager em;
    private int chunkSize = 25;
    private int currentIndex = 25;

    JpaLargeResultSupport() {
        LOGGER.info("Large result set support has been instantiated for OR mapper Hibernate");
    }

    @Override
    public void start(EntityManager em, TypedQuery<E> query, int chunkSize) {
        this.em = em;
        this.chunkSize = chunkSize;
        HibernateQuery hq = (HibernateQuery) query;
        untypedHibernateQuery = hq.getHibernateQuery();
        untypedHibernateQuery.setFetchSize(Integer.valueOf(chunkSize));
        untypedHibernateQuery.setReadOnly(true);
        untypedHibernateQuery.setLockMode("a", LockMode.NONE);
        results = untypedHibernateQuery.scroll(ScrollMode.FORWARD_ONLY);
    }

    @Override
    public E getNext() {
        if (NEED_CLEAR) {
            if (++currentIndex > chunkSize) {
                currentIndex = 0;
                em.clear();
            }
        }
        if (!results.next()) {
            return null;
        }
        return (E) results.get(0);
    }

    @Override
    public List<E> getNextChunk() {
        if (NEED_CLEAR) {
            em.clear();
        }
        List<E> resultSet = new ArrayList<E>(chunkSize);
        int i = 0;
        while ((i < chunkSize) && results.next()) {
            resultSet.add((E) results.get(0));
            ++i;
        }
        return resultSet;
    }

    @Override
    public void end(TypedQuery<E> query) {
        results.close();
        results = null;
        em = null;
        untypedHibernateQuery = null;
    }

}
