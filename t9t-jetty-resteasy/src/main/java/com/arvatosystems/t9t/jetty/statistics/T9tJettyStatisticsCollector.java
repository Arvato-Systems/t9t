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
package com.arvatosystems.t9t.jetty.statistics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.server.handler.StatisticsHandler;

import io.prometheus.client.jetty.JettyStatisticsCollector;

public class T9tJettyStatisticsCollector extends JettyStatisticsCollector {
    private final StatisticsHandler statisticsHandler;

    public T9tJettyStatisticsCollector(final StatisticsHandler statisticsHandler) {
        super(statisticsHandler);
        this.statisticsHandler = statisticsHandler;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        return Arrays.asList(buildGauge("jetty_requests_active", "Number of requests currently active", statisticsHandler.getRequestsActive()),
            buildGauge("jetty_requests_active_max", "Maximum number of requests that have been active at once", statisticsHandler.getRequestsActiveMax()),
            buildGauge("jetty_request_time_max_seconds", "Maximum time spent handling requests", statisticsHandler.getRequestTimeMax() / 1000.0),
            buildCounter("jetty_request_time_seconds_total", "Total time spent in all request handling", statisticsHandler.getRequestTimeTotal() / 1000.0));
    }

    private static MetricFamilySamples buildGauge(final String name, final String help, final double value) {
        return new MetricFamilySamples(name, Type.GAUGE, help,
            Collections.singletonList(new MetricFamilySamples.Sample(name, Collections.emptyList(), Collections.emptyList(), value)));
    }

    private static MetricFamilySamples buildCounter(final String name, final String help, final double value) {
        return new MetricFamilySamples(name, Type.COUNTER, help,
            Collections.singletonList(new MetricFamilySamples.Sample(name, Collections.emptyList(), Collections.emptyList(), value)));
    }
}
