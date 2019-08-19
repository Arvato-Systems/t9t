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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.search.TwoRefs;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;

/** Defines methods to return either the artificial key (via any key) or the full JPA entity (via some key).
 *
 * For every relevant JPA entity, one separate interface is extended from this one, which works as a customization target for CDI.
 * If the JPA entity is extended as part of customization, the base interface will stay untouched, but its implementation must point
 * to a customized resolver, inheriting the base resolver.
 */
public interface IResolverSurrogateKey<
    REF extends Ref,
    TRACKING extends TrackingBase,
    ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>
    > extends IResolverAnyKey<Long, TRACKING, ENTITY> {

    /** Return a Long reference from any key object, if the Entity has an artificial primary key of type Long.
     * Throws a runtime / internal logic error exception if the entity has not a suitable key.
     * Returns null if the parameter entityRef itself is null.
     * Throws an exception (T9tException.RECORD_DOES_NOT_EXIST) if no entity of given key exists.
     * Throws an exception (T9tException.RECORD_INACTIVE) if the record exists, but has been marked inactive and parameter onlyActive = true.
     *
     * This method only works for primary keys of type Long. Use a different method in some inheriting class for other entities.
     *
     * @param entityRef The input DTO, which inherits a suitable reference to the object.
     * @param onlyActive True if inactive records should be treated as nonexisting.
     * @return Long The primary key of the entity.
     * @throws T9tException
     */
    Long getRef(REF entityRef, boolean onlyActive);

    /** Return the full JPA entity for any given relevant key.
     * Returns null if the parameter entityRef is null.
     * Throws an exception (T9tException.RECORD_DOES_NOT_EXIST) if there is no data record for the specified entityRef.
     * Throws an exception (T9tException.RECORD_INACTIVE) if the record exists, but has been marked inactive and parameter onlyActive = true.
     *
     * @param entityRef The input DTO, which inherits a suitable reference to the object.
     * @param onlyActive True if inactive records should be treated as nonexisting.
     * @return ENTITY
     * @throws T9tException
     */
    ENTITY getEntityData(REF entityRef, boolean onlyActive);

    /** Allocates a new artificial primary key for this entity. */
    Long createNewPrimaryKey();

    /** Reads a specific field. */
    public <Z> Z getField(REF entityRef, boolean onlyActive, String fieldName, Class<Z> cls);

    /** Reads 2 surrogate key fields. */
    public TwoRefs getRefs(REF entityRef, boolean onlyActive, String fieldName);
}
