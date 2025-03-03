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
package com.arvatosystems.t9t.monitoring.be.request;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.monitoring.request.QuerySystemParamsRequest;
import com.arvatosystems.t9t.monitoring.request.QuerySystemParamsResponse;

public class QuerySystemParamsRequestHandler extends AbstractRequestHandler<QuerySystemParamsRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuerySystemParamsRequestHandler.class);

    @Override
    public QuerySystemParamsResponse execute(final RequestContext ctx, final QuerySystemParamsRequest rq) {
        final QuerySystemParamsResponse rs = new QuerySystemParamsResponse();

        final Runtime rt = Runtime.getRuntime();
        rs.setCurrentTimeMillis(System.currentTimeMillis());
        rs.setAvailableProcessors(rt.availableProcessors());
        rs.setTotalMemory(rt.totalMemory());
        rs.setFreeMemory(rt.freeMemory());
        rs.setMaxMemory(rt.maxMemory());
        rs.setHostname(MessagingUtil.HOSTNAME);

        // get further data from the MXBean
        final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        rs.setJvmUptimeInMillis(runtimeBean.getUptime());
        rs.setName(runtimeBean.getName());
        rs.setVmName(runtimeBean.getVmName());
        rs.setVmVendor(runtimeBean.getVmVendor());
        rs.setVmVersion(runtimeBean.getVmVersion());
        rs.setSpecName(runtimeBean.getSpecName());
        rs.setSpecVendor(runtimeBean.getSpecVendor());
        rs.setSpecVersion(runtimeBean.getSpecVersion());

        LOGGER.info("Runtime parameters queried: {}", rs);
        return rs;
    }
}
