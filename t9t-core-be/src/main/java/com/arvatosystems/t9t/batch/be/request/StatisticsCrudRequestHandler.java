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
package com.arvatosystems.t9t.batch.be.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler;
import com.arvatosystems.t9t.base.entities.WriteTracking;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsDTO;
import com.arvatosystems.t9t.batch.StatisticsRef;
import com.arvatosystems.t9t.batch.request.StatisticsCrudRequest;
import com.arvatosystems.t9t.batch.services.IStatisticsResolver;

import de.jpaw.dp.Jdp;

public class StatisticsCrudRequestHandler extends AbstractCrudSurrogateKeyBERequestHandler<StatisticsRef, StatisticsDTO, WriteTracking, StatisticsCrudRequest> {
    private final IStatisticsResolver resolver = Jdp.getRequired(IStatisticsResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final StatisticsCrudRequest crudRequest) throws Exception {
        return execute(ctx, crudRequest, resolver);
    }
}
