module com.arvatosystems.t9t.auth.jwt {
	exports com.arvatosystems.t9t.auth.jwt;

	requires transitive de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.joda.time;
	requires org.slf4j;
}