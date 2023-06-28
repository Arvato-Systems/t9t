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
package com.arvatosystems.t9t.ssm.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.ssm.request.UpdateSchedulerDataRequest;
import com.arvatosystems.t9t.ssm.services.ISchedulerService;

import de.jpaw.dp.Jdp;

public class UpdateSchedulerDataRequestHandler extends AbstractRequestHandler<UpdateSchedulerDataRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSchedulerDataRequestHandler.class);

    private final ISchedulerService schedulerService = Jdp.getRequired(ISchedulerService.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final UpdateSchedulerDataRequest request) throws Exception {
        LOGGER.info("Scheduler direct update operation {} for scheduler ID {}", request.getOperationType(), request.getSchedulerId());
        schedulerService.updateScheduler(ctx, request.getOperationType(), request.getSchedulerId(), request.getSetup());
        return ok();
    }
}
