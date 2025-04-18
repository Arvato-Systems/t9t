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
package com.arvatosystems.t9t.base.vertx;

import com.arvatosystems.t9t.metrics.IMetricsProvider;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.RoutingContext;

/**
 * Metrics providers must implement this interface.
 *
 */
public interface IVertxMetricsProvider extends IMetricsProvider {
    /** Hook to install a handler for the /metrics route. */
    Handler<RoutingContext> getMetricsHandler();

    /** Hook to configure the vert.x options. */
    void setOptions(VertxOptions options);

    /** Hook to configure meters once vert.x is up and running. */
    void installMeters(Vertx vertx);
}
