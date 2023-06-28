/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.ISingleRefGenerator;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;

@Singleton
@Named("lazySequenceJPA")  // only acquires an ID once the first request has been seen
public class LazyJpaSequenceBasedSingleRefGenerator implements ISingleRefGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LazyJpaSequenceBasedSingleRefGenerator.class);

    protected final EntityManagerFactory emf = Jdp.getRequired(EntityManagerFactory.class);

    @Override
    public long getNextSequence(@Nonnull final String selectStatement) {
        try (EntityManager em = emf.createEntityManager()) {
            // no data in cache, must obtain a new database sequence number
            // use the current thread's EntityManager to request a new value
            // from the database, because then we do not need to synchronize
            // different threads requesting different values at the same time.

            em.getTransaction().begin();
            final Query q = em.createNativeQuery(selectStatement);
            final Object result = q.getSingleResult();
            em.getTransaction().commit();
            if (result instanceof Number rNumber) {
                // approach to cover all numeric values...
                return rNumber.longValue();
            } else {
                LOGGER.error("sequence query returned type {} which cannot be processed (yet)", result.getClass().getCanonicalName());
                throw new T9tException(T9tException.JDBC_BAD_TYPE_RETURNED, result.getClass().getCanonicalName());
            }
        } catch (final Exception e) {
            LOGGER.error("General SQL exception: Could not obtain next sequence value: {}", ExceptionUtil.causeChain(e));
            throw new T9tException(T9tException.JDBC_GENERAL_SQL, ExceptionUtil.causeChain(e));
        }
    }
}
