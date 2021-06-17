module com.arvatosystems.t9t.vertx.metrics {
	exports com.arvatosystems.t9t.metrics.vertx.impl;

	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.vertx.base;
	requires de.jpaw.jdp;
	requires io.vertx.core;
	requires io.vertx.metrics.micrometer;
	requires io.vertx.web;
	requires micrometer.core;
	requires micrometer.registry.prometheus;
	requires org.slf4j;
}