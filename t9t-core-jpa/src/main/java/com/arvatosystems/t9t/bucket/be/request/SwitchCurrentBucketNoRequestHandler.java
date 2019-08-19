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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bucket.jpa.entities.BucketCounterEntity;
import com.arvatosystems.t9t.bucket.jpa.persistence.IBucketCounterEntityResolver;
import com.arvatosystems.t9t.bucket.jpa.persistence.IBucketEntryEntityResolver;
import com.arvatosystems.t9t.bucket.request.DeleteBucketRequest;
import com.arvatosystems.t9t.bucket.request.SwitchCurrentBucketNoRequest;
import com.arvatosystems.t9t.bucket.request.SwitchCurrentBucketNoResponse;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public class SwitchCurrentBucketNoRequestHandler extends AbstractRequestHandler<SwitchCurrentBucketNoRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwitchCurrentBucketNoRequestHandler.class);

    protected final IBucketCounterEntityResolver counterResolver = Jdp.getRequired(IBucketCounterEntityResolver.class);
    protected final IBucketEntryEntityResolver   entryResolver   = Jdp.getRequired(IBucketEntryEntityResolver.class);
    protected final IAutonomousExecutor          autoExecutor    = Jdp.getRequired(IAutonomousExecutor.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, SwitchCurrentBucketNoRequest rp) {
        final String qualifier = rp.getQualifier();
        final BucketCounterEntity counterEntity = counterResolver.findByQualifier(false, qualifier);
        final int oldBucketNo = counterEntity.getCurrentVal();
        int newBucketNo = oldBucketNo + 1;
        if (newBucketNo >= counterEntity.getMaxVal())
            newBucketNo = 0;  // restart

        // first, delete the target bucket, unless disabled
        if (rp.getDeleteBeforeSwitch()) {
            ctx.statusText = "Deleting new bucket " + qualifier + ":" + newBucketNo;
            DeleteBucketRequest dbrq = new DeleteBucketRequest();
            dbrq.setQualifier(qualifier);
            dbrq.setBucketNo(newBucketNo);
            ServiceResponse deleteResp = autoExecutor.execute(ctx, dbrq);
            if (!ApplicationException.isOk(deleteResp.getReturnCode()))
                return deleteResp;
        }

        counterEntity.setCurrentVal(newBucketNo);
        LOGGER.debug("Switching bucket {} from {} to {}", qualifier, oldBucketNo, newBucketNo);
        SwitchCurrentBucketNoResponse resp = new SwitchCurrentBucketNoResponse();
        resp.setBeforeSwitchBucketNo(oldBucketNo);
        resp.setAfterSwitchBucketNo(newBucketNo);
        return resp;
    }
}
