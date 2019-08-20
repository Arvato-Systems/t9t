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
package com.arvatosystems.t9t.monitoring.be.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.monitoring.request.SetJvmDebugFlagsRequest;
import com.arvatosystems.t9t.monitoring.services.IDebugFlags;

import de.jpaw.dp.Jdp;

public class SetJvmDebugFlagsRequestHandler extends AbstractReadOnlyRequestHandler<SetJvmDebugFlagsRequest> {
    protected final IDebugFlags debugFlags = Jdp.getRequired(IDebugFlags.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, SetJvmDebugFlagsRequest rq) throws Exception {
        debugFlags.setFlags(ctx, rq.getValues());
        return ok();
    }
}
