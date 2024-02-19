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
package com.arvatosystems.t9t.batch.jpa.request;

import com.arvatosystems.t9t.base.jpa.impl.AbstractSearchWithTotalsRequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsAggregationDTO;
import com.arvatosystems.t9t.batch.jpa.entities.StatisticsAggregationEntity;
import com.arvatosystems.t9t.batch.jpa.mapping.IStatisticsAggregationDTOMapper;
import com.arvatosystems.t9t.batch.jpa.persistence.IStatisticsAggregationEntityResolver;
import com.arvatosystems.t9t.batch.request.StatisticsAggregationSearchRequest;

import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.dp.Jdp;

public class StatisticsAggregationSearchRequestHandler extends
    AbstractSearchWithTotalsRequestHandler<Long, StatisticsAggregationDTO, NoTracking, StatisticsAggregationSearchRequest, StatisticsAggregationEntity> {

    private final IStatisticsAggregationEntityResolver resolver = Jdp.getRequired(IStatisticsAggregationEntityResolver.class);
    private final IStatisticsAggregationDTOMapper mapper = Jdp.getRequired(IStatisticsAggregationDTOMapper.class);

    @Override
    public ReadAllResponse<StatisticsAggregationDTO, NoTracking> execute(final RequestContext ctx, final StatisticsAggregationSearchRequest request)
        throws Exception {
        return execute(ctx, request, resolver, mapper);
    }

}
