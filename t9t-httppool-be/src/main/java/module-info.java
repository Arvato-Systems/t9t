module com.arvatosystems.t9t.httppool {
	exports com.arvatosystems.t9t.httppool.be;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.apache.httpcomponents.httpcore;
	requires org.slf4j;
}