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
package com.arvatosystems.t9t.statistics.service.impl;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsDTO;
import com.arvatosystems.t9t.batch.jpa.entities.StatisticsEntity;
import com.arvatosystems.t9t.batch.jpa.mapping.IStatisticsDTOMapper;
import com.arvatosystems.t9t.batch.jpa.persistence.IStatisticsEntityResolver;
import com.arvatosystems.t9t.statistics.services.IStatisticsService;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;

/**
 * Default implementation of {@linkplain IStatisticsService}.
 */
@Singleton
public class StatisticsService implements IStatisticsService {

    protected final IStatisticsEntityResolver statisticsEntityResolver = Jdp.getRequired(IStatisticsEntityResolver.class);
    protected final IStatisticsDTOMapper statisticsDataDTOMappers = Jdp.getRequired(IStatisticsDTOMapper.class);
    protected final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);

    @Override
    public void saveStatisticsData(final StatisticsDTO data) {
        final StatisticsEntity entity = statisticsDataDTOMappers.mapToEntity(data);
        if (entity.getJobRef() == null) {
            entity.setCProcessRef(ctxProvider.get().internalHeaderParameters.getProcessRef());
        }
        entity.setObjectRef(statisticsEntityResolver.createNewPrimaryKey());
        statisticsEntityResolver.save(entity);
    }
}
