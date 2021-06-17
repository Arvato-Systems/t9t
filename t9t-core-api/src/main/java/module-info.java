module com.arvatosystems.t9t.core.api {
	exports com.arvatosystems.t9t.batch;
	exports com.arvatosystems.t9t.core.request;
	exports com.arvatosystems.t9t.event;
	exports com.arvatosystems.t9t.plugins;
	exports com.arvatosystems.t9t.plugins.request;
	exports com.arvatosystems.t9t.bucket.request;
	exports com.arvatosystems.t9t.monitoring.request;
	exports com.arvatosystems.t9t.event.request;
	exports com.arvatosystems.t9t.plugins.api;
	exports com.arvatosystems.t9t.core;
	exports com.arvatosystems.t9t.batch.request;
	exports com.arvatosystems.t9t.bucket;

	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.base.api;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.joda.time;
}