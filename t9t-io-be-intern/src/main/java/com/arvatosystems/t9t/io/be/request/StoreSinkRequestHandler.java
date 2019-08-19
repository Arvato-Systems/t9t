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
package com.arvatosystems.t9t.io.be.request;

import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.request.StoreSinkRequest;
import com.arvatosystems.t9t.out.services.IOutPersistenceAccess;

import de.jpaw.dp.Jdp;

public class StoreSinkRequestHandler extends AbstractRequestHandler<StoreSinkRequest> {
    // @Inject
    protected final IOutPersistenceAccess dpl = Jdp.getRequired(IOutPersistenceAccess.class);

    @Override
    public SinkCreatedResponse execute(RequestContext ctx, StoreSinkRequest rq) {
        Long sinkRef = dpl.getNewSinkKey();
        rq.getDataSink().setObjectRef(sinkRef);
        dpl.storeNewSink(rq.getDataSink());
        SinkCreatedResponse sinkCreatedResponse = new SinkCreatedResponse();
        sinkCreatedResponse.setSinkRef(sinkRef);
        sinkCreatedResponse.setReturnCode(0);
        return sinkCreatedResponse;
    }
}
