module com.arvatosystems.t9t.rep.be {
	exports com.arvatosystems.t9t.rep.be.request;
	exports com.arvatosystems.t9t.rep.be.util;
	exports com.arvatosystems.t9t.rep.be;
	exports com.arvatosystems.t9t.rep.services.impl;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.doc.api;
	requires com.arvatosystems.t9t.io.api;
	requires com.arvatosystems.t9t.rep.api;
	requires com.arvatosystems.t9t.rep.sapi;
	requires com.arvatosystems.t9t.server;
	requires com.arvatosystems.t9t.translation.sapi;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.auth;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires jasperreports;
	requires java.sql;
	requires org.joda.time;
	requires org.slf4j;
}