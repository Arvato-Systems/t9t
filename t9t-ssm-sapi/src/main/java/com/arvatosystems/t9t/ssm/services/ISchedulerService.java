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
package com.arvatosystems.t9t.ssm.services;

import com.arvatosystems.t9t.ssm.SchedulerSetupDTO;


public interface ISchedulerService {

    /**
     * This method schedules a job using the underlying service implementation.
     *
     * @param setup information about the job that shall be created
     */
    void createScheduledJob(SchedulerSetupDTO setup);

    /**
     * This method updates a previously scheduled job using the underlying service implementation.
     *
     * @param setup information about the job that shall be updated
     */
    void updateScheduledJob(SchedulerSetupDTO oldSetup, SchedulerSetupDTO setup);

    /**
     * This method removes a previously schedules job using the underlying service implementation.
     * If "preventive" is set, the job is not expected to exist, and is only deleted to make sure.
     * Any messages about non-existing jobs should be ignored then!
     */
    void removeScheduledJob(String schedulerId);

    /**
     * determine the CRON expression of a given setup
     * @param setup
     * @return
     */
    String determineCronExpression(SchedulerSetupDTO setup);
}
