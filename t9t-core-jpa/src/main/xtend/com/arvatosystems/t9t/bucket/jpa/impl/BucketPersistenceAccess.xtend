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
package com.arvatosystems.t9t.bucket.jpa.impl

import com.arvatosystems.t9t.base.event.BucketWriteKey
import com.arvatosystems.t9t.bucket.jpa.entities.BucketCounterEntity
import com.arvatosystems.t9t.bucket.jpa.entities.BucketEntryEntity
import com.arvatosystems.t9t.bucket.services.IBucketPersistenceAccess
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.util.ToStringHelper
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import java.util.List
import java.util.Map
import javax.persistence.EntityManagerFactory
import org.joda.time.Instant

@Singleton
@AddLogger
class BucketPersistenceAccess implements IBucketPersistenceAccess {
    public static final Integer NO_BUCKET_COUNTER = 9999;
    @Inject EntityManagerFactory emf

    override open() {
    }

    override close() {
    }

    def protected getBucketNo(List<BucketCounterEntity> cel, Long tenantRef, String qualifier) {
        for (ce : cel) {
            if (ce.tenantRef == tenantRef && ce.qualifier == qualifier)
                return ce.currentVal
        }
        return NO_BUCKET_COUNTER
    }

    override write(Map<BucketWriteKey, Integer> m) {
        try {
            val now = new Instant
            val em = emf.createEntityManager

            try {
                em.transaction.begin

                // obtain counters
                val tenants = m.keySet.map[tenantRef].toList
                val buckets = m.keySet.map[typeId].toList
                val query1a = em.createQuery(
                    "SELECT bc FROM BucketCounterEntity bc WHERE bc.tenantRef IN :tenants AND bc.qualifier IN :buckets",
                    BucketCounterEntity)
                query1a.setParameter("tenants", tenants)
                query1a.setParameter("buckets", buckets)
                val currentCounters = query1a.resultList

                for (kv : m.entrySet) {
                    val bucketNo = getBucketNo(currentCounters, kv.key.tenantRef, kv.key.typeId)
                    // determine current bucket
                    val query = em.createQuery('''
                        SELECT be FROM BucketEntryEntity be
                         WHERE be.qualifier = :bucketId
                           AND be.bucket    = :bucket
                           AND be.ref       = :ref
                    ''', BucketEntryEntity);
    //                query.setParameter("tenantRef", kv.key.tenantRef)
                    query.setParameter("bucketId",  kv.key.typeId)
                    query.setParameter("ref",       kv.key.objectRef)
                    query.setParameter("bucket",    bucketNo)
                    val existingEntries = query.resultList
                    if (existingEntries.isEmpty) {
                        // create a new entry
                        val e        = new BucketEntryEntity
                        e.tenantRef  = kv.key.tenantRef
                        e.qualifier  = kv.key.typeId
                        e.ref        = kv.key.objectRef
                        e.bucket     = bucketNo
                        e.modes      = kv.value
                        e.CTimestamp = now
                        e.MTimestamp = now
                        em.persist(e)
                    } else {
                        // use existing entry: merge data
                        val e        = existingEntries.get(0)
                        e.modes      = Integer.valueOf(e.modes.intValue().bitwiseOr(kv.value.intValue()))
                        e.MTimestamp = now
                    }
                }
                em.transaction.commit
                em.clear
            } finally {
                em.close
            }
        } catch (Exception e) {
            LOGGER.error("Problem writing buckets: ", e)
            LOGGER.error("The following bucket entries have NOT been persisted: {}", ToStringHelper.toStringML(m))
        }
    }
}
