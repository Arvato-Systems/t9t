module com.arvatosystems.t9t.base.sapi {
	exports com.arvatosystems.t9t.base.services;
	exports com.arvatosystems.t9t.base.services.impl;

	requires transitive com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.server;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jpaw.util;
	requires java.sql;
	requires org.joda.time;
	requires org.slf4j;
}
