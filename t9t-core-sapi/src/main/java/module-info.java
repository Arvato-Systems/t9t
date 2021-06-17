module com.arvatosystems.t9t.core.sapi {
	exports com.arvatosystems.t9t.batch.services;
	exports com.arvatosystems.t9t.statistics.services;
	exports com.arvatosystems.t9t.core.services;
	exports com.arvatosystems.t9t.monitoring.services;
	exports com.arvatosystems.t9t.event.services;
	exports com.arvatosystems.t9t.bucket.services;
	exports com.arvatosystems.t9t.plugins.services;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.core.api;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jpaw.util;
	requires org.joda.time;
}