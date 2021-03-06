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
package com.arvatosystems.t9t.metrics.vertx.impl;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.vertx.IVertxMetricsProvider;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.PrometheusScrapingHandler;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;

@Singleton
public class VertxPrometheusMetricsProvider implements IVertxMetricsProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxPrometheusMetricsProvider.class);
    final private AtomicBoolean metricsEnabled = new AtomicBoolean(false);

    @Override
    public Handler<RoutingContext> getMetricsHandler() {
        LOGGER.info("Creating Prometheus route handler");
        return PrometheusScrapingHandler.create();
    }

    @Override
    public void setOptions(VertxOptions options) {
        final MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions()
          .setEnabled(true)
          .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true));
        options.setMetricsOptions(metricsOptions);
        metricsEnabled.set(true);
        LOGGER.info("Vert.x options set to include Prometheus via Micrometer");
    }

    @Override
    public void installMeters(Vertx vertx) {
        LOGGER.info("Registering histogram meter...");
        final PrometheusMeterRegistry registry = (PrometheusMeterRegistry) BackendRegistries.getDefaultNow();
        registry.config().meterFilter(
          new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
              return DistributionStatisticConfig.builder()
                .percentilesHistogram(true)
                .build()
                .merge(config);
            }
        });
        LOGGER.info("Registered histogram meter.");

        LOGGER.info("Adding JVM meters...");
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        LOGGER.info("Added JVM meters.");

        LOGGER.info("Adding autonoumous pool meters...");
        final ExecutorService autonomousPool = Jdp.getRequired(IAutonomousExecutor.class).getExecutorServiceForMetering();
        new ExecutorServiceMetrics(autonomousPool, "t9t-autonomous", Collections.emptySet()).bindTo(registry);
        LOGGER.info("Added autonoumous pool meters...");
    }
}
