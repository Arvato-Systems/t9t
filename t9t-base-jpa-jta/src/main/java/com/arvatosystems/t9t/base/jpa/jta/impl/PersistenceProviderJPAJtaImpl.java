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
package com.arvatosystems.t9t.base.jpa.jta.impl;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.PersistenceProviders;

/**
 *
 * @author LUEC034
 */
public class PersistenceProviderJPAJtaImpl implements PersistenceProviderJPA {

    private final EntityManager entityManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceProviderJPAJtaImpl.class);

    private UserTransaction transaction;

    public PersistenceProviderJPAJtaImpl(EntityManager em) {
        this.entityManager = em;

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
        LOGGER.trace("open(): starting transaction");
        try {
            if (transaction == null) {
                transaction = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
            }
            transaction.begin();
        } catch (NamingException | NotSupportedException | SystemException e) {
            throw new RuntimeException("JPA transaction open() called and can not find UserTransaction");
        }

    }

    @Override
    public void rollback() {
        LOGGER.trace("rollback(): terminating transaction");
        try {
            transaction.rollback();
        } catch (SystemException e) {
            throw new RuntimeException("JPA transaction rollback() called and caused a SystemException");
        }
    }

    @Override
    public void commit() throws Exception {
        LOGGER.trace("commit(): transaction end");
        int transactionStatus = transaction.getStatus();
        if (transactionStatus == Status.STATUS_NO_TRANSACTION) {
            throw new RuntimeException("commit() called on transaction without active transaction.");
        } else {
            transaction.commit();
        }
    }

    @Override
    public void close() {
        try {
            if (transaction.getStatus() == Status.STATUS_ACTIVE) {
                LOGGER.warn("attempt to close an open transaction, performing an implicit rollback");
                transaction.rollback();
            }
        } catch (SystemException ex) {
            throw new RuntimeException("JPA transaction rollback() called and caused a SystemException");
        }
        // no close required as container managed entity managers will close automatically

    }

}
