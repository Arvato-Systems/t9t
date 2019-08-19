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
package com.arvatosystems.t9t.base.be.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IBucketWriter;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupShutdown;


// start and stop the bucket writer service at the desired point in time

@Startup(30051)
public class Init30051BucketWriter implements StartupShutdown {
    private static final Logger LOGGER = LoggerFactory.getLogger(Init30051BucketWriter.class);
    private IBucketWriter bucketWriter;

    @Override
    public void onStartup() {
        LOGGER.info("Starting bucket writer...");
        bucketWriter = Jdp.getRequired(IBucketWriter.class);
        bucketWriter.open();
    }

    @Override
    public void onShutdown() {
        LOGGER.info("Shutting down bucket writer...");
        bucketWriter.close();
    }
}
