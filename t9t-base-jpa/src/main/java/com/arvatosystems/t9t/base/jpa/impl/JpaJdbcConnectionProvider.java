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
package com.arvatosystems.t9t.base.jpa.impl;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.arvatosystems.t9t.base.jpa.ormspecific.IJpaJdbcConnectionProvider;
import com.arvatosystems.t9t.base.services.IJdbcConnectionProvider;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;

@Singleton
public class JpaJdbcConnectionProvider implements IJdbcConnectionProvider {
    private static AtomicInteger counter = new AtomicInteger();

    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);
    protected final IJpaJdbcConnectionProvider connProvider = Jdp.getRequired(IJpaJdbcConnectionProvider.class);

    @Override
    public Connection getJDBCConnection() {
        counter.incrementAndGet();
        return connProvider.get(jpaContextProvider.get().getEntityManager());
    }

    @Override
    public List<Integer> checkHealth() {
        Integer count = counter.get();
        return Collections.singletonList(count);
    }
}
