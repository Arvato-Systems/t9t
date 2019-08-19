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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.jpa.IDataProcessor;
import com.arvatosystems.t9t.base.jpa.IResolverAnyKey;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.jpa.api.JpaCriteriaBuilder;
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Alternative;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.enums.TokenizableEnum;

/** base implementation of the IEntityResolver interface, only suitable for simple configuration data tables */
@Alternative
public abstract class AbstractResolverAnyKey<
    KEY extends Serializable,
    TRACKING extends TrackingBase,
    ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>
> implements IResolverAnyKey<KEY, TRACKING, ENTITY> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResolverAnyKey.class);

    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);
    protected final Provider<RequestContext> contextProvider = Jdp.getProvider(RequestContext.class);

    @Override
    public final EntityManager getEntityManager() {
        return jpaContextProvider.get().getEntityManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTenantId(ENTITY e) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getTenantRef(ENTITY e) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSharedTenantId() {
        return contextProvider.get().tenantMapping.getSharedTenantId(getRtti());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTenantId(ENTITY e, String tenantId) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTenantRef(ENTITY e, Long tenantRef) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOfMatchingTenant(ENTITY e) {
        return !isTenantIsolated() || getSharedTenantId().equals(getTenantId(e));
    }

    /** Returns information if the entity has a field tenantId and data is isolated between tenants.
     * If set, appropriate additional criteria will be set for generic search.
     * The default implementation returns true. Only in case of shared tables, this should be overridden.
     *
     * Overwritten by autodetection.
     *
     * @return true if the entity is tenant specific, else false.
     */
    @Override
    public boolean isTenantIsolated() {
        return true;
    }

    /** Returns information if the entity is used in tenant isolation mode, but with view rights to the global tenant
     * (for defaults / global settings).
     *
     * Overwritten by annotation.
     *
     * @return true if the entity allows (read only) access to the default tenant, else false.
     */
    @Override
    public boolean isTenantMeOrGlobal() {
        return false;
    }

    /** Returns information if the entity is used in tenant isolation mode, but the global tenant
     * has access to all other tenant's data.
     *
     * Overwritten by annotation.
     *
     * @return true if the entity is tenant specific, else false.
     */
    @Override
    public boolean globalTenantCanAccessAll() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @throws T9tException
     */
    @Override
    public ENTITY newEntityInstance() {
        RequestContext ctx = contextProvider.get();
        ENTITY e = ctx.customization.newEntityInstance(getRtti(), getBaseJpaEntityClass());
        setTenantId(e, ctx.tenantMapping.getSharedTenantId(getRtti()));
        return e;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ENTITY> getEntityClass() {
        return contextProvider.get().customization.getEntityClass(getRtti(), getBaseJpaEntityClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean readAllowed(String tenantId) {
        if (isTenantIsolated()) {
            String myTenant = getSharedTenantId();
            if (myTenant.equals(tenantId)) {
                return true; // always access to own tenant allowed
            }
            // access to different tenant to be clarified by additional parameters
            if (myTenant.equals(T9tConstants.GLOBAL_TENANT_ID)) {
                return globalTenantCanAccessAll();
            } else {
                return (T9tConstants.GLOBAL_TENANT_ID.equals(tenantId)) && isTenantMeOrGlobal();
            }
        }
        return true; // no tenant field => always allowed
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeAllowed(String tenantId) {
        String myTenant = getSharedTenantId();
        if (myTenant.equals(tenantId)) {
            return true; // always access to own tenant allowed
        }
        if (isTenantIsolated()) {
            // write access to different tenant is never allowed
            return false; // regular tenant can never modify the global tenant's data
        } else {
            // write access only for the global tenant!
            return true; // QUICKFIX!  myTenant.equals(T9tConstants.GLOBAL_TENANT_ID);
        }
    }

    @Override
    public List<ENTITY> readAll(boolean onlyActive) { // ? extends
        String myTenant = null;
        int tenantParams = 0;

        // Compute the effective class for the given tenant
        Class<ENTITY> derivedEntityClass = getEntityClass(); // ? extends

        // Create the query string
        String queryString = String.format("SELECT u FROM %s u%s", derivedEntityClass.getSimpleName(), onlyActive ? " where u.isActive = true" : "");
        if (isTenantIsolated()) {
            myTenant = getSharedTenantId();
            if (myTenant.equals(T9tConstants.GLOBAL_TENANT_ID)) {
                // global tenant
                if (!globalTenantCanAccessAll()) {
                    // need single tenant restriction
                    tenantParams = 1;
                    queryString = queryString + " AND tenantId = ?1";
                }
            } else {
                // regular tenant
                if (isTenantMeOrGlobal()) {
                    tenantParams = 2;
                    queryString = queryString + " AND tenantId IN (?1, ?2)";
                } else {
                    tenantParams = 1;
                    queryString = queryString + " AND tenantId = ?1";
                }
            }
        }
        LOGGER.trace("Query string is {}", queryString);

        // Perform the query on the database
        TypedQuery<ENTITY> query = (TypedQuery<ENTITY>) getEntityManager().createQuery(queryString, derivedEntityClass); // ? extends

        switch (tenantParams) {
        case 2:
            query.setParameter(2, T9tConstants.GLOBAL_TENANT_ID);
            // fall through
        case 1:
            query.setParameter(1, myTenant);
            break;
        }

        // Create and return the response
        return query.getResultList();
    }

    @Override
    public String entityNameAndKey(Object key) {
        return getBaseJpaEntityClass().getSimpleName() + ": " + (key instanceof BonaPortable ? key.toString() : "(" + key.getClass().getSimpleName() + ")" + key.toString());
    }

    // plausi check for find operations. Done because hibernate's exception is not really self explanatory
    protected void nullCheck(final KEY key) {
        if (key == null) {
            LOGGER.error("{}: Attempt to find entity of type {} with null supplied as key", getClass().getSimpleName(), getEntityClass().getSimpleName());
            throw new T9tException(T9tException.FIND_ON_NULL_KEY, getClass().getSimpleName() + ": " + getEntityClass().getSimpleName());
        }
    }

    /**
     * Internal all-params base implementation
     *
     * @throws T9tException
     */
    protected ENTITY findInternal(final KEY key, boolean onlyActive, boolean nullCheck, LockModeType lockMode) {
        nullCheck(key);
        ENTITY e = getEntityManager().find(getEntityClass(), key, lockMode == null ? LockModeType.PESSIMISTIC_WRITE : lockMode);
        return findInternalSub(e, key, onlyActive, nullCheck);
    }

    protected ENTITY findInternal(final KEY key, boolean onlyActive, boolean nullCheck) {
        nullCheck(key);
        ENTITY e = getEntityManager().find(getEntityClass(), key);
        return findInternalSub(e, key, onlyActive, nullCheck);
    }

    protected ENTITY findInternalSub(final ENTITY e, final KEY key, boolean onlyActive, boolean nullCheck) {
        if (e == null) {
            if (nullCheck) {
                throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, entityNameAndKey(key));
            } else {
                return null;
            }
        }
        if (!readAllowed(getTenantId(e))) {
            LOGGER.error("Attempted access violation in {}.findInternal(): {}", this.getClass().getSimpleName(), entityNameAndKey(key));
            throw new T9tException(T9tException.READ_ACCESS_ONLY_CURRENT_TENANT, entityNameAndKey(key));
        }
        if (onlyActive && !e.ret$Active()) {
            throw new T9tException(T9tException.RECORD_INACTIVE, entityNameAndKey(key));
        }
        return e;
    }
    /**
     * {@inheritDoc}
     *
     * @throws T9tException
     */
    @Override
    public ENTITY find(final KEY key) {
        nullCheck(key);
        ENTITY e = getEntityManager().find(getEntityClass(), key);
        if ((e != null) && !readAllowed(getTenantId(e))) {
            LOGGER.error("Attempted access violation in {}.find(): {}", this.getClass().getSimpleName(), entityNameAndKey(key));
            throw new T9tException(T9tException.READ_ACCESS_ONLY_CURRENT_TENANT, entityNameAndKey(key));
        }
        return e;
    }

    /**
     * {@inheritDoc}
     *
     * @throws T9tException
     */
    @Override
    public ENTITY find(final KEY key, LockModeType lockMode) {
        return findInternal(key, false, false, lockMode);
    }

    /**
     * {@inheritDoc}
     *
     * @throws T9tException
     */
    @Override
    public ENTITY findActive(final KEY key, boolean onlyActive) {
        return findInternal(key, onlyActive, true);
    }

    /**
     * {@inheritDoc}
     *
     * @throws T9tException
     */
    @Override
    public ENTITY findActive(final KEY key, boolean onlyActive, LockModeType lockMode) {
        return findInternal(key, onlyActive, true, lockMode);
    }

    private void logSearch(Class<ENTITY> derivedEntityClass, SearchCriteria searchCriteria, String what) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Search on {} {} (offset={}, limit={}) with filter criteria {}",
                what,
                derivedEntityClass.getCanonicalName(),
                searchCriteria.getOffset(),
                searchCriteria.getLimit(),
                searchCriteria.getSearchFilter() == null ? "NONE" : searchCriteria.getSearchFilter());
        }
    }

    @Override
    public List<KEY> searchKey(SearchCriteria searchCriteria) {
        // Get the criteria builder to start building the query
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();

        // Create basic query without restrictions

        Class<ENTITY> derivedEntityClass = (Class<ENTITY>) getEntityClass();
        CriteriaQuery<KEY> criteriaQuery = criteriaBuilder.createQuery(getKeyClass());
        Root<ENTITY> from = criteriaQuery.from(derivedEntityClass);
        criteriaQuery = criteriaQuery.select(from.<KEY> get(hasArtificialPrimaryKey() ? "objectRef" : "key"));
        if (Boolean.TRUE.equals(searchCriteria.getApplyDistinct()))
            criteriaQuery = criteriaQuery.distinct(true);

        logSearch(derivedEntityClass, searchCriteria, "key of entity");
        return runSearch(searchCriteria, criteriaBuilder, from, criteriaQuery);
    }

    /**
     * {@inheritDoc}
     *
     * @throws T9tException
     */
    @Override
    public List<ENTITY> search(SearchCriteria searchCriteria) {

        // Get the criteria builder to start building the query
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();

        // Create basic query without restrictions
        Class<ENTITY> derivedEntityClass = (Class<ENTITY>) getEntityClass();
        CriteriaQuery<ENTITY> criteriaQuery = criteriaBuilder.createQuery(derivedEntityClass);
        Root<ENTITY> from = criteriaQuery.from(derivedEntityClass);
        criteriaQuery = criteriaQuery.select(from);
        if (Boolean.TRUE.equals(searchCriteria.getApplyDistinct()))
            criteriaQuery = criteriaQuery.distinct(true);

        logSearch(derivedEntityClass, searchCriteria, "entity");
        return runSearch(searchCriteria, criteriaBuilder, from, criteriaQuery);
    }

    /**
     * {@inheritDoc}
     *
     * @throws T9tException
     */
    @Override
    public List<ENTITY> search(SearchCriteria searchCriteria, IDataProcessor<KEY, TRACKING, ENTITY> processor) {
        List<ENTITY> r = search(searchCriteria);
        if (processor != null) {
            for (ENTITY e : r) {
                processor.process(e);
            }
            return new ArrayList<ENTITY>(0);
        }
        return r;
    }

    private <R> List<R> runSearch(SearchCriteria searchCriteria, CriteriaBuilder criteriaBuilder, Root<ENTITY> from, CriteriaQuery<R> select)
            {
        // Add filters, if supplied
        PathResolver r = new PathResolver(getEntityClass(), from);
        JpaCriteriaBuilder bld = new JpaCriteriaBuilder(r, criteriaBuilder);

        Predicate whereList = null;
        if (searchCriteria.getSearchFilter() != null) {
            whereList = bld.buildPredicate(searchCriteria.getSearchFilter());
        }

        // perform special filtering on tenant
        if (isTenantIsolated()) {
            String tenantId = getSharedTenantId();
            if (!tenantId.equals(T9tConstants.GLOBAL_TENANT_ID)) {
                Predicate tenantPredicate = null;
                // regular tenant
                if (isTenantMeOrGlobal()) {
                    // IN (global, me)
                    List<String> tenants = new ArrayList<String>(2);
                    tenants.add(T9tConstants.GLOBAL_TENANT_ID);
                    tenants.add(tenantId);
                    Expression<String> exp = from.get(T9tConstants.TENANT_ID_FIELD_NAME);
                    tenantPredicate = exp.in(tenants);
                } else {
                    tenantPredicate = criteriaBuilder.equal(from.<String> get(T9tConstants.TENANT_ID_FIELD_NAME), tenantId);
                }
                whereList = whereList == null ? tenantPredicate : criteriaBuilder.and(whereList, tenantPredicate);
            } else {
                // global tenant
                if (!globalTenantCanAccessAll()) {
                    Predicate tenantPredicate = criteriaBuilder.equal(from.<String> get(T9tConstants.TENANT_ID_FIELD_NAME), tenantId);
                    whereList = whereList == null ? tenantPredicate : criteriaBuilder.and(whereList, tenantPredicate);
                }
            }
        }

        // Append restrictions to overall query if any available
        if (whereList != null)
            select.where(whereList);

        // determine the effective sort columns
        List<SortColumn> sortColumns = searchCriteria.getSortColumns();
        int limit = searchCriteria.getLimit();
        if ((sortColumns == null) && (searchCriteria.getOffset() != 0 || limit != 0)) {
            // There is no sorting provided, but pagination requested. Add a default sort order.
            sortColumns = getDefaultSortColumns();
        }

        // Sorting, if supplied
        if (sortColumns != null) {
            List<Order> orderList = new ArrayList<>();
            // Walk through the list of sort columns if any
            for (SortColumn column : sortColumns) {
                Path<?> path = r.getPath(column.getFieldName());
                orderList.add(column.getDescending() ? criteriaBuilder.desc(path) : criteriaBuilder.asc(path));
            }
            select.orderBy(orderList);
        }

        // Run the query and return the results
        TypedQuery<R> createQuery = getEntityManager().createQuery(select);

        // Apply pagination from searchOptions
        createQuery.setFirstResult(searchCriteria.getOffset());
        createQuery.setMaxResults(limit > 0 ? limit : Integer.MAX_VALUE);

        return createQuery.getResultList();
    }

    /** Obtains a default sort order to make backend pagination reliable. */
    protected abstract List<SortColumn> getDefaultSortColumns();

    protected void createNewArtificialKeyIfRequired(final ENTITY entity) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final ENTITY entity) {
        if (isTenantIsolated()) {
            // there is a tenantId column in the entity....
            if (getTenantId(entity) == null) {
                // if it's not set, set it to the current tenant...
                setTenantId(entity, getSharedTenantId());
            } else {
                // if set, then verify that we may actually write to this tenant
                if (!writeAllowed(getTenantId(entity))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
            }
        }
        createNewArtificialKeyIfRequired(entity);
        getEntityManager().persist(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENTITY update(final ENTITY entity) {
        if (!writeAllowed(getTenantId(entity))) {
            throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
        }
        return getEntityManager().merge(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final ENTITY entity) {
        if (!writeAllowed(getTenantId(entity))) {
            throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
        }
        getEntityManager().remove(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() {
        getEntityManager().flush();
    }

    /** called by generated resolver subclasses. */
    protected void logFindBy(String name) {
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("{} requested for resolver class {}", name, getClass().getSimpleName());
    }

    /** called by subclasses. */
    protected ENTITY getEntityDataByGenericKey(BonaPortable entityRef, boolean onlyActive) {
        if (entityRef == null) {
            return null;        // play null-safe
        }
        ENTITY e = getEntityDataByGenericKey(entityRef, getEntityClass(), (t, cb) -> t, false);
        if (onlyActive && !e.ret$Active()) {
            throw new T9tException(T9tException.RECORD_INACTIVE, entityNameAndKey(entityRef));
        }
        return e;
    }

    protected <ZZ> ZZ getEntityDataByGenericKey(BonaPortable entityRef, Class<ZZ> clz,
            BiFunction<Root<ENTITY>, CriteriaBuilder, Selection<ZZ>> cvter, boolean addActiveWhereClause) {
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("read by generic key({}) requested for resolver class {}", entityRef, getClass().getSimpleName());

        // no primary key reference available, build a query by alternate key
        // use all fields of the instance of entityRef (which must be not null, else we throw an exception)
        // We want only the ...Key classes here. As a plausibility check, we required the ref class to be final.
        int refClassModifiers = entityRef.getClass().getModifiers();
        if (!Modifier.isFinal(refClassModifiers) || Modifier.isInterface(refClassModifiers)) {
            if (entityRef instanceof Ref && (((Ref)entityRef).getObjectRef() != null)) {
                Long pk = ((Ref)entityRef).getObjectRef();
                LOGGER.debug("getEntityDataByGenericKey called where primary key was available: {}({})", entityRef.ret$PQON(), pk);
                entityRef = new Ref(pk);
            } else {
                LOGGER.error("Bad class {} passed to key resolver", entityRef.getClass().getCanonicalName());
                LOGGER.debug("Class contents is {}", entityRef);
                throw new T9tException(T9tException.RESOLVE_BAD_CLASS, entityRef.getClass().getCanonicalName());
            }
        }
        // further checks here...
        Class<ENTITY> entityClass = getEntityClass();
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<ZZ> criteriaQuery = criteriaBuilder.createQuery(clz);
        Root<ENTITY> from = criteriaQuery.from(entityClass);
        CriteriaQuery<ZZ> select = ((CriteriaQuery<ZZ>) criteriaQuery).select(cvter.apply(from, criteriaBuilder));
        List<Predicate> whereList = new ArrayList<Predicate>();
        String tenantId = null;
        Object entRef = entityRef;

        try {
            @SuppressWarnings("unused")
            boolean tenantIdSet = false; // we may want this for diagnosis
            for (Field f : entityRef.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (!Modifier.isStatic(f.getModifiers())) {
                    if (f.getType() == int.class) {
                        Integer ref = Integer.valueOf(f.getInt(entityRef));
                        entRef = ref;
                        whereList.add(criteriaBuilder.equal(from.<Integer> get(f.getName()), ref));
                    } else if (f.getType() == long.class) {
                        Long ref = Long.valueOf(f.getLong(entityRef));
                        entRef = ref;
                        whereList.add(criteriaBuilder.equal(from.<Long> get(f.getName()), ref));
                    } else {
                        // any Object (includes wrapper classes such as Long)
                        entRef = getFieldValue(f, entityRef);
                        whereList.add(criteriaBuilder.equal(from.get(f.getName()), entRef));
                        if (f.getName().equals("tenantId")) {
                            tenantIdSet = true;
                        }
                    }
                }
            }
            // if tenantIdSet is false, but the entity contains a tenantId field, then set it from the internalHeaderParameters
            if (isTenantIsolated()) { // TODO: shouldn't we have " && !tenantIdSet" here???
                LOGGER.trace("adding implicit tenantId for query of entity {}", getBaseJpaEntityClass().getCanonicalName());
                tenantId = getSharedTenantId();
                if (!tenantId.equals(T9tConstants.GLOBAL_TENANT_ID)) {
                    // regular tenant
                    if (isTenantMeOrGlobal()) {
                        // IN (global, me)
                        List<String> tenants = new ArrayList<String>(2);
                        tenants.add(T9tConstants.GLOBAL_TENANT_ID);
                        tenants.add(tenantId);
                        Expression<String> exp = from.get(T9tConstants.TENANT_ID_FIELD_NAME);
                        whereList.add(exp.in(tenants));
                        select.orderBy(criteriaBuilder.desc(from.get(T9tConstants.TENANT_ID_FIELD_NAME)));   // desc to ensure current tenant entry is first
                    } else {
                        whereList.add(criteriaBuilder.equal(from.<String> get(T9tConstants.TENANT_ID_FIELD_NAME), tenantId));
                    }
                } else {
                    // global tenant
                    if (!globalTenantCanAccessAll()) {
                        whereList.add(criteriaBuilder.equal(from.<String> get(T9tConstants.TENANT_ID_FIELD_NAME), tenantId));
                    } else {
                        select.orderBy(criteriaBuilder.asc(from.get(T9tConstants.TENANT_ID_FIELD_NAME)));     // asc to ensure current tenant entry is first
                    }
                }
            }
        } catch (IllegalArgumentException e1) {
            LOGGER.error("resolver caused IllegalArgumentException", e1);
            throw new T9tException(T9tException.RESOLVE_PARAMETER, entityRef.ret$PQON());
        } catch (IllegalAccessException e1) {
            LOGGER.error("resolver caused IllegalAccessException", e1);
            throw new T9tException(T9tException.RESOLVE_ACCESS, entityRef.ret$PQON());
        }
        // add criteria to isActive, if desired
        if (addActiveWhereClause) {
            whereList.add(criteriaBuilder.equal(from.<Boolean> get("isActive"), true));
        }
        // Append restrictions to overall query if any available
        if (whereList.size() > 0) {
            Predicate[] predicates = new Predicate[whereList.size()];
            whereList.toArray(predicates);
            select.where(criteriaBuilder.and(predicates));
        } else {
            // no parameters found! Problem
            LOGGER.error("resolver called with a reference object without fields: {}", entityRef);
            throw new T9tException(T9tException.MISSING_KEY_PARAMETER, entityRef.ret$PQON());
        }

        // Run the query and return the results
        TypedQuery<ZZ> query = getEntityManager().createQuery(select);
        List<ZZ> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            // this can be a regular business case and is not necessarily an error
            LOGGER.debug("No result for fetching entity data for tenantId {}, entityClass {}, objectRef {}", tenantId, entityClass, entRef);
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, entityNameAndKey(entRef));
        }
        if (resultList.size() > 2) {
            // not plausible - e really do not expect this
            LOGGER.debug("Expected unique key - Too many results for fetching entity data for tenantId {}, entityClass {}, objectRef {}", tenantId, entityClass, entRef);
            throw new T9tException(T9tException.TOO_MANY_RECORDS, entityNameAndKey(entRef));
        }
        // one or two results...
        // if one, use it, else use the one for the specific tenant
        return resultList.get(0);
    }

    private Object getFieldValue(Field f, BonaPortable entityRef) throws IllegalArgumentException, IllegalAccessException {
        Object entRef = null;
        if (f.getType().isEnum()) {
            if (TokenizableEnum.class.isAssignableFrom(f.getType())) {
                TokenizableEnum tokenizableEnum = (TokenizableEnum) f.get(entityRef);
                if (tokenizableEnum != null) {
                    entRef = tokenizableEnum.getToken();
                }
            } else {
                Enum<?> enumz = (Enum<?>) f.get(entityRef);
                if (enumz != null) {
                    entRef = enumz.ordinal();
                }
            }
        } else {
            entRef = f.get(entityRef);
        }

        return entRef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypedQuery<ENTITY> constructQuery(String query) {
        return getEntityManager().createQuery(query, getEntityClass());
    }

    @Override
    public ENTITY getEntityDataForKey(KEY key, boolean onlyActive) {
        ENTITY e = find(key);
        if (e == null)
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, entityNameAndKey(key));
        if (onlyActive && !e.ret$Active())
            throw new T9tException(T9tException.RECORD_INACTIVE, entityNameAndKey(key));
        return e;
    }
}
