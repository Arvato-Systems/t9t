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

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bucket.jpa.persistence.IBucketEntryEntityResolver;
import com.arvatosystems.t9t.bucket.request.DeleteBucketRequest;

import de.jpaw.dp.Jdp;

public class DeleteBucketRequestHandler extends AbstractRequestHandler<DeleteBucketRequest> {
    protected final IBucketEntryEntityResolver resolver = Jdp.getRequired(IBucketEntryEntityResolver.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, DeleteBucketRequest rp) {
        EntityManager em = resolver.getEntityManager();

        Query q = em.createQuery(
                "DELETE FROM BucketEntryEntity be WHERE be.tenantRef = :tenantRef AND be.qualifier = :qualifier AND be.bucket = :bucketNo"
        );
        q.setParameter("tenantRef", resolver.getSharedTenantRef());
        q.setParameter("qualifier", rp.getQualifier());
        q.setParameter("bucketNo", rp.getBucketNo());
        q.executeUpdate();
        return ok();
    }
}
