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
package com.arvatosystems.t9t.base.jpa;

import java.util.Collection;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.pojos.apiw.Ref;

// TODO: move to t9t
public interface IGeneralCache<R extends Ref, E extends BonaPersistableKey<Long>> {
    /** Return an entity from an existing known pool, or query from database. */
    E getEntityData(R ref, Collection<E> knownPool);

    /** Return an entity ref from an existing known pool, or query from database. */
    Long getRef(R ref, Collection<E> knownPool);
}
