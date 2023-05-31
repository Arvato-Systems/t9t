package com.arvatosystems.t9t.metrics.vertx.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * {@link MeterBinder} for exporting t9t version to metrics endpoint.<br>
 * Sample output:
 * <pre>t9t_info{version="6.5-SNAPSHOT",} 1.0</pre>
 */
public class T9tVersionMetrics implements MeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(T9tVersionMetrics.class);

    private static final String NAME = "t9t.info";
    private static final String DESCRIPTION = "t9t version info";
    private static final String UNKNOWN = "unknown";
    private static final String VERSION = "version";

    @Override
    public void bindTo(final MeterRegistry registry) {
        String implementationVersion = this.getClass().getPackage().getImplementationVersion();
        if (T9tUtil.isBlank(implementationVersion)) { // normal when starting from eclipse
            LOGGER.debug("Cannot read implementation version from maven manifest");
            implementationVersion = UNKNOWN;
        }

        Gauge.builder(NAME, () -> Long.valueOf(1)).description(DESCRIPTION).tags(VERSION, implementationVersion).strongReference(true).register(registry);
    }
}
