/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jpa.rl.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jpa.IPersistenceProviderJPAShadow;
import com.arvatosystems.t9t.base.jpa.ormspecific.IQueryHintSetter;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.PersistenceProviders;
import de.jpaw.dp.CustomScope;
import de.jpaw.dp.Default;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import jakarta.persistence.EntityManagerFactory;

/**
 * The provider for a secondary JPA persistence context in readonly mode, which could be the shadow database connection.
 * This implementation hooks into the RequestContext (which could be passed across threads) and checks for an existing JPA provider.
 * If none exists, it creates a new one and registers it.
*/
@Default
public class PersistenceProviderJPAShadowProvider implements CustomScope<IPersistenceProviderJPAShadow> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceProviderJPAShadowProvider.class);
    private final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);
    private final IQueryHintSetter queryHintSetter = Jdp.getRequired(IQueryHintSetter.class);
    private final EntityManagerFactory emf;

    public PersistenceProviderJPAShadowProvider(final EntityManagerFactory emf) {
        super();
        this.emf = emf;
    }

    @Override
    public IPersistenceProviderJPAShadow get() {
        final RequestContext ctx = ctxProvider.get();
        IPersistenceProviderJPAShadow jpaContext = (IPersistenceProviderJPAShadow) ctx.getPersistenceProvider(PersistenceProviders.AEROSPIKE.ordinal());
        if (jpaContext == null) {
            // does not exist, create a new one!
            LOGGER.debug("Creating secondary JPA session (EntityManager) for shadow DB access");
            jpaContext = new PersistenceProviderJPAShadowRLImpl(emf);
            ctx.addPersistenceContext(jpaContext);
            queryHintSetter.setReadOnlySession(jpaContext.getEntityManager());
        }

        return jpaContext;
    }

    @Override
    public void set(final IPersistenceProviderJPAShadow instance) {
        LOGGER.warn("Set operation is not supported");
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        LOGGER.debug("Closing JPA request context (unsupported)");
        throw new UnsupportedOperationException();
    }
}
