/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.client.connections;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleConnection {
    protected final int index;
    protected final HttpClient httpClient;
    protected final AtomicInteger currentPending = new AtomicInteger(0);
    protected final AtomicInteger totalUses = new AtomicInteger(0);
    protected final AtomicInteger peakUse = new AtomicInteger(0);

    protected SingleConnection(final int instanceNo, final ExecutorService executorService) {
        index = instanceNo;
        httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(20))
                .executor(executorService)
                .build();
    }
}
