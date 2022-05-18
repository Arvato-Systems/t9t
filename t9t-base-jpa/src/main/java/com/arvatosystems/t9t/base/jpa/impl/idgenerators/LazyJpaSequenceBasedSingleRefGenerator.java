/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jpa.impl.idgenerators;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.cfg.be.DatabaseBrandType;

import de.jpaw.dp.Jdp;

class LazyJpaSequenceBasedSingleRefGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LazyJpaSequenceBasedSingleRefGenerator.class);

    private volatile long lastProvidedValue;
    private volatile int remainingCachedIds;
    private final int cacheSize;
    private final String sqlCommandForNextValue;

    // @Inject
    private final EntityManagerFactory emf = Jdp.getRequired(EntityManagerFactory.class);

    LazyJpaSequenceBasedSingleRefGenerator(final int index, final DatabaseBrandType dialect, final int cacheSize) {

        lastProvidedValue = -1L;
        remainingCachedIds = 0;
        this.cacheSize = cacheSize;
        switch (dialect) {
        case POSTGRES:
            sqlCommandForNextValue = String.format("SELECT nextval('cm_idgen_%04d_seq')", index);
            break;
        case ORACLE:
            sqlCommandForNextValue = String.format("SELECT cm_idgen_%04d_seq.NEXTVAL FROM DUAL", index);
            break;
        case HANA:
            sqlCommandForNextValue = String.format("SELECT cm_idgen_%04d_seq.NEXTVAL FROM DUMMY", index);
            break;
        case MS_SQL_SERVER: // requires MS SQL server 2012 or newer (sequences were introduces only then)
            sqlCommandForNextValue = String.format("SELECT NEXT VALUE FOR [dbo].[cm_idgen_%04d_seq]", index);
            break;
        default: // does not happen!
            LOGGER.error("undefined DatabaseDialect: {}", dialect.name());
            throw new T9tException(T9tException.JDBC_UNKNOWN_DIALECT, dialect.name()); // this would be a fatal error
        }
    }

    synchronized long getnextId() {
        if (remainingCachedIds > 0) {
            --remainingCachedIds;
            ++lastProvidedValue;
        } else {
            // no data in cache, must obtain a new database sequence number
            // use the current thread's EntityManager to request a new value
            // from the database, because then we do not need to synchronize
            // different threads requesting different values at the same time.
            final EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            final Query q = em.createNativeQuery(sqlCommandForNextValue);
            final Object result = q.getSingleResult();
            long nextval;
            if (result instanceof Number) {
                // approach to cover all numeric values...
                nextval = ((Number) result).longValue();
            } else {
                LOGGER.error("sequence query returned type {} which cannot be processed (yet)", result.getClass().getCanonicalName());
                throw new T9tException(T9tException.JDBC_BAD_TYPE_RETURNED, result.getClass().getCanonicalName());
            }
            em.getTransaction().commit();
            em.close();
            // store data for the next bunch of results
            lastProvidedValue = nextval * cacheSize;
            remainingCachedIds = cacheSize - 1;
        }
        return lastProvidedValue;
    }
}
