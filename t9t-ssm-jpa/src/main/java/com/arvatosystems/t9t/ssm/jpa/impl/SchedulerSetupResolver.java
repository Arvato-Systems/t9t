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
package com.arvatosystems.t9t.ssm.jpa.impl;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractJpaResolver;
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO;
import com.arvatosystems.t9t.ssm.SchedulerSetupRecurrenceType;
import com.arvatosystems.t9t.ssm.SchedulerSetupRef;
import com.arvatosystems.t9t.ssm.jpa.entities.SchedulerSetupEntity;
import com.arvatosystems.t9t.ssm.jpa.mapping.ISchedulerSetupDTOMapper;
import com.arvatosystems.t9t.ssm.jpa.persistence.ISchedulerSetupEntityResolver;
import com.arvatosystems.t9t.ssm.services.ISchedulerService;
import com.arvatosystems.t9t.ssm.services.ISchedulerSetupResolver;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class SchedulerSetupResolver extends AbstractJpaResolver<SchedulerSetupRef, SchedulerSetupDTO, FullTrackingWithVersion, SchedulerSetupEntity>
    implements ISchedulerSetupResolver {

    protected final ISchedulerService schedulerService = Jdp.getRequired(ISchedulerService.class);


    public SchedulerSetupResolver() {
        super("SchedulerSetup", Jdp.getRequired(ISchedulerSetupEntityResolver.class), Jdp.getRequired(ISchedulerSetupDTOMapper.class));
    }

    @Override
    public void update(SchedulerSetupDTO dto) {
        if (dto.getRecurrencyType() != SchedulerSetupRecurrenceType.FAST)
            dto.setCronExpression(schedulerService.determineCronExpression(dto));
        super.update(dto);
    }


    @Override
    public void create(SchedulerSetupDTO dto) {
        if (dto.getRecurrencyType() != SchedulerSetupRecurrenceType.FAST)
            dto.setCronExpression(schedulerService.determineCronExpression(dto));
        super.create(dto);
    }


    @Override
    public SchedulerSetupRef createKey(Long ref) {
        return ref == null ? null : new SchedulerSetupRef(ref);
    }
}
