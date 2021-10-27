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
package com.arvatosystems.t9t.base.jpa;

import java.io.Serializable;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;

/** Defines methods to return either the artificial key (via any key) or the full JPA entity (via some key).
 *
 * For every relevant JPA entity, one separate interface is extended from this one, which works as a customization target for CDI.
 * If the JPA entity is extended as part of customization, the base interface will stay untouched, but its implementation must point
 * to a customized resolver, inheriting the base resolver.
 *
 * Same as IResolverAnyKey, but using a tenantRef instead of tenantId.
 */
public interface IResolverAnyKey42<
    KEY extends Serializable,
    TRACKING extends TrackingBase,
    ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>
  > extends IResolverAnyKey<KEY, TRACKING, ENTITY> {

    /** returns the entity's tenantRef without the use of reflection, or null if the entity does not contain
     * a tenantRef field.
     * @param e
     * @return the tenantRef
     */
    Long getTenantRef(ENTITY e);

    /**
     * Returns the mapped tenantRef for this entity. This is by default identical to the current tenantRef, but may be overridden for specific tenants in
     * specific projects. This method will be used to set default values for the entity and also for queries, therefore overwriting it will cause a shared use
     * of tenants. A future default implementation will use a database based mapping.
     *
     * @return the tenantRef to use for the database
     */
    Long getSharedTenantRef();

    /** Sets the entity's tenantRef without the use of reflection, or NOOP if the entity does not contain
     * a tenantRef field.
     * @param e - an instance of the Entity
     * @param tenantRef - the tenant to be set (if null, the current call's tenant ref wil be used)
     */
    void setTenantRef(ENTITY e, Long tenantRef);

    /**
     * Returns true if the current tenant is allowed to see a record of tenant (tenantRef). The tenant passed must be the final (possibly mapped) tenant.
     */
    boolean readAllowed(Long tenantRef);

    /**
     * Returns true if the current tenant is allowed to write a record of tenant (tenantRef). The tenant passed must be the final (possibly mapped) tenant.
     */
    boolean writeAllowed(Long tenantRef);
}
