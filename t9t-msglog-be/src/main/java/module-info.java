module com.arvatosystems.t9t.msglog.be {
	exports com.arvatosystems.t9t.msglog.be.impl;

	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.cfg;
	requires com.arvatosystems.t9t.msglog.api;
	requires com.arvatosystems.t9t.msglog.sapi;
	requires com.arvatosystems.t9t.server;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.auth;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.joda.time;
	requires org.slf4j;
}