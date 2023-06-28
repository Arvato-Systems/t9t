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
package com.arvatosystems.t9t.base.be.request;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.PerformWithRetryRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public class PerformWithRetryRequestHandler extends AbstractRequestHandler<PerformWithRetryRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformWithRetryRequestHandler.class);

    private final IAutonomousExecutor executor = Jdp.getRequired(IAutonomousExecutor.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final PerformWithRetryRequest request) {
        int count = 0;
        final long delay = request.getWaitBetweenRetries() == null ? 200L : request.getWaitBetweenRetries();
        Instant stopAt = request.getStopAt();
        if (request.getMaxNumberOfMilliseconds() != null) {
            final Instant alsoStopAt = Instant.now().plusMillis(request.getMaxNumberOfMilliseconds().longValue());
            if (stopAt == null || stopAt.isAfter(alsoStopAt))
                stopAt = alsoStopAt;
        }
        final RequestParameters rp = request.getRequest();
        ServiceResponse resp = new ServiceResponse();

        for (;;) {
            ++count;

            resp = executor.execute(ctx, rp, false);
            switch (resp.getReturnCode() / ApplicationException.CLASSIFICATION_FACTOR) {
            case ApplicationException.SUCCESS:
                return ok();
            case ApplicationException.CL_DENIED:
                if (request.getAllowNo()) {
                    return ok();
                }
                break;
            default:
                // any other error
                break;
            }
            if (request.getMaxNumberOfRuns() != null && count >= request.getMaxNumberOfRuns()) {
                LOGGER.info("Ending PerformWithRetry({}) after {} executions because max number of executions reached", rp.ret$PQON(), count);
                break;
            }
            if (stopAt != null) {
                if (stopAt.isBefore(Instant.now())) {
                    LOGGER.info("Ending PerformWithRetry({}) after {} executions due to time expiry", rp.ret$PQON(), count);
                    break;
                }
            }
            ctx.incrementProgress();  // one per request done
            try {
                Thread.sleep(delay);
            } catch (final InterruptedException e) {
                break;
            }
        }
        return resp;  // return the last response
    }
}
