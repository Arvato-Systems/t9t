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
package com.arvatosystems.t9t.base.jpa;

import java.io.Serializable;

import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;

/** Defines methods to return either the artificial key (via any key) or the full JPA entity (via some key).
 *
 * For every relevant JPA entity, one separate interface is extended from this one, which works as a customization target for CDI.
 * If the JPA entity is extended as part of customization, the base interface will stay untouched, but its implementation must point
 * to a customized resolver, inheriting the base resolver.
 */
public interface IResolverCompositeKey<
    REF extends BonaPortable,
    KEY extends Serializable,  // can be removed!
    TRACKING extends TrackingBase,
    ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>
  > extends IResolverAnyKey<KEY, TRACKING, ENTITY> {

    /** Return the full JPA entity for any given relevant key.
     * Returns null if the parameter entityRef is null.
     * Throws an exception (T9tException.RECORD_DOES_NOT_EXIST) if there is no data record for the specified entityRef.
     *
     * @param entityRef The input DTO, which inherits a suitable reference to the object.
     * @return ENTITY
     * @throws T9tException
     */
    ENTITY getEntityData(REF entityRef);

    /** Converts any REF to a KEY (if supported). */
    KEY refToKey(REF arg);
}
