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
package com.arvatosystems.t9t.base.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.LogMessageRequest;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.util.ToStringHelper;

public class LogMessageRequestHandler extends AbstractReadOnlyRequestHandler<LogMessageRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogMessageRequestHandler.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, LogMessageRequest rq) {
        LOGGER.info("Log message: {}", rq.getMessage());
        ctx.statusText = rq.getMessage();
        if (rq.getData() != null)
            LOGGER.info("Log data: {}", ToStringHelper.toStringML(rq.getData()));
        return ok();
    }
}
