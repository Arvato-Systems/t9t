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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.be.execution.RequestContextScope;
import com.arvatosystems.t9t.base.request.WaitUntilServerIdleRequest;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

/**
 * A technical request handler which is used to return information about currently executed processes.
 */
public class WaitUntilServerIdleRequestHandler extends AbstractReadOnlyRequestHandler<WaitUntilServerIdleRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitUntilServerIdleRequestHandler.class);

    private final RequestContextScope requestContextScope = Jdp.getRequired(RequestContextScope.class);
    private static final long DEFAULT_DELAY   = 20L;
    private static final long DEFAULT_TIMEOUT = 10_000L;
    private static final long DEFAULT_CONFIRM_DELAY = 10L;
    private static final int  DEFAULT_CONFIRM_COUNT = 3;

    @Override
    public ServiceResponse execute(final RequestContext ctx, final WaitUntilServerIdleRequest rq) {
        final Long onlySessionRef = rq.getOnlyMySession() ? ctx.internalHeaderParameters.getJwtInfo().getSessionRef() : null;
        final String onlyTenantId = T9tConstants.GLOBAL_TENANT_ID.equals(ctx.tenantId) ? null : ctx.tenantId;
        final long delay   = rq.getDelayInMs()   == null ? DEFAULT_DELAY   : rq.getDelayInMs();
        final long timeout = rq.getTimeoutInMs() == null ? DEFAULT_TIMEOUT : rq.getTimeoutInMs();
        final long startAt = System.currentTimeMillis();
        final long timeoutAt = startAt + timeout;
        final String what = rq.getOnlyMySession() ? "Session" : "System";
        int  confirmCount = DEFAULT_CONFIRM_COUNT;
        long confirmDelay = DEFAULT_CONFIRM_DELAY;

        boolean withConfirmation = false;
        if (rq.getConfirmAfterMs() != null) {
            confirmDelay     = rq.getConfirmAfterMs();
            withConfirmation = true;
        }
        if (rq.getConfirmCount() != null) {
            confirmCount     = rq.getConfirmCount();
            withConfirmation = true;
        }
        if (!withConfirmation) {
            // neither parameter set: do not confirm
            confirmCount = 0;
        }

        final ServiceResponse resp = new ServiceResponse();
        int retries = 0;
        try {
            do {
                if (isNowIdle(onlySessionRef, onlyTenantId, confirmCount, confirmDelay)) {
                    LOGGER.debug("{} is idle after {} retries of {} ms", what, retries, delay);
                    return resp;  // ok
                }
                Thread.sleep(delay);
                ++retries;
            } while (System.currentTimeMillis() < timeoutAt);
        } catch (final InterruptedException e) {
            LOGGER.info("Interrupted while waiting for {} idle after {} ms", what, System.currentTimeMillis() - startAt);
            resp.setReturnCode(2);  // not idle within timeout
            return resp;
        }
        LOGGER.info("Timeout after {} retries, waiting for {} idle ({} ms expired)", retries, what, System.currentTimeMillis() - startAt);
        resp.setReturnCode(1);  // not idle within timeout
        return resp;
    }

    private boolean isNowIdle(final Long onlySessionRef, final String onlyTenantId, int confirmCount, final long confirmAfterMs) throws InterruptedException {
        for (;;) {
            if (requestContextScope.numberOfProcesses(onlySessionRef, onlyTenantId) > 1) {
                // not idle
                return false;
            }
            if (confirmCount == 0) {
                return true;  // no more confirmations required
            }
            --confirmCount;
            Thread.sleep(confirmAfterMs);
        }
    }
}
