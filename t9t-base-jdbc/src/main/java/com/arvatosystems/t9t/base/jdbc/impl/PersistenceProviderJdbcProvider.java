/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jdbc.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jdbc.PersistenceProviderJdbc;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.zaxxer.hikari.HikariDataSource;

import de.jpaw.bonaparte.pojos.api.PersistenceProviders;
import de.jpaw.dp.CustomScope;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;

/**
 * The provider for the JDBC persistence context.
 * This implementation hooks into the RequestContext (which could be passed across threads) and checks for an existing JDBC provider.
 * If none exists, it creates a new one and registers it.
 */
class PersistenceProviderJdbcProvider implements CustomScope<PersistenceProviderJdbc> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceProviderJdbcProvider.class);
    private final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);
    private final HikariDataSource ds;

    public PersistenceProviderJdbcProvider(HikariDataSource ds) {
        super();
        this.ds = ds;
    }

    @Override
    public PersistenceProviderJdbc get() {
        RequestContext ctx = ctxProvider.get();
        PersistenceProviderJdbc jdbcContext = (PersistenceProviderJdbc)ctx.getPersistenceProvider(PersistenceProviders.UNUSED.ordinal());
        if (jdbcContext == null) {
            // does not exist, create a new one!
            LOGGER.trace("Adding JDBC to request context");
            jdbcContext = new PersistenceProviderJdbcImpl(ds);
            ctx.addPersistenceContext(jdbcContext);
        }

        return jdbcContext;
    }

    @Override
    public void set(PersistenceProviderJdbc instance) {
        LOGGER.warn("Set operation is not supported");
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        LOGGER.trace("Closing JPA request context (unsupported)");
        throw new UnsupportedOperationException();
    }
}
