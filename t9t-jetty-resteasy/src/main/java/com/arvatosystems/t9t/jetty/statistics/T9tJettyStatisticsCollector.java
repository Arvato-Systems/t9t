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
