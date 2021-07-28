module com.arvatosystems.t9t.vertx.metrics {
	exports com.arvatosystems.t9t.metrics.vertx.impl;

	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.vertx.base;
	requires transitive com.arvatosystems.t9t.jdp;
	requires transitive de.jpaw.jdp;
	requires transitive io.vertx.core;
	requires transitive io.vertx.metrics.micrometer;
	requires transitive io.vertx.web;
	requires transitive micrometer.core;
	requires transitive micrometer.registry.prometheus;
	requires org.slf4j;
}
