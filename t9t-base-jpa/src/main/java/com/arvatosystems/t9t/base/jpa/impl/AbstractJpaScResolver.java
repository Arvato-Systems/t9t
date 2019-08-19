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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.arvatosystems.t9t.base.jpa.IEntityMapper42;
import com.arvatosystems.t9t.base.jpa.IResolverSuperclassKey42;
import com.arvatosystems.t9t.base.search.DummySearchCriteria;
import com.arvatosystems.t9t.base.search.SearchCriteria;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.AbstractRef;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.refs.PersistenceException;
import de.jpaw.bonaparte.refsc.RefResolver;
import de.jpaw.dp.Jdp;

public abstract class AbstractJpaScResolver<REF extends AbstractRef, KEY extends REF, DTO extends KEY, TRACKING extends TrackingBase, ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>>
   implements RefResolver<REF, KEY, DTO, TRACKING> {

    protected final String entityName;
    protected final IResolverSuperclassKey42<REF, KEY, TRACKING, ENTITY> resolver;
    protected final IEntityMapper42<KEY, DTO, TRACKING, ENTITY> mapper;

    protected AbstractJpaScResolver(String name,
            IResolverSuperclassKey42<REF, KEY, TRACKING, ENTITY> r,
            IEntityMapper42<KEY, DTO, TRACKING, ENTITY> m) {
        entityName = name;
        resolver = r;
        mapper = m;
    }

    protected ENTITY getEntityForKeyOrThrow(KEY pk) {
        ENTITY e = resolver.find(pk);
        if (e == null)
            throw new PersistenceException(PersistenceException.RECORD_DOES_NOT_EXIST, pk.ret$RefP(), entityName);
        return e;
    }

    @Override
    public void clear() {
        resolver.getEntityManager().clear();
    }

    @Override
    public void create(DTO dto) {
        resolver.save(mapper.mapToEntity(dto, false));
    }

    @Override
    public void flush() {
        resolver.getEntityManager().flush();
    }

//  @Override
//  public DTO getDTO(KEY ref) {
//      return mapper.mapToDto(resolver.find(ref));
//  }

    @Override
    public DTO getDTO(REF ref) {
        return mapper.mapToDto(resolver.getEntityData(ref, false));
    }

//  @Override
//  public KEY getRef(REF ref) {
//      return resolver.getRef(ref, false);
//  }

    @Override
    public TRACKING getTracking(KEY pk) {
        return getEntityForKeyOrThrow(pk).ret$Tracking();
    }

    @Override
    public void remove(KEY ref) {
        resolver.remove(resolver.getEntityDataForKey(ref, false));
    }

    @Override
    public void update(DTO dto) {
        ENTITY eOld = getEntityForKeyOrThrow(createKey(dto.ret$RefP()));
        ENTITY eNew = mapper.mapToEntity(dto, false);
        eOld.mergeFrom(eNew);
    }

    protected TypedQuery<ENTITY> createQuery(EntityManager em) {
        return null;
    }

    public List<DTO> readAll() {
        EntityManagerFactory emf = Jdp.getRequired(EntityManagerFactory.class);
        EntityManager em = emf.createEntityManager();
        TypedQuery<ENTITY> q = createQuery(em);
        List<ENTITY> results = q.getResultList();
        return mapper.mapListToDto(results);
    }

    protected abstract KEY createKeyInt(long ref);

    @Override
    public KEY createKey(Long ref) {
        return (ref == null) ? null : createKeyInt(ref.longValue());
    }

    @Override
    public KEY createKey(long ref) {
        return ref <= 0L ? null : createKeyInt(ref);
    }

    // backwards compat workaround
    private SearchCriteria buildCriteria(int limit, int offset, SearchFilter filter, List<SortColumn> sortColumns) {
        return new DummySearchCriteria(
                limit, offset,
                filter,
                sortColumns,
                null, null, null
            );
    }

    @Override
    public List<DataWithTracking<DTO, TRACKING>> query(int limit, int offset, SearchFilter filter, List<SortColumn> sortColumns) {
        return (List) mapper.mapListToDwt(resolver.search(buildCriteria(limit, offset, filter, sortColumns)));
    }

    @Override
    public List<Long> queryKeys(int limit, int offset, SearchFilter filter, List<SortColumn> sortColumns) {
        final List<KEY> result1 = resolver.searchKey(buildCriteria(limit, offset, filter, sortColumns));
        final List<Long> result2 = new ArrayList<>(result1.size());
        for (KEY k: result1)
            result2.add(k.ret$RefW());
        return result2;
    }
}
