/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.jpa.IEntityMapper;
import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey;
import com.arvatosystems.t9t.base.search.DummySearchCriteria;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.IRefResolver;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.bonaparte.refs.PersistenceException;
import de.jpaw.dp.Jdp;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

public abstract class AbstractJpaResolver<
  REF extends Ref,
  DTO extends REF,
  TRACKING extends TrackingBase,
  ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>
> implements IRefResolver<REF, DTO, TRACKING> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJpaResolver.class);

    protected final String entityName;
    protected final IResolverSurrogateKey<REF, TRACKING, ENTITY> resolver;
    protected final IEntityMapper<Long, DTO, TRACKING, ENTITY> mapper;

    protected AbstractJpaResolver(final String name,
            final IResolverSurrogateKey<REF, TRACKING, ENTITY> r,
            final IEntityMapper<Long, DTO, TRACKING, ENTITY> m) {
        entityName = name;
        resolver = r;
        mapper = m;
    }

    protected ENTITY getEntityForKeyOrThrow(final Long pk) {
        final ENTITY e = resolver.find(pk);
        if (e == null)
            throw new PersistenceException(PersistenceException.RECORD_DOES_NOT_EXIST, pk.longValue(), entityName);
        return e;
    }

    @Override
    public void clear() {
        resolver.getEntityManager().clear();
    }

    @Override
    public void create(final DTO dto) {
        resolver.save(mapper.mapToEntity(dto, false));
    }

    @Override
    public void flush() {
        resolver.getEntityManager().flush();
    }

    @Override
    public DTO getDTO(final Long ref) {
        return mapper.mapToDto(resolver.find(ref));
    }

    @Override
    public DTO getDTO(final REF ref) {
        return mapper.mapToDto(resolver.getEntityData(ref, false));
    }

    @Override
    public Long getRef(final REF ref) {
        return resolver.getRef(ref, false);
    }

    @Override
    public TRACKING getTracking(final Long pk) {
        return getEntityForKeyOrThrow(pk).ret$Tracking();
    }

    @Override
    public void remove(final Long ref) {
        resolver.remove(resolver.getEntityDataForKey(ref, false));
    }

    @Override
    public void update(final DTO dto) {
        final ENTITY eOld = getEntityForKeyOrThrow(dto.getObjectRef());
        final String oldTenant = resolver.getTenantId(eOld);

        if (!resolver.writeAllowed(oldTenant)) { // check if I am allowed to write to this entity
            LOGGER.error("Denying WRITE access to tenant {} for user {} in entity resolver {}",
                    oldTenant, resolver.getSharedTenantId(), resolver.getClass().getCanonicalName());
            throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
        }

        final ENTITY eNew = mapper.mapToEntity(dto, false);
        eOld.mergeFrom(eNew);
    }

    protected TypedQuery<ENTITY> createQuery(final EntityManager em) {
        return null;
    }

    public List<DTO> readAll() {
        final EntityManagerFactory emf = Jdp.getRequired(EntityManagerFactory.class);
        final EntityManager em = emf.createEntityManager();
        final TypedQuery<ENTITY> q = createQuery(em);
        final List<ENTITY> results = q.getResultList();
        return mapper.mapListToDto(results);
    }

    @Override
    public REF createKey(final long ref) {
        return ref <= 0L ? null : createKey(Long.valueOf(ref));
    }

    // backwards compat workaround
    private SearchCriteria buildCriteria(final int limit, final int offset, final SearchFilter filter, final List<SortColumn> sortColumns) {
        final SearchCriteria dummyCriteria = new DummySearchCriteria();
        dummyCriteria.setLimit(limit);
        dummyCriteria.setOffset(offset);
        dummyCriteria.setSearchFilter(filter);
        dummyCriteria.setSortColumns(sortColumns);
        return dummyCriteria;
    }

    @Override
    public List<DataWithTrackingS<DTO, TRACKING>> query(final int limit, final int offset, final SearchFilter filter, final List<SortColumn> sortColumns) {
        mapper.processSearchPrefixForDB(filter, sortColumns);
        return mapper.mapListToDwt(resolver.search(buildCriteria(limit, offset, filter, sortColumns)));
    }

    @Override
    public List<Long> queryKeys(final int limit, final int offset, final SearchFilter filter, final List<SortColumn> sortColumns) {
        mapper.processSearchPrefixForDB(filter, sortColumns);
        return resolver.searchKey(buildCriteria(limit, offset, filter, sortColumns));
    }

    @Override
    public Long createNewPrimaryKey() {
        return resolver.createNewPrimaryKey();
    }
}
