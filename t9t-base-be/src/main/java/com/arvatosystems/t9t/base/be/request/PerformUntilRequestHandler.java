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
package com.arvatosystems.t9t.base.be.request;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.PerformUntilRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public class PerformUntilRequestHandler extends AbstractRequestHandler<PerformUntilRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformUntilRequestHandler.class);

    private final IAutonomousExecutor executor = Jdp.getRequired(IAutonomousExecutor.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final PerformUntilRequest request) {
        int count = 0;
        Instant stopAt = request.getStopAt();
        if (request.getMaxNumberOfMilliseconds() != null) {
            final Instant alsoStopAt = Instant.now().plusMillis(request.getMaxNumberOfMilliseconds().longValue());
            if (stopAt == null || stopAt.isAfter(alsoStopAt))
                stopAt = alsoStopAt;
        }
        final RequestParameters rp = request.getRequest();

        for (;;) {
            if (stopAt != null) {
                if (stopAt.isBefore(Instant.now())) {
                    LOGGER.info("Ending PerformUntil({}) after {} executions due to time expiry", rp.ret$PQON(), count);
                    break;
                }
            }
            if (request.getMaxNumberOfRuns() != null && count >= request.getMaxNumberOfRuns()) {
                LOGGER.info("Ending PerformUntil({}) after {} executions because max number of executions reached", rp.ret$PQON(), count);
                break;
            }
            ++count;

            final ServiceResponse resp = executor.execute(ctx, rp, false);  // check permissions
            switch (resp.getReturnCode() / ApplicationException.CLASSIFICATION_FACTOR) {
            case ApplicationException.SUCCESS:
                break; // continue processing
            case ApplicationException.CL_DENIED:
                if (request.getAllowNo()) {
                    break; // continue processing
                } else {
                    LOGGER.info("Ending PerformUntil({}) after {} executions because subrequest returns DENY", rp.ret$PQON(), count);
                    return resp; // stop
                }
            default:
                // any other error
                LOGGER.info("Ending PerformUntil({}) after {} executions because subrequest return an Exception", rp.ret$PQON(), count);
                return resp;
            }
            ctx.incrementProgress();  // one per request done
        }
        return new ServiceResponse();
    }
}
