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
package com.arvatosystems.t9t.bucket.jpa.impl;

import com.arvatosystems.t9t.base.event.BucketWriteKey;
import com.arvatosystems.t9t.bucket.jpa.entities.BucketCounterEntity;
import com.arvatosystems.t9t.bucket.jpa.entities.BucketEntryEntity;
import com.arvatosystems.t9t.bucket.services.IBucketPersistenceAccess;

import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BucketPersistenceAccess implements IBucketPersistenceAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(BucketPersistenceAccess.class);
    public static final Integer NO_BUCKET_COUNTER = Integer.valueOf(9999);

    private final EntityManagerFactory emf = Jdp.getRequired(EntityManagerFactory.class);

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public void write(final Map<BucketWriteKey, Integer> m) {
        try {
            final Instant now = Instant.now();
            final EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();

                // obtain counters
                final Set<BucketWriteKey> keys = m.keySet();
                final List<Long> tenants = new ArrayList<>(keys.size());
                final List<String> buckets = new ArrayList<>(keys.size());
                for (final BucketWriteKey key : keys) {
                    tenants.add(key.getTenantRef());
                    buckets.add(key.getTypeId());
                }
                final TypedQuery<BucketCounterEntity> query1a = em.createQuery(
                        "SELECT bc FROM BucketCounterEntity bc WHERE bc.tenantRef IN :tenants AND bc.qualifier IN :buckets", BucketCounterEntity.class);
                query1a.setParameter("tenants", tenants);
                query1a.setParameter("buckets", buckets);
                final List<BucketCounterEntity> currentCounters = query1a.getResultList();

                for (final Map.Entry<BucketWriteKey, Integer> kv : m.entrySet()) {
                    final Integer bucketNo = getBucketNo(currentCounters, kv.getKey().getTenantRef(), kv.getKey().getTypeId());
                    // determine current bucket
                    final TypedQuery<BucketEntryEntity> query = em.createQuery(
                            "SELECT be FROM BucketEntryEntity be WHERE be.qualifier = :bucketId AND be.bucket = :bucket AND be.ref = :ref",
                            BucketEntryEntity.class);
                    query.setParameter("bucketId", kv.getKey().getTypeId());
                    query.setParameter("ref", kv.getKey().getObjectRef());
                    query.setParameter("bucket", bucketNo);
                    final List<BucketEntryEntity> existingEntries = query.getResultList();
                    if (existingEntries.isEmpty()) {
                        // create a new entry
                        final BucketEntryEntity e = new BucketEntryEntity();
                        e.setTenantRef(kv.getKey().getTenantRef());
                        e.setQualifier(kv.getKey().getTypeId());
                        e.setRef(kv.getKey().getObjectRef());
                        e.setBucket(bucketNo);
                        e.setModes(kv.getValue());
                        e.setCTimestamp(now);
                        e.setMTimestamp(now);
                        em.persist(e);
                    } else {
                        // use existing entry: merge data
                        final BucketEntryEntity e = existingEntries.get(0);
                        e.setModes(Integer.valueOf((e.getModes().intValue() | kv.getValue().intValue())));
                        e.setMTimestamp(now);
                    }
                }
                em.getTransaction().commit();
                em.clear();
            } finally {
                em.close();
            }
        } catch (final Exception e) {
            LOGGER.error("Problem writing buckets: ", e);
            LOGGER.error("The following bucket entries have NOT been persisted: {}", ToStringHelper.toStringML(m));
        }
    }

    protected Integer getBucketNo(final List<BucketCounterEntity> cel, final Long tenantRef, final String qualifier) {
        for (final BucketCounterEntity ce : cel) {
            if (ce.getTenantRef().equals(tenantRef) && ce.getQualifier().equals(qualifier)) {
                return ce.getCurrentVal();
            }
        }
        return NO_BUCKET_COUNTER;
    }
}
