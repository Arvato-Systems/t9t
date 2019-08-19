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
package com.arvatosystems.t9t.statistics.be.impl;

import java.util.function.Consumer;
import java.util.function.Function;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsDTO;
import com.arvatosystems.t9t.cfg.be.StatusProvider;
import com.arvatosystems.t9t.statistics.services.IAutonomousRunner;
import com.arvatosystems.t9t.statistics.services.IStatisticsService;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Singleton
public class AutonomousRunner implements IAutonomousRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutonomousRunner.class);

    protected final IStatisticsService statisticsWriter = Jdp.getRequired(IStatisticsService.class);
    protected final IAutonomousExecutor autonomousExecutor = Jdp.getRequired(IAutonomousExecutor.class);

    @Override
    public <T> void runSingleAutonomousTx(RequestContext ctx, int expectedTotal,
            Iterable<T> iterable, Function<T,RequestParameters> requestProvider,
            Consumer<StatisticsDTO> logEnhancer, String processId) {
        int numRecords = expectedTotal;
        int numErrors = 0;
        int numProcessed = 0;
        ctx.statusText = "Processing " + numRecords + " records";

        LOGGER.debug("Running autonomous transactions for {} records", numRecords);
        for (T object : iterable) {
            if (StatusProvider.isShutdownInProgress()) {
                LOGGER.info("Shutdown in progress detected - stopping after processing {} of {} requests", numProcessed, numRecords);
                break;
            }
            ++numProcessed;
            ctx.incrementProgress();
            RequestParameters request = requestProvider.apply(object);
            int retCode = autonomousExecutor.execute(ctx, request).getReturnCode();
            if (!ApplicationException.isOk(retCode))
                ++numErrors;
        }
        ctx.statusText = "Writing statistics";

        Instant now = new Instant();
        long timeTaken = now.getMillis() - ctx.executionStart.getMillis();
        LOGGER.info("Processed {} tasks ({} errors) in {} ms", numRecords, numErrors, timeTaken);
        StatisticsDTO stat = new StatisticsDTO();
        stat.setRecordsProcessed(numProcessed);
        stat.setRecordsError(numErrors);
        stat.setStartTime(ctx.executionStart);
        stat.setEndTime(now);
        stat.setCount1(numRecords);
        stat.setProcessId(processId);
        if (logEnhancer != null)
            logEnhancer.accept(stat);
        if (numProcessed < numRecords)
            stat.setInfo2("Interrupted by shutdown");
        statisticsWriter.saveStatisticsData(stat);
    }
}
