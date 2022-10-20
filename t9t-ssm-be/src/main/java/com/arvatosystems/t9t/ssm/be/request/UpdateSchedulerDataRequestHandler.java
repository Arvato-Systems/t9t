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
package com.arvatosystems.t9t.ssm.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO;
import com.arvatosystems.t9t.ssm.request.UpdateSchedulerDataRequest;
import com.arvatosystems.t9t.ssm.services.ISchedulerService;

import de.jpaw.dp.Jdp;

public class UpdateSchedulerDataRequestHandler extends AbstractRequestHandler<UpdateSchedulerDataRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSchedulerDataRequestHandler.class);

    private final ISchedulerService schedulerService = Jdp.getRequired(ISchedulerService.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final UpdateSchedulerDataRequest request) throws Exception {
        final SchedulerSetupDTO setup = request.getSetup();  // just for shorthand
        final String schedulerId = request.getSchedulerId();  // just for shorthand
        LOGGER.info("Scheduler update operation {} for scheduler ID {}", request.getOperationType(), schedulerId);

        switch (request.getOperationType()) {
        case CREATE:
            schedulerService.createScheduledJob(ctx, setup);
            break;
        case DELETE:
            schedulerService.removeScheduledJob(ctx, schedulerId);
            break;
        case UPDATE:
            // update just the schedule
            schedulerService.updateScheduledJob(ctx, setup);
            break;
        case MERGE:
            // full update including job map (remove and recreate)
            schedulerService.recreateScheduledJob(ctx, setup);
            break;
        default:
            throw new T9tException(T9tException.UNSUPPORTED_OPERATION);
        }
        return ok();
    }
}
