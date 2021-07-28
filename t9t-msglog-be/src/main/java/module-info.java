module com.arvatosystems.t9t.msglog.be {
	exports com.arvatosystems.t9t.msglog.be.impl;

	requires transitive com.arvatosystems.t9t.annotations;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.arvatosystems.t9t.msglog.api;
	requires transitive com.arvatosystems.t9t.msglog.sapi;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.joda.time;
	requires org.slf4j;
}