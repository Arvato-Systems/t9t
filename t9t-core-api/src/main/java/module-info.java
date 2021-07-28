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

	requires transitive com.arvatosystems.t9t.annotations;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.joda.time;
}