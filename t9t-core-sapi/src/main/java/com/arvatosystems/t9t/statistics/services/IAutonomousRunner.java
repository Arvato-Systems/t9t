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
package com.arvatosystems.t9t.statistics.services;

import java.util.function.Consumer;
import java.util.function.Function;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.StatisticsDTO;

/** Provides an interface to run a series of tasks as autonomous processes.
 * The loop checks for server shutdowns, increments the progress bar, and finally writes a statistics log entry. */
public interface IAutonomousRunner {
    public <T> void runSingleAutonomousTx(
        RequestContext ctx,
        int expectedTotal,                              // expected total number of records (Collections.size())
        Iterable<T> iterable,                           // provider of loop variables
        Function<T,RequestParameters> requestProvider,  // converts a loop variable to a request
        Consumer<StatisticsDTO> logEnhancer,            // optional (may be null): enhances the statistics output (set info1...)
        String processId                                // processId of the statistics log
    );
}
