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
package com.arvatosystems.t9t.jpa.refs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.api.PersistenceProviders;

/** Implementation of the JPA PersistenceProvider for RESOURCE_LOCAL persistence units. */
public class PersistenceProviderJPARLImpl implements PersistenceProviderJPA {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceProviderJPARLImpl.class);
    private EntityManager entityManager;
    private EntityTransaction transaction = null;

    /** The constructor of the provider is usually invoked by some application specific producer. */
    public PersistenceProviderJPARLImpl(EntityManagerFactory emf) {
        LOGGER.debug("new(): creating EntityManager");
        entityManager = emf.createEntityManager();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public String getId() {
        return PersistenceProviders.JPA.name();
    }

    @Override
    public int getPriority() {
        return PersistenceProviders.JPA.ordinal();
    }

    @Override
    public void open() {
        LOGGER.debug("open(): starting transaction");
        if (transaction != null)
            throw new RuntimeException("JPA transaction open() called on an existing transaction");
        transaction = entityManager.getTransaction();
        transaction.begin();
    }

    @Override
    public void rollback() {
        LOGGER.debug("rollback(): terminating transaction");
        if (transaction != null) {
            try {
                transaction.rollback();
            } catch (Exception e) {
                // cannot do anything because we are rolling back already anyway
                LOGGER.error("{} on JPA rollback: {}", e.getClass().getSimpleName(), e.getMessage());
            } finally {
                transaction = null;
            }
        }
    }

    @Override
    public void commit() throws Exception {
        LOGGER.debug("commit(): transaction end");
        if (transaction != null) {
            try {
                transaction.commit();
            } finally {
                transaction = null;
            }
        }
    }


    @Override
    public void close() {
        if (transaction != null) {
            LOGGER.warn("attempt to close an open transaction, performing an implicit rollback");
            transaction.rollback();  // rollback should set transaction to null
        }
        LOGGER.debug("close(): destroying EntityManager");
        // allow multiple closes...
        if (entityManager != null) {
            entityManager.close();
            entityManager = null;
        }
    }
}
