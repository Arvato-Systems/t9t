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
package com.arvatosystems.t9t.base.be.tests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.be.impl.MutexSingleJVM;
import com.arvatosystems.t9t.base.services.IMutex;

public class MutexTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MutexTest.class);

    @Test
    public void mutexTest() throws InterruptedException {
        LOGGER.info("Mutexttest START");
        IMutex<Integer> mutex = new MutexSingleJVM<Integer>();
        final Supplier<Integer> task = () -> {
            LOGGER.info("start task");
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOGGER.info("end task");
            return 42;
        };

        ExecutorService execs = Executors.newFixedThreadPool(10);
        LOGGER.info("submitting tasks START");
        for (int i = 0; i < 10; ++i) {
            final int jjj = i;
            execs.execute(() -> { mutex.runSynchronizedOn(Long.valueOf(jjj % 3), task); } );
        }
        LOGGER.info("submitting tasks END");
        LOGGER.info("Mutexttest END");
        execs.shutdown();
        execs.awaitTermination(2, TimeUnit.MINUTES);
    }
}
