/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.batch.jpa.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.jpa.entities.StatisticsEntity;
import com.arvatosystems.t9t.batch.jpa.mapping.IStatisticsDTOMapper;
import com.arvatosystems.t9t.batch.jpa.persistence.IStatisticsEntityResolver;
import com.arvatosystems.t9t.batch.request.LogStatisticsRequest;

import de.jpaw.dp.Jdp;

public class LogStatisticsRequestHandler extends AbstractRequestHandler<LogStatisticsRequest> {

    private final IStatisticsEntityResolver entityResolver = Jdp.getRequired(IStatisticsEntityResolver.class);
    private final IStatisticsDTOMapper dtoMapper = Jdp.getRequired(IStatisticsDTOMapper.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final LogStatisticsRequest rq) {
        // see StatisticsService
        final StatisticsEntity entity = dtoMapper.mapToEntity(rq.getStatistics());
        entity.setObjectRef(entityResolver.createNewPrimaryKey());
        this.entityResolver.save(entity);
        return ok();
    }
}
