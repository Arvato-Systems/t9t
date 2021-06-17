module com.arvatosystems.t9t.httppool {
	exports com.arvatosystems.t9t.httppool.be;

	requires com.arvatosystems.t9t.base.api;
	requires com.google.common;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jpaw.util;
	requires org.apache.httpcomponents.httpcore;
	requires org.slf4j;
}