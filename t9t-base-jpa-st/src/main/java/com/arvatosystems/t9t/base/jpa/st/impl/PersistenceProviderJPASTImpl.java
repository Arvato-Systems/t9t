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
package com.arvatosystems.t9t.base.jpa.st.impl;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionStatus;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.PersistenceProviders;

public class PersistenceProviderJPASTImpl implements PersistenceProviderJPA {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceProviderJPASTImpl.class);

    private final JpaTransactionManager jpaTransactionManager;

    private TransactionStatus transaction;
    private EntityManager sharedEntityManager;

    public PersistenceProviderJPASTImpl(JpaTransactionManager jpaTransactionManager, EntityManager sharedEntityManager) {
        this.jpaTransactionManager = jpaTransactionManager;
        this.sharedEntityManager = sharedEntityManager;
    }

    @Override
    public EntityManager getEntityManager() {
        LOGGER.trace("getEntityManager()");
        return sharedEntityManager;
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
        if (transaction == null) {
            LOGGER.trace("open(): starting transaction");
            transaction = jpaTransactionManager.getTransaction(null);
        }
    }

    @Override
    public void rollback() {
        LOGGER.trace("rollback(): terminating transaction");

        if (transaction == null) {
            throw new RuntimeException("rollback() called on transaction without active transaction.");
        }

        jpaTransactionManager.rollback(transaction);
        transaction = null;
    }

    @Override
    public void commit() throws Exception {
        LOGGER.trace("commit(): transaction end");

        if (transaction == null) {
            throw new RuntimeException("commit() called on transaction without active transaction.");
        }

        jpaTransactionManager.commit(transaction);
        transaction = null;
    }

    @Override
    public void close() {
        LOGGER.trace("close(): close persistence provider");

        if (transaction != null) {
            LOGGER.warn("attempt to close an open transaction, performing an implicit rollback");
            jpaTransactionManager.rollback(transaction);
            transaction = null;
        }
    }

}
