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
package com.arvatosystems.t9t.base.jpa.impl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.arvatosystems.t9t.base.T9tUtil;
import jakarta.persistence.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.jpa.IDataProcessor;
import com.arvatosystems.t9t.base.jpa.IResolverAnyKey;
import com.arvatosystems.t9t.base.search.DummySearchCriteria;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.jpa.api.JpaCriteriaBuilder;
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.AggregateColumn;
import de.jpaw.bonaparte.pojos.api.AggregateFunctionType;
import de.jpaw.bonaparte.pojos.api.BooleanFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Alternative;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.enums.TokenizableEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

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
    public String getTenantId(final ENTITY e) {
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
    public String getSharedTenantId(final RequestContext ctx) {
        return ctx.tenantMapping.getSharedTenantId(getRtti());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTenantId(final ENTITY e, final String tenantId) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOfMatchingTenant(final ENTITY e) {
        return !isTenantIsolated() || getSharedTenantId().equals(getTenantId(e));
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
        final RequestContext ctx = contextProvider.get();
        final ENTITY e = ctx.customization.newEntityInstance(getRtti(), getBaseJpaEntityClass());
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
    public boolean readAllowed(final String tenantId) {
        if (isTenantIsolated()) {
            final String myTenant = getSharedTenantId();
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
    public boolean writeAllowed(final String tenantId) {
        final String myTenant = getSharedTenantId();
        if (myTenant.equals(tenantId)) {
            return true; // always access to own tenant allowed
        }
        if (isTenantIsolated()) {
            // access to different tenant to be clarified by additional parameters
            if (myTenant.equals(T9tConstants.GLOBAL_TENANT_ID)) {
                return globalTenantCanAccessAll();
                // return false; // regular tenant can never modify the global tenant's data, globalTenantCanAccessAll defines read access only
            } else {
                return false; // regular tenant can never modify the global tenant's data
            }
        } else {
            // write access only for the global tenant!
            return myTenant.equals(T9tConstants.GLOBAL_TENANT_ID);
        }
    }

    private static final SearchCriteria SEARCH_CRITERIA_ONLY_ACTIVE = new DummySearchCriteria();
    private static final SearchCriteria SEARCH_CRITERIA_ANY = new DummySearchCriteria();
    static {
        SEARCH_CRITERIA_ONLY_ACTIVE.setSearchFilter(new BooleanFilter("isActive", true));
        SEARCH_CRITERIA_ONLY_ACTIVE.freeze();
        SEARCH_CRITERIA_ANY.freeze();
    }

    @Override
    public List<ENTITY> readAll(final boolean onlyActive) {
        return search(onlyActive ? SEARCH_CRITERIA_ONLY_ACTIVE : SEARCH_CRITERIA_ANY);
    }

    @Override
    public String entityNameAndKey(final Object key) {
        return getBaseJpaEntityClass().getSimpleName() + ": "
          + (key instanceof BonaPortable ? key.toString() : "(" + key.getClass().getSimpleName() + ")" + key.toString());
    }

    // plausi check for find operations. Done because hibernate's exception is not really self explanatory
    protected void nullCheck(final KEY key) {
        if (key == null) {
            LOGGER.error("{}: Attempt to find entity of type {} with null supplied as key", getClass().getSimpleName(), getEntityClass().getSimpleName());
            throw new T9tException(T9tException.FIND_ON_NULL_KEY, getClass().getSimpleName() + ": " + getEntityClass().getSimpleName());
        }
        // also log activity on higher levels
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("find({}); requested for resolver class {}", key, getClass().getSimpleName());
    }

    private ENTITY findInternalSub(final ENTITY e, final KEY key, final boolean allowNull) {
        if (e == null) {
            if (allowNull) {
                return null;
            }
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, entityNameAndKey(key));
        } else if (!readAllowed(getTenantId(e))) {
            LOGGER.error("Attempted access violation in {}.findInternal(): {}", this.getClass().getSimpleName(), entityNameAndKey(key));
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
    public ENTITY find(final KEY key) {
        nullCheck(key);
        final ENTITY e = getEntityManager().find(getEntityClass(), key);
        return findInternalSub(e, key, true);
    }

    /**
     * {@inheritDoc}
     *
     * @throws T9tException
     */
    @Override
    public ENTITY find(final KEY key, final LockModeType lockMode) {
        nullCheck(key);
        final ENTITY e = getEntityManager().find(getEntityClass(), key, lockMode == null ? LockModeType.PESSIMISTIC_WRITE : lockMode);
        return findInternalSub(e, key, true);
    }

    private void logSearch(final Class<ENTITY> derivedEntityClass, final SearchCriteria searchCriteria, final String what) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Search on {} {} (offset={}, limit={}, distinct={}) with filter criteria {}",
                what,
                derivedEntityClass.getCanonicalName(),
                searchCriteria.getOffset(),
                searchCriteria.getLimit(),
                searchCriteria.getApplyDistinct(),
                searchCriteria.getSearchFilter() == null ? "NONE" : searchCriteria.getSearchFilter());
        }
    }

    @Override
    public List<KEY> searchKey(final SearchCriteria searchCriteria) {
        // Get the criteria builder to start building the query
        final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();

        // Create basic query without restrictions

        final Class<ENTITY> derivedEntityClass = getEntityClass();
        CriteriaQuery<KEY> criteriaQuery = criteriaBuilder.createQuery(getKeyClass());
        final Root<ENTITY> from = criteriaQuery.from(derivedEntityClass);
        criteriaQuery = criteriaQuery.select(from.<KEY>get(hasArtificialPrimaryKey() ? "objectRef" : "key"));
        if (Boolean.TRUE.equals(searchCriteria.getApplyDistinct()))
            criteriaQuery = criteriaQuery.distinct(true);

        logSearch(derivedEntityClass, searchCriteria, "key of entity");
        return runSearch(searchCriteria, criteriaBuilder, from, criteriaQuery, null);
    }

    /**
     * {@inheritDoc}
     *
     * @throws T9tException
     */
    @Override
    public List<ENTITY> search(final SearchCriteria searchCriteria) {

        // Get the criteria builder to start building the query
        final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();

        // Create basic query without restrictions
        final Class<ENTITY> derivedEntityClass = getEntityClass();
        CriteriaQuery<ENTITY> criteriaQuery = criteriaBuilder.createQuery(derivedEntityClass);
        final Root<ENTITY> from = criteriaQuery.from(derivedEntityClass);

        Map<String, Expression<?>> aggregateMap = null;
        if (T9tUtil.isEmpty(searchCriteria.getGroupByColumns())) {
            // no group by columns provided
            criteriaQuery = criteriaQuery.select(from);
            if (Boolean.TRUE.equals(searchCriteria.getApplyDistinct())) {
                criteriaQuery = criteriaQuery.distinct(true);
            }
        } else {
            // group by columns are provided
            final List<Expression<?>> groupBy = new ArrayList<>(searchCriteria.getGroupByColumns().size());
            for (String columns: searchCriteria.getGroupByColumns()) {
                groupBy.add(from.get(columns));
            }
            criteriaQuery = criteriaQuery.groupBy(groupBy);

            // handle column aggregation
            final Field[] allFields = derivedEntityClass.getDeclaredFields();
            final List<Selection<?>> selections = new ArrayList<>(allFields.length);
            aggregateMap = new HashMap<>(allFields.length);
            if (searchCriteria.getAggregateColumns() != null) {
                populateAggregatedSelections(criteriaBuilder, from, searchCriteria.getAggregateColumns(), selections, aggregateMap);
            }
            // populate default aggregated selections for the remaining columns
            populateDefaultAggregatedSelections(criteriaBuilder, from, allFields, searchCriteria.getGroupByColumns(), aggregateMap, selections);
            criteriaQuery = criteriaQuery.multiselect(selections);
        }

        logSearch(derivedEntityClass, searchCriteria, "entity");
        return runSearch(searchCriteria, criteriaBuilder, from, criteriaQuery, aggregateMap);
    }

    @Override
    public Long count(final SearchFilter filter, final Boolean applyDistinct) {
        // Get the criteria builder to start building the query
        final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();

        // Create basic query without restrictions
        final Class<ENTITY> derivedEntityClass = getEntityClass();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        final Root<ENTITY> from = criteriaQuery.from(derivedEntityClass);
        criteriaQuery.select(criteriaBuilder.count(from));
        if (Boolean.TRUE.equals(applyDistinct))
            criteriaQuery = criteriaQuery.distinct(true);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SELECT COUNT on {} (distinct={}) with filter criteria {}",
                derivedEntityClass.getCanonicalName(),
                applyDistinct,
                filter == null ? "NONE" : filter);
        }
        // apply and specific filters and also the tenant restriction
        createWhereList(filter, criteriaBuilder, from, criteriaQuery);
        // Run the query and return the results
        return getEntityManager().createQuery(criteriaQuery).getSingleResult();
    }

    /**
     * {@inheritDoc}
     *
     * @throws T9tException
     */
    @Override
    public List<ENTITY> search(final SearchCriteria searchCriteria, final IDataProcessor<KEY, TRACKING, ENTITY> processor) {
        final List<ENTITY> r = search(searchCriteria);
        if (processor != null) {
            for (final ENTITY e : r) {
                processor.process(e);
            }
            return new ArrayList<>(0);
        }
        return r;
    }

    /** Creates the WHERE conditions on the SELECT query. */
    private <R> void createWhereList(final SearchFilter filter, final CriteriaBuilder criteriaBuilder, final Root<ENTITY> from, final CriteriaQuery<R> select) {
        // Add filters, if supplied
        final PathResolver r = new PathResolver(getEntityClass(), from);
        final JpaCriteriaBuilder bld = new JpaCriteriaBuilder(r, criteriaBuilder);

        Predicate whereList = null;
        if (filter != null) {
            whereList = bld.buildPredicate(filter);
        }

        // perform special filtering on tenant
        if (isTenantIsolated()) {
            final String tenantId = getSharedTenantId();
            if (!tenantId.equals(T9tConstants.GLOBAL_TENANT_ID)) {
                Predicate tenantPredicate = null;
                // regular tenant
                if (isTenantMeOrGlobal()) {
                    // IN (global, me)
                    final List<String> tenants = new ArrayList<>(2);
                    tenants.add(T9tConstants.GLOBAL_TENANT_ID);
                    tenants.add(tenantId);
                    final Expression<String> exp = from.get(T9tConstants.TENANT_ID_FIELD_NAME);
                    tenantPredicate = exp.in(tenants);
                } else {
                    tenantPredicate = criteriaBuilder.equal(from.<String>get(T9tConstants.TENANT_ID_FIELD_NAME), tenantId);
                }
                whereList = whereList == null ? tenantPredicate : criteriaBuilder.and(whereList, tenantPredicate);
            } else {
                // global tenant
                if (!globalTenantCanAccessAll()) {
                    final Predicate tenantPredicate = criteriaBuilder.equal(from.<String>get(T9tConstants.TENANT_ID_FIELD_NAME), tenantId);
                    whereList = whereList == null ? tenantPredicate : criteriaBuilder.and(whereList, tenantPredicate);
                }
            }
        }
        // Append restrictions to overall query if any available
        if (whereList != null) {
            select.where(whereList);
        }
    }

    private <R> List<R> runSearch(final SearchCriteria searchCriteria, final CriteriaBuilder criteriaBuilder,
      final Root<ENTITY> from, final CriteriaQuery<R> select, final Map<String, Expression<?>> aggregateMap) {

        createWhereList(searchCriteria.getSearchFilter(), criteriaBuilder, from, select);

        // determine the effective sort columns
        List<SortColumn> sortColumns = searchCriteria.getSortColumns();
        final int limit = searchCriteria.getLimit();
        if (searchCriteria.getOffset() != 0 || limit != 0) {
            // pagination requested
            final List<SortColumn> defaultColumns = getDefaultSortColumns();
            if (sortColumns == null || sortColumns.isEmpty()) {
                // There is no sorting provided. Add a default sort order.
                sortColumns = defaultColumns;
            } else {
                // merge provided sort columns and default sort columns
                final List<SortColumn> sortColumnsToAdd = new ArrayList<>(defaultColumns.size());
                for (final SortColumn defaultColumn : defaultColumns) {
                    boolean found = false;
                    for (final SortColumn sortColumn : sortColumns) {
                        if (defaultColumn.getFieldName().equalsIgnoreCase(sortColumn.getFieldName())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        sortColumnsToAdd.add(defaultColumn);
                    }
                }
                if (!sortColumnsToAdd.isEmpty()) {
                    // the provided sort columns can be immutable, we have to merge into a new Array
                    final List<SortColumn> mergedSortColumns = new ArrayList<>(sortColumns.size() + sortColumnsToAdd.size());
                    mergedSortColumns.addAll(sortColumns);
                    mergedSortColumns.addAll(sortColumnsToAdd);
                    sortColumns = mergedSortColumns;
                }
            }
        }

        // Sorting, if supplied
        if (sortColumns != null && !sortColumns.isEmpty()) {
            final PathResolver r = new PathResolver(getEntityClass(), from);
            final List<Order> orderList = new ArrayList<>(sortColumns.size());
            // Walk through the list of sort columns if any
            for (final SortColumn column : sortColumns) {
                final String fieldName = column.getFieldName();
                if (aggregateMap != null && aggregateMap.containsKey(fieldName)) {
                    // for now, only first level of fields are supported for sort with aggregation
                    if (fieldName.indexOf('.') == -1) {
                        final Expression<?> selection = aggregateMap.get(column.getFieldName());
                        orderList.add(column.getDescending() ? criteriaBuilder.desc(selection) : criteriaBuilder.asc(selection));
                    }
                } else {
                    final Path<?> path = r.getPath(column.getFieldName());
                    orderList.add(column.getDescending() ? criteriaBuilder.desc(path) : criteriaBuilder.asc(path));
                }
            }
            if (!orderList.isEmpty()) {
                select.orderBy(orderList);
            }
        }

        // Run the query and return the results
        final TypedQuery<R> createQuery = getEntityManager().createQuery(select);

        // Apply pagination from searchOptions
        createQuery.setFirstResult(searchCriteria.getOffset());
        createQuery.setMaxResults(limit > 0 ? limit : Integer.MAX_VALUE);

        return createQuery.getResultList();
    }

    /** Obtains a default sort order to make backend pagination reliable. */
    protected abstract List<SortColumn> getDefaultSortColumns();

    protected void createNewArtificialKeyIfRequired(final ENTITY entity) { }

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
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("merge requested for resolver class {}", getClass().getSimpleName());
        return entity; // NOOP! getEntityManager().merge(entity);
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
    protected void logFindBy(final String name) {
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("{} requested for resolver class {}", name, getClass().getSimpleName());
    }

    /** called by subclasses. */
    protected ENTITY getEntityDataByGenericKey(final BonaPortable entityRef) {
        if (entityRef == null) {
            return null;        // play null-safe
        }
        return getEntityDataByGenericKey(entityRef, getEntityClass(), (t, cb) -> t);
    }

    protected <ZZ> ZZ getEntityDataByGenericKey(BonaPortable entityRef, final Class<ZZ> clz,
            final BiFunction<Root<ENTITY>, CriteriaBuilder, Selection<ZZ>> cvter) {
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("read by generic key({}) requested for resolver class {}", entityRef, getClass().getSimpleName());

        // no primary key reference available, build a query by alternate key
        // use all fields of the instance of entityRef (which must be not null, else we throw an exception)
        // We want only the ...Key classes here. As a plausibility check, we required the ref class to be final.
        final int refClassModifiers = entityRef.getClass().getModifiers();
        if (!Modifier.isFinal(refClassModifiers) || Modifier.isInterface(refClassModifiers)) {
            // in some cases, a Ref might be passed... Work around and create a Ref which does not contain other fields
            if (entityRef instanceof Ref eRef && eRef.getObjectRef() != null) {
                final Long pk = eRef.getObjectRef();
                LOGGER.debug("getEntityDataByGenericKey called where primary key was available: {}({})", entityRef.ret$PQON(), pk);
                entityRef = new Ref(pk);
            } else {
                LOGGER.error("Bad class {} passed to key resolver", entityRef.getClass().getCanonicalName());
                LOGGER.debug("Class contents is {}", entityRef);
                final T9tException e = new T9tException(T9tException.RESOLVE_BAD_CLASS, entityRef.getClass().getCanonicalName());
                LOGGER.error("Stack trace is ", e);
                throw e;
            }
        }
        // further checks here...
        final Class<ENTITY> entityClass = getEntityClass();
        final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<ZZ> criteriaQuery = criteriaBuilder.createQuery(clz);
        final Root<ENTITY> from = criteriaQuery.from(entityClass);
        final CriteriaQuery<ZZ> select = criteriaQuery.select(cvter.apply(from, criteriaBuilder));
        final List<Predicate> whereList = new ArrayList<>();
        String tenantId = null;
        Object entRef = entityRef;

        try {
            @SuppressWarnings("unused")
            boolean tenantIdSet = false; // we may want this for diagnosis
            for (final Field f : entityRef.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (!Modifier.isStatic(f.getModifiers())) {
                    if (f.getType() == int.class) {
                        final Integer ref = Integer.valueOf(f.getInt(entityRef));
                        entRef = ref;
                        whereList.add(criteriaBuilder.equal(from.<Integer>get(f.getName()), ref));
                    } else if (f.getType() == long.class) {
                        final Long ref = Long.valueOf(f.getLong(entityRef));
                        entRef = ref;
                        whereList.add(criteriaBuilder.equal(from.<Long>get(f.getName()), ref));
                    } else {
                        // any Object (includes wrapper classes such as Long)
                        entRef = getFieldValue(f, entityRef);
                        whereList.add(criteriaBuilder.equal(from.get(f.getName()), entRef));
                        if (f.getName().equals(T9tConstants.TENANT_ID_FIELD_NAME)) {
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
                        final List<String> tenants = new ArrayList<>(2);
                        tenants.add(T9tConstants.GLOBAL_TENANT_ID);
                        tenants.add(tenantId);
                        final Expression<String> exp = from.get(T9tConstants.TENANT_ID_FIELD_NAME);
                        whereList.add(exp.in(tenants));
                        select.orderBy(criteriaBuilder.desc(from.get(T9tConstants.TENANT_ID_FIELD_NAME)));   // desc to ensure current tenant entry is first
                    } else {
                        whereList.add(criteriaBuilder.equal(from.<Long>get(T9tConstants.TENANT_ID_FIELD_NAME), tenantId));
                    }
                } else {
                    // global tenant
                    if (!globalTenantCanAccessAll()) {
                        whereList.add(criteriaBuilder.equal(from.<String>get(T9tConstants.TENANT_ID_FIELD_NAME), tenantId));
                    } else {
                        select.orderBy(criteriaBuilder.asc(from.get(T9tConstants.TENANT_ID_FIELD_NAME)));    // asc to ensure current tenant entry is first
                    }
                }
            }
        } catch (final IllegalArgumentException e1) {
            LOGGER.error("resolver caused IllegalArgumentException", e1);
            throw new T9tException(T9tException.RESOLVE_PARAMETER, entityRef.ret$PQON());
        } catch (final IllegalAccessException e1) {
            LOGGER.error("resolver caused IllegalAccessException", e1);
            throw new T9tException(T9tException.RESOLVE_ACCESS, entityRef.ret$PQON());
        }
        // Append restrictions to overall query if any available
        if (whereList.size() > 0) {
            final Predicate[] predicates = new Predicate[whereList.size()];
            whereList.toArray(predicates);
            select.where(criteriaBuilder.and(predicates));
        } else {
            // no parameters found! Problem
            LOGGER.error("resolver called with a reference object without fields: {}", entityRef);
            throw new T9tException(T9tException.MISSING_KEY_PARAMETER, entityRef.ret$PQON());
        }

        // Run the query and return the results
        final TypedQuery<ZZ> query = getEntityManager().createQuery(select);
        final List<ZZ> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            // this can be a regular business case and is not necessarily an error
            LOGGER.debug("No result for fetching entity data for tenantId {}, entityClass {}, key {}", tenantId, entityClass, entRef);
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, entityNameAndKey(entRef));
        }
        if (resultList.size() > 2) {
            // not plausible - e really do not expect this
            LOGGER.debug("Expected unique key - Too many results for fetching entity data for tenantId {}, entityClass {}, key {}",
              tenantId, entityClass, entRef);
            throw new T9tException(T9tException.TOO_MANY_RECORDS, entityNameAndKey(entRef));
        }
        // one or two results...
        // if one, use it, else use the one for the specific tenant
        return resultList.get(0);
    }

    private Object getFieldValue(final Field f, final BonaPortable entityRef) throws IllegalArgumentException, IllegalAccessException {
        Object entRef = null;
        if (f.getType().isEnum()) {
            if (TokenizableEnum.class.isAssignableFrom(f.getType())) {
                final TokenizableEnum tokenizableEnum = (TokenizableEnum) f.get(entityRef);
                if (tokenizableEnum != null) {
                    entRef = tokenizableEnum.getToken();
                }
            } else {
                final Enum<?> enumz = (Enum<?>) f.get(entityRef);
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
    public TypedQuery<ENTITY> constructQuery(final String query) {
        return getEntityManager().createQuery(query, getEntityClass());
    }

    @Override
    public ENTITY getEntityDataForKey(final KEY key) {
        nullCheck(key);
        final ENTITY e = getEntityManager().find(getEntityClass(), key);
        return findInternalSub(e, key, false);
    }

    // populate aggregated selections based on the given aggregate columns
    private void populateAggregatedSelections(final CriteriaBuilder criteriaBuilder, final Root<ENTITY> from, final List<AggregateColumn> aggregateColumns,
        final List<Selection<?>> selections, final Map<String, Expression<?>> aggregateMap) {
        for (AggregateColumn aggregateColumn : aggregateColumns) {
            final String fieldName = aggregateColumn.getFieldName();
            final Expression<?> expression = applyAggregateFunction(criteriaBuilder, from, aggregateColumn.getFunction(), fieldName);
            selections.add(expression.alias(fieldName));
            aggregateMap.put(fieldName, expression);
        }
    }

    // populate default aggregated selections for the remaining columns
    private void populateDefaultAggregatedSelections(final CriteriaBuilder criteriaBuilder, final Root<ENTITY> from, final Field[] allFields,
        final List<String> groupByColumns, final Map<String, Expression<?>> aggregateMap, final List<Selection<?>> selections) {
        for (Field field : allFields) {
            final Annotation classAnnotation = field.getAnnotation(Column.class);
            final String fieldName = field.getName();
            final Expression<?> expression;
            if (classAnnotation == null || aggregateMap.containsKey(fieldName)) {
                continue;
            }
            if (groupByColumns.contains(fieldName)) {
                // column is part of group by, no need to aggregate
                expression = from.get(fieldName);
            } else {
                if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                    // for boolean fields, just use false as default aggregation value
                    expression = criteriaBuilder.literal(false);
                } else {
                    // use max as default aggregation function
                    expression = criteriaBuilder.max(from.get(fieldName));
                }
                aggregateMap.put(fieldName, expression);
            }
            selections.add(expression.alias(fieldName));
        }
    }

    private Expression<?> applyAggregateFunction(final CriteriaBuilder criteriaBuilder, final Root<ENTITY> from, final AggregateFunctionType functionType,
        final String fieldName) {
        return switch (functionType) {
            case SUM -> criteriaBuilder.sum(from.get(fieldName));
            case AVG -> criteriaBuilder.avg(from.get(fieldName));
            case MAX -> criteriaBuilder.max(from.get(fieldName));
            case MIN -> criteriaBuilder.min(from.get(fieldName));
            case COUNT -> criteriaBuilder.count(from.get(fieldName));
            case COUNT_DISTINCT -> criteriaBuilder.countDistinct(from.get(fieldName));
            default -> throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, functionType.name());
        };
    }
}
