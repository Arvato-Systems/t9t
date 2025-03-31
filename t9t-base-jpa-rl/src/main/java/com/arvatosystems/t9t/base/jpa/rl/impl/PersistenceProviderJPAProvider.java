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

import com.arvatosystems.t9t.base.jpa.ormspecific.IQueryHintSetter;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPARLImpl;
import de.jpaw.bonaparte.pojos.api.PersistenceProviders;
import de.jpaw.dp.CustomScope;
import de.jpaw.dp.Default;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import jakarta.persistence.EntityManagerFactory;

/**
 * The provider for the JPA persistence context.
 * This implementation hooks into the RequestContext (which could be passed across threads) and checks for an existing JPA provider.
 * If none exists, it creates a new one and registers it.
 *
 * This provider returns an entity manager which connects to either the primary DB or the shadow, depending on the request and its parameters.
 */
@Default
public class PersistenceProviderJPAProvider implements CustomScope<PersistenceProviderJPA> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceProviderJPAProvider.class);
    private final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);
    private final IQueryHintSetter queryHintSetter = Jdp.getRequired(IQueryHintSetter.class);
    private final EntityManagerFactory emf;
    private final EntityManagerFactory shadowEmf;

    public PersistenceProviderJPAProvider(final EntityManagerFactory emf, final EntityManagerFactory shadowEmf) {
        super();
        this.emf = emf;
        this.shadowEmf = shadowEmf;
    }

    @Override
    public PersistenceProviderJPA get() {
        final RequestContext ctx = ctxProvider.get();
        PersistenceProviderJPA jpaContext = (PersistenceProviderJPA) ctx.getPersistenceProvider(PersistenceProviders.JPA.ordinal());
        if (jpaContext == null) {
            // does not exist, create a new one!
            final boolean readOnly = ctx.getReadOnlyMode();
            final boolean useShadowDB = ctx.getUseShadowDatabase() && shadowEmf != null;
            LOGGER.debug("Creating new {}JPA session (EntityManager) in {} mode and adding to RequestContext", useShadowDB ? "SHADOW-" : "",
                    readOnly ? "ReadOnly" : "R/W");
            if (useShadowDB) {
                jpaContext = new PersistenceProviderJPARLImpl(shadowEmf);
            } else {
                jpaContext = new PersistenceProviderJPARLImpl(emf);
            }
            ctx.addPersistenceContext(jpaContext);
            if (readOnly) {
                queryHintSetter.setReadOnlySession(jpaContext.getEntityManager());
            }
        }

        return jpaContext;
    }

    @Override
    public void set(final PersistenceProviderJPA instance) {
        LOGGER.warn("Set operation is not supported");
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        LOGGER.debug("Closing JPA request context (unsupported)");
        throw new UnsupportedOperationException();
    }
}
