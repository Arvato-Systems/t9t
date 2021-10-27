/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.ssm.be.request;

import org.quartz.Scheduler;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.ssm.request.ClearAllRequest;

import de.jpaw.dp.Inject;
import de.jpaw.dp.Jdp;

public class ClearAllRequestHandler extends AbstractRequestHandler<ClearAllRequest> {
    @Inject
    private final Scheduler scheduler = Jdp.getRequired(Scheduler.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final ClearAllRequest crudRequest) throws Exception {
        this.scheduler.clear();
        return this.ok();
    }
}
