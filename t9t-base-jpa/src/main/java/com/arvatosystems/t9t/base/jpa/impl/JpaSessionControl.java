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
package com.arvatosystems.t9t.base.jpa.impl;

import jakarta.persistence.EntityManager;

import com.arvatosystems.t9t.base.jpa.ormspecific.IQueryHintSetter;
import com.arvatosystems.t9t.base.services.IJpaSessionControl;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;

@Singleton
public class JpaSessionControl implements IJpaSessionControl {
    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);
    protected final IQueryHintSetter queryHintSetter = Jdp.getRequired(IQueryHintSetter.class);

    @Override
    public void setManualFlushMode() {
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        queryHintSetter.setManualFlushMode(em);
    }

    @Override
    public void setSessionReadOnly() {
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        queryHintSetter.setReadOnlySession(em);
    }
}
