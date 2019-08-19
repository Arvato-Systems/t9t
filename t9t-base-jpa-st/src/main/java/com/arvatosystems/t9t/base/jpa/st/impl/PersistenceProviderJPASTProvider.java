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

import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.PersistenceProviders;
import de.jpaw.dp.CustomScope;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;

/**
 * The provider for the JPA persistence context. This implementation hooks into the RequestContext (which could be
 * passed across threads) and checks for an existing JPA provider. If none exists, it creates a new one and registers
 * it.
 */
public class PersistenceProviderJPASTProvider implements CustomScope<PersistenceProviderJPA> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceProviderJPASTProvider.class);

    private final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);
    private final JpaTransactionManager txMgr;
    private final EntityManager sharedEntityManager;

    public PersistenceProviderJPASTProvider(JpaTransactionManager txMgr, EntityManager sharedEntityManager) {
        this.txMgr = txMgr;
        this.sharedEntityManager = sharedEntityManager;
    }

    @Override
    public PersistenceProviderJPA get() {
        RequestContext ctx = ctxProvider.get();
        PersistenceProviderJPA jpaContext = (PersistenceProviderJPA) ctx.getPersistenceProvider(PersistenceProviders.JPA.ordinal());
        if (jpaContext == null) {
            // does not exist, create a new one!
            LOGGER.trace("Adding JPA to request context");
            jpaContext = new PersistenceProviderJPASTImpl(txMgr, sharedEntityManager);
            ctx.addPersistenceContext(jpaContext);
        }

        return jpaContext;
    }

    @Override
    public void set(PersistenceProviderJPA instance) {
        LOGGER.warn("Set operation is not supported");
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        LOGGER.debug("Closing JPA request context (unsupported)");
        throw new UnsupportedOperationException();
    }
}
