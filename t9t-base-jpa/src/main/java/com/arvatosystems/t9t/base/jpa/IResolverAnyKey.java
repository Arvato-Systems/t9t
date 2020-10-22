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
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.search.CountCriteria;
import com.arvatosystems.t9t.base.search.CountResponse;
import com.arvatosystems.t9t.base.search.SearchCriteria;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;

/** Defines methods to return either the artificial key (via any key) or the full JPA entity (via some key).
 *
 * For every relevant JPA entity, one separate interface is extended from this one, which works as a customization target for CDI.
 * If the JPA entity is extended as part of customization, the base interface will stay untouched, but its implementation must point
 * to a customized resolver, inheriting the base resolver.
 *
 * Same as IResolverAnyKey, but using a tenantRef instead of tenantId.
 */
public interface IResolverAnyKey<
    KEY extends Serializable,
    TRACKING extends TrackingBase,
    ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>
  > {

    /** Returns the entityManager of the current context (creates one, if it does not yet exist).
     */
    public EntityManager getEntityManager();

    /** Returns information if the object's key is a Long key which is generated by a sequence.
     * @return false if the object has a natural primary key (often composite), true if it was generated from a sequence and has no business meaning.
     */
    public boolean hasArtificialPrimaryKey();

    /**
     * Returns true is the provided entity is of the current (or shared) tenant, or has no tenant field.
     *
     * @return true if an update to the provided entity would hit the current tenant (MERGE)
     */
    public boolean isOfMatchingTenant(ENTITY e);

    /** Returns information if the entity has a field tenantRef and data is therefore isolated between tenants.
     * If set, appropriate additional criteria will be set for generic search.
     * The default implementation returns true. Only in case of shared tables, this should be overridden.
     *
     * @return true if the entity is tenant specific, else false.
     */
    public boolean isTenantIsolated();

    /** Returns information if the entity is used in tenant isolation mode, but with view rights to the global tenant
     * (for defaults / global settings).
     *
     * @return true if the entity allows (read only) access to the default tenant, else false.
     */
    public boolean isTenantMeOrGlobal();

    /** Returns information if the entity is used in tenant isolation mode, but the global tenant
     * has access to all other tenant's data.
     *
     * @return true if the entity is tenant specific, else false.
     */
    public boolean globalTenantCanAccessAll();

    /** Returns the RTTI of the Entity class (using a static initialization of class$rtti() )
     *
     * @return the Entity's rtti (which matches the RTTI bon file's RTTI value.
     */
    public int getRtti();

    /** Returns the base class of the entity.
     *
     * @return the class object of the JPA entity which is valid for the application.
     */
    public Class<ENTITY> getBaseJpaEntityClass();

    /** Returns the class of the entity's key.
     *
     * @return the class object of the JPA entity primary key.
     */

    public Class<KEY> getKeyClass();

    /** Returns the class of the entity's tracking columns.
     *
     * @return the class object of the JPA entity which determines the type of tracking data.
     */
    public Class<TRACKING> getTrackingClass();

    /** Returns the customized class of the entity.
     *
     * @return the class object of the JPA entity which is valid for the given customization.
     * @throws T9tException if the class could not be resolved.
     */
    public Class<ENTITY> getEntityClass();

    /** Returns a new instance of the customized class of the entity.
     * If the entity has a tenantRef field, its contents will be initialized from the internalHeaderParameters.
     *
     * @return a new instance of the customization of the JPA class object (Entity)
     * @throws T9tException if the class could not be resolved or not instantiated.
     */
    public ENTITY newEntityInstance();

    /** Return a list of all JPA entities for any given table.
     * Provided for historic reasons.
     *
     * @param onlyActive True if inactive records should be treated as nonexisting.
     * @return ENTITY
     * @throws T9tException
     */
    List<ENTITY> readAll(boolean onlyActive);

    /** Perform generic search and return the result set (full entities).
     * If a processor is given, the result records are rather output one by one through that interface, for example
     * for conversion into some DTO and output to CSV / XLS then. In that case, a null list is returned.
     *
     * @param criteria the search, sort and result subset criteria
     * @return ENTITY
     * @throws T9tException
     */
    List<ENTITY> search(SearchCriteria criteria, IDataProcessor<KEY, TRACKING, ENTITY> processor);

    /** Perform generic search and return the result set (full entities).
     *
     * @param criteria the search, sort and result subset criteria
     * @return ENTITY
     * @throws T9tException
     */
    List<ENTITY> search(SearchCriteria criteria);

    /** Perform generic search and return the size of the result set.
    *
    * @param criteria the search, sort and result subset criteria
    * @return Long
    * @throws T9tException
    */
    default CountResponse count(CountCriteria countCriteria) {
        return new CountResponse(0, count(countCriteria.getSearchFilter(), countCriteria.getApplyDistinct()));
    }

    /** Perform generic search and return the size of the result set.
    *
    * @param criteria the search, sort and result subset criteria
    * @return Long
    * @throws T9tException
    */
    Long count(SearchFilter filter, Boolean applyDistinct);

    /** Perform generic search and return the result set (keys only).
     *
     * @param searchCriteria the search, sort and result subset criteria
     * @return ENTITY
     * @throws T9tException
     */
    List<KEY> searchKey(SearchCriteria searchCriteria);

    // useful DAO methods for the entity.

    /**
     * Find an entity given the object reference.
     * @param objectRef object reference
     * @return the entity or NULL if such object can't be found
     * @throws T9tException if there is an access problem (tenant violation)
     */
    ENTITY find(KEY objectRef);

    /**
     * Find an entity given the object reference, and allows to specify the lock mode type.
     * @param objectRef object reference
     * @param lockMode the lock mode. If null if passed, PESSIMISTIC_WRITE is assumed.
     * @return the entity or NULL if such object can't be found
     * @throws T9tException if there is an access problem (tenant violation)
     */
    ENTITY find(KEY objectRef, LockModeType lockMode);

    /**
     * Find an entity given the object reference.
     * @param objectRef object reference
     * @param onlyActive if true, but the object is not active, an T9tException.RECORD_INACTIVE exception is thrown
     * @return the entity
     * @throws T9tException, also if the object does not exist or is set to inactive and onlyActive is true
     */
    ENTITY findActive(KEY objectRef, boolean onlyActive);

    /**
     * Find an entity given the object reference.
     * @param objectRef object reference
     * @param onlyActive if true, but the object is not active, an T9tException.RECORD_INACTIVE exception is thrown
     * @param lockMode the lock mode. If null if passed, PESSIMISTIC_WRITE is assumed.
     * @return the entity
     * @throws T9tException, also if the object does not exist or is set to inactive and onlyActive is true
     */
    ENTITY findActive(KEY objectRef, boolean onlyActive, LockModeType lockMode);

    /**
     * Save entity.
     * @param entity the entity
     */
    void save(ENTITY entity);

    /**
     * Update entity.
     * @param entity the entity
     */
    ENTITY update(ENTITY entity);

    /**
     * Remove entity
     * @param entity the entity
     */
    void remove(ENTITY entity);

    /**
     * Flush any data access buffer from this DAO.
     * WARNING! Use it wisely since it might affect performance!
     */
    void flush();

    /** Construct a TypedQuery on the entity class. */
    TypedQuery<ENTITY> constructQuery(String query);

    /** Return the full JPA entity for any given relevant key.
     * Returns null if the parameter entityRef is null.
     * Throws an exception (T9tException.RECORD_DOES_NOT_EXIST) if there is no data record for the specified entityRef.
     * Throws an exception (T9tException.RECORD_INACTIVE) if the record exists, but has been marked inactive and parameter onlyActive = true.
     *
     * @param key The key
     * @param onlyActive True if inactive records should be treated as nonexisting.
     * @return ENTITY
     * @throws T9tException
     */
    ENTITY getEntityDataForKey(KEY key, boolean onlyActive);

    /** Create a specification for the key object, mainly for logging purposes, also used by the AutoMappers. */
    public String entityNameAndKey(Object key);
}
