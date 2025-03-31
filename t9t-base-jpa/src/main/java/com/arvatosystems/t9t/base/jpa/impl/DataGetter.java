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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.jpa.IDataGetter;
import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey;
import com.arvatosystems.t9t.base.jpa.ormspecific.IQueryHintSetter;
import com.arvatosystems.t9t.base.search.GetDataResponse;
import com.google.common.collect.Iterables;

import de.jpaw.bonaparte.jpa.BonaData;
import de.jpaw.bonaparte.jpa.BonaKey;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class DataGetter implements IDataGetter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataGetter.class);
    protected final IQueryHintSetter queryHintSetter = Jdp.getRequired(IQueryHintSetter.class);

    @Override
    public <D extends Ref> GetDataResponse<D> query(final IResolverSurrogateKey resolver, final String constructor,
      final Class<D> keyClass, final Set<Long> refs) {
        final GetDataResponse<D> resp = new GetDataResponse<>();
        if (refs.isEmpty()) {
            // shortcut 0 entries (should not happen, but would lead to an error if not treated separately here)
            resp.setData(Collections.emptyList());
            return resp;
        }
        final String queryString = "SELECT " + constructor + " FROM " + resolver.getBaseJpaEntityClass().getSimpleName()
           + " e WHERE e.objectRef IN :refs AND e.tenantId = :tenantId";
        final List<D> result = refs.size() <= 1000
          ? directQuery(queryString, resolver, keyClass, refs)
          : queryWithOracleDbLimitationWorkaround(queryString, resolver, keyClass, refs);
        if (result.size() != refs.size()) {
            LOGGER.error("Requests pojos for {}: {} refs, but only got {}", keyClass.getSimpleName(), refs.size(), result.size());
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "refs for " + keyClass.getSimpleName());
        }
        resp.setData(result);
        return resp;
    }

    protected <D> List<D> directQuery(final String queryString, final IResolverSurrogateKey resolver, final Class<D> keyClass, final Collection<Long> refs) {
        final TypedQuery<D> q = resolver.getEntityManager().createQuery(queryString, keyClass);
        q.setParameter("tenantId", resolver.getSharedTenantId());
        q.setParameter("refs", refs);
        queryHintSetter.setReadOnly(q);
        queryHintSetter.setComment(q, "NQ:selectPojo");
        return q.getResultList();
    }

    protected <D> List<D> queryWithOracleDbLimitationWorkaround(final String queryString, final IResolverSurrogateKey resolver,
      final Class<D> keyClass, final Set<Long> refs) {
        LOGGER.warn("Query on {} refs of class {} requested - splitting", refs.size(), keyClass.getSimpleName());
        final List<D> resultList = new ArrayList<>(refs.size());
        for (final List<Long> partition : Iterables.partition(refs, 1000)) {
            resultList.addAll(directQuery(queryString, resolver, keyClass, partition));
        }
        return resultList;
    }

    @Override
    public <D extends Ref, E extends BonaKey<Long> & BonaData<D>> GetDataResponse<D>
       query(final IResolverSurrogateKey resolver, final Class<E> entityClass, final Class<D> keyClass, final Set<Long> refs) {
        return query(resolver, entityClass, keyClass, refs, null, null);
    }

    @Override
    public <D extends Ref, E extends BonaKey<Long> & BonaData<D>> GetDataResponse<D>
      query(final IResolverSurrogateKey resolver, final Class<E> entityClass, final Class<D> keyClass, final Set<Long> refs,
      final String entityGraphName, final BiConsumer<D, E> updater) {
        final GetDataResponse<D> resp = new GetDataResponse<>();
        if (refs.isEmpty()) {
            // shortcut 0 entries (should not happen, but would lead to an error if not treated separately here)
            resp.setData(Collections.emptyList());
            return resp;
        }
        final String queryString = "SELECT e FROM "
          + resolver.getBaseJpaEntityClass().getSimpleName()
          + " e WHERE e.objectRef IN :refs AND e.tenantId = :tenantId";
        final List<E> result = refs.size() <= 1000
          ? directQuery(queryString, resolver, entityGraphName, refs)
          : queryWithOracleDbLimitationWorkaround(queryString, resolver, entityGraphName, refs);

        if (result.size() != refs.size()) {
            LOGGER.error("Requests data for {}: {} refs, but only got {}", keyClass.getSimpleName(), refs.size(), result.size());
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "refs for " + keyClass.getSimpleName());
        }
        final List<D> mapped = new ArrayList<>(result.size());
        for (final E e: result) {
            final D d = e.ret$Data();
            d.setObjectRef(e.ret$Key());
            if (updater != null)
                updater.accept(d, e);
            mapped.add(d);
        }
        resp.setData(mapped);
        return resp;
    }

    protected <E> List<E> directQuery(final String queryString, final IResolverSurrogateKey resolver, final String entityGraphName,
      final Collection<Long> refs) {
        final TypedQuery<E> q = resolver.getEntityManager().createQuery(queryString, resolver.getBaseJpaEntityClass());
        q.setParameter("tenantId", resolver.getSharedTenantId());
        q.setParameter("refs", refs);
        queryHintSetter.setReadOnly(q);
        queryHintSetter.setComment(q, "NQ:selectData");
        if (entityGraphName != null) {
            queryHintSetter.setComment(q, "NQ:selectDataWithEG_" + entityGraphName);
            final EntityGraph entityGraph = resolver.getEntityManager().getEntityGraph(entityGraphName);
            q.setHint("jakarta.persistence.loadgraph", entityGraph);
        } else {
            queryHintSetter.setComment(q, "NQ:selectData");
        }
        return q.getResultList();
    }

    protected <E> List<E> queryWithOracleDbLimitationWorkaround(final String queryString, final IResolverSurrogateKey resolver,
      final String entityGraphName, final Set<Long> refs) {
        LOGGER.warn("Query on {} refs of class {} requested - splitting", refs.size(), resolver.getBaseJpaEntityClass().getSimpleName());
        final List<E> resultList = new ArrayList<>(refs.size());
        for (final List<Long> partition : Iterables.partition(refs, 1000)) {
            resultList.addAll(directQuery(queryString, resolver, entityGraphName, partition));
        }
        return resultList;
    }
}
