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
package com.arvatosystems.t9t.bucket.be.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.base.services.ISplittingOutputSessionProvider;
import com.arvatosystems.t9t.bucket.jpa.entities.BucketEntryEntity;
import com.arvatosystems.t9t.bucket.jpa.persistence.IBucketCounterEntityResolver;
import com.arvatosystems.t9t.bucket.jpa.persistence.IBucketEntryEntityResolver;
import com.arvatosystems.t9t.bucket.request.AbstractBucketExportRequest;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;

public abstract class AbstractBucketBaseExportRequestHandler<T extends AbstractBucketExportRequest> extends AbstractRequestHandler<T> {
    protected static final int MAX_CHUNK_SIZE = 1000;  // Oracle cannot do more

    protected final IBucketCounterEntityResolver counterResolver = Jdp.getRequired(IBucketCounterEntityResolver.class);
    protected final IBucketEntryEntityResolver   entryResolver   = Jdp.getRequired(IBucketEntryEntityResolver.class);
    protected final IAutonomousExecutor          autoExecutor    = Jdp.getRequired(IAutonomousExecutor.class);
    protected final Provider<IOutputSession> outputSessionprovider = Jdp.getProvider(IOutputSession.class);
    protected final ISplittingOutputSessionProvider splittingOutputSessionprovider = Jdp.getRequired(ISplittingOutputSessionProvider.class);

    protected List<Long> getRefs(String qualifier, int bucketNoToSelect) {
        EntityManager em = entryResolver.getEntityManager();
        TypedQuery<Long> query = em.createQuery(
                "SELECT be.ref FROM BucketEntryEntity be WHERE be.tenantRef = :tenantRef AND be.qualifier = :qualifier AND be.bucket = :bucketNo",
                Long.class);
        query.setParameter("tenantRef", entryResolver.getSharedTenantRef());
        query.setParameter("qualifier", qualifier);
        query.setParameter("bucketNo",  bucketNoToSelect);
        return new ArrayList<Long>(query.getResultList());
    }

    /** Method to retrieve the full entry records, only required by exports which have to distinguish cases when a record has been added. */
    protected Map<Long, BucketEntryEntity> getEntries(String qualifier, int bucketNoToSelect, List<Long> refs) {
        EntityManager em = entryResolver.getEntityManager();
        TypedQuery<BucketEntryEntity> query = em.createQuery(
                "SELECT be FROM BucketEntryEntity be WHERE be.tenantRef = :tenantRef AND be.qualifier = :qualifier AND be.bucket = :bucketNo AND be.ref IN :refs",
                BucketEntryEntity.class);
        query.setParameter("tenantRef", entryResolver.getSharedTenantRef());
        query.setParameter("qualifier", qualifier);
        query.setParameter("bucketNo",  bucketNoToSelect);
        query.setParameter("refs",      refs);
        List<BucketEntryEntity> result = query.getResultList();
        Map<Long, BucketEntryEntity> indexedResult = new HashMap<Long, BucketEntryEntity>(2 * result.size());
        for (BucketEntryEntity e : result)
            indexedResult.put(e.getRef(), e);
        return indexedResult;
    }

    /** Method to retrieve the full entry records, only required by exports which have to distinguish cases when a record has been added. */
    protected Map<Long, Integer> getModes(String qualifier, int bucketNoToSelect, List<Long> refs) {
        EntityManager em = entryResolver.getEntityManager();
        TypedQuery<BucketEntryEntity> query = em.createQuery(
                "SELECT be FROM BucketEntryEntity be WHERE be.tenantRef = :tenantRef AND be.qualifier = :qualifier AND be.bucket = :bucketNo AND be.ref IN :refs",
                BucketEntryEntity.class);
        query.setParameter("tenantRef", entryResolver.getSharedTenantRef());
        query.setParameter("qualifier", qualifier);
        query.setParameter("bucketNo",  bucketNoToSelect);
        query.setParameter("refs",      refs);
        List<BucketEntryEntity> result = query.getResultList();
        Map<Long, Integer> indexedResult = new HashMap<Long, Integer>(2 * result.size());
        for (BucketEntryEntity e : result)
            indexedResult.put(e.getRef(), e.getModes());
        return indexedResult;
    }
}
