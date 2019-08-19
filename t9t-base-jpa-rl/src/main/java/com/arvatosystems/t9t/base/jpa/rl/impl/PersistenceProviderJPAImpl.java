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
package com.arvatosystems.t9t.base.jpa.rl.impl;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableNoData;
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPARLImpl;

public class PersistenceProviderJPAImpl extends PersistenceProviderJPARLImpl {

    public PersistenceProviderJPAImpl(EntityManagerFactory emf) {
        super(emf);
    }

    public final Map<BonaPersistableNoData<?, ?>, Map<Class<? extends BonaPortable>, BonaPortable>> dtoCache
        = new IdentityHashMap<BonaPersistableNoData<?, ?>, Map<Class<? extends BonaPortable>, BonaPortable>>(61);
}
