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
package com.arvatosystems.t9t.monitoring.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.monitoring.request.QuerySystemParamsRequest;
import com.arvatosystems.t9t.monitoring.request.QuerySystemParamsResponse;

public class QuerySystemParamsRequestHandler extends AbstractRequestHandler<QuerySystemParamsRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuerySystemParamsRequestHandler.class);

    @Override
    public QuerySystemParamsResponse execute(RequestContext ctx, QuerySystemParamsRequest rq) {
        final Runtime rt = Runtime.getRuntime();
        final QuerySystemParamsResponse rs = new QuerySystemParamsResponse();
        rs.setCurrentTimeMillis(System.currentTimeMillis());
        rs.setAvailableProcessors(rt.availableProcessors());
        rs.setTotalMemory(rt.totalMemory());
        rs.setFreeMemory(rt.freeMemory());
        rs.setMaxMemory(rt.maxMemory());
        LOGGER.info("Runtime parameters queried: {}", rs);
        return rs;
    }
}
