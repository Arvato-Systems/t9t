module com.arvatosystems.t9t.base.sapi {
	exports com.arvatosystems.t9t.base.services;
	exports com.arvatosystems.t9t.base.services.impl;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jpaw.util;
	requires transitive java.sql;
	requires transitive org.joda.time;
	requires org.slf4j;
}
