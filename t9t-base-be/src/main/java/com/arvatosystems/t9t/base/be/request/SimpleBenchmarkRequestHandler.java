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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.SimpleBenchmarkRequest;
import com.arvatosystems.t9t.base.request.SimpleBenchmarkResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public class SimpleBenchmarkRequestHandler extends AbstractRequestHandler<SimpleBenchmarkRequest>  {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBenchmarkRequestHandler.class);

    protected final IExecutor messaging = Jdp.getRequired(IExecutor.class);
    protected final IAutonomousExecutor autoExecutor = Jdp.getRequired(IAutonomousExecutor.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, SimpleBenchmarkRequest request) {

        long minDuration = -1L;
        long maxDuration = -1L;
        long avgDuration = -1L;
        int repeats = request.getNumberOfIterations();
        RequestParameters rq = request.getRequest();
        rq.freeze();  // ensure that the benchmarked requests do not alter the

        long initialTime = System.nanoTime();
        long timeStamp = initialTime;
        long endTime = initialTime;
        while (--repeats >= 0) {
            RequestParameters rq2 = request.getMustCopyRequest() ? rq.ret$MutableClone(true, true) : rq;
            ServiceResponse resp = request.getRunAutonomous() ? autoExecutor.execute(ctx, rq2) : messaging.executeSynchronous(ctx, rq2);
            if (!request.getIgnoreErrors() && !ApplicationException.isOk(resp.getReturnCode())) {
                LOGGER.error("Invalid return code {}: {} in {}th iteration", resp.getReturnCode(), resp.getErrorDetails(),
                        request.getNumberOfIterations() - repeats);
                throw new T9tException(resp.getReturnCode(), resp.getErrorDetails());
            }
            endTime = System.nanoTime();
            long thisTime = endTime - timeStamp;
            if (minDuration < 0L || thisTime < minDuration)
                minDuration = thisTime;
            if (thisTime > maxDuration)
                maxDuration = thisTime;
            timeStamp = endTime;  // assign to new start point (avoid duplicate invocation of nanoTime())
        }

        SimpleBenchmarkResponse resp = new SimpleBenchmarkResponse();
        if (request.getNumberOfIterations() <= 0) {
            LOGGER.info("Count was 0 - no meaningful average possible");
        } else {
            avgDuration = (endTime - initialTime) / request.getNumberOfIterations();
            resp.setMinNanos(minDuration);
            resp.setMaxNanos(maxDuration);
            resp.setAvgNanos(avgDuration);
            LOGGER.info("Benchmark result for {} iterations: min = {} ns, max = {} ns, avg = {} ns, autonomous = {}",
                request.getNumberOfIterations(), minDuration, maxDuration, avgDuration, request.getRunAutonomous());
        }
        return resp;
    }
}
