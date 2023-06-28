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
package com.arvatosystems.t9t.ssm.services;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO;

import de.jpaw.bonaparte.pojos.api.OperationType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;


public interface ISchedulerService {

    /**
     * This method schedules a job using the underlying service implementation.
     *
     * @param setup information about the job that shall be created
     */
    void createScheduledJob(RequestContext ctx, SchedulerSetupDTO setup);

    /**
     * This method removes an existing job and recreates it from scratch.
     *
     * @param setup information about the job that shall be created
     */
    void recreateScheduledJob(RequestContext ctx, SchedulerSetupDTO setup);

    /**
     * This method updates a previously scheduled job (just the schedule) using the underlying service implementation.
     *
     * @param setup information about the job that shall be updated
     */
    void updateScheduledJob(RequestContext ctx, SchedulerSetupDTO setup);

    /**
     * This method removes a previously schedules job using the underlying service implementation.
     * If "preventive" is set, the job is not expected to exist, and is only deleted to make sure.
     * Any messages about non-existing jobs should be ignored then!
     */
    void removeScheduledJob(RequestContext ctx, String schedulerId);

    /**
     * determine the CRON expression of a given setup
     * @param setup
     * @return
     */
    String determineCronExpression(SchedulerSetupDTO setup);

    /**
     * Updates the Quartz scheduler from event data.
     *
     * @param ctx           the context of the calling request handler or event handler
     * @param operationType the type of operation performed
     * @param               the key of the scheduler
     * @param setup         the description of the scheduler, for creation or update
     */
    void updateScheduler(@Nonnull RequestContext ctx, @Nonnull OperationType operationType, @Nonnull String schedulerId, @Nullable SchedulerSetupDTO setup);
}
