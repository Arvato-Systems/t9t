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
package com.arvatosystems.t9t.bucket.be.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.event.BucketWriteKey;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IBucketWriter;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bucket.request.SingleBucketWriteRequest;

import de.jpaw.dp.Jdp;

public class SingleBucketWriteRequestHandler extends AbstractRequestHandler<SingleBucketWriteRequest> {
    protected final IBucketWriter bucketWriter = Jdp.getRequired(IBucketWriter.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final SingleBucketWriteRequest rq) {
        if (!rq.getAsync()) {
            final Map<BucketWriteKey, Integer> cmds = new HashMap<>(rq.getValues().size());
            for (final Map.Entry<String, Integer> me: rq.getValues().entrySet()) {
                cmds.put(new BucketWriteKey(ctx.tenantId, rq.getObjectRef(), me.getKey()), me.getValue());
            }
            bucketWriter.writeToBuckets(cmds);
        } else {
            for (final Map.Entry<String, Integer> me: rq.getValues().entrySet()) {
                executor.writeToBuckets(Collections.singleton(me.getKey()), rq.getObjectRef(), me.getValue());
            }
        }
        return ok();
    }
}
