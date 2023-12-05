/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bucket.jpa.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bucket.jpa.entities.BucketCounterEntity;
import com.arvatosystems.t9t.bucket.jpa.persistence.IBucketCounterEntityResolver;
import com.arvatosystems.t9t.bucket.jpa.persistence.IBucketEntryEntityResolver;
import com.arvatosystems.t9t.bucket.request.ResetBucketNoInProgressRequest;

import de.jpaw.dp.Jdp;

public class ResetBucketNoInProgressRequestHandler extends AbstractRequestHandler<ResetBucketNoInProgressRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResetBucketNoInProgressRequestHandler.class);

    protected final IBucketCounterEntityResolver counterResolver = Jdp.getRequired(IBucketCounterEntityResolver.class);
    protected final IBucketEntryEntityResolver   entryResolver   = Jdp.getRequired(IBucketEntryEntityResolver.class);
    protected final IAutonomousExecutor          autoExecutor    = Jdp.getRequired(IAutonomousExecutor.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final ResetBucketNoInProgressRequest rp) {
        final String qualifier = rp.getQualifier();
        final BucketCounterEntity counterEntity = counterResolver.findByQualifier(false, qualifier);
        counterEntity.setBucketNoInProgress(null);
        LOGGER.debug("Resetting bucket {} bucketNoInProgress", qualifier);
        return ok();
    }
}