module com.arvatosystems.t9t.rep.be {
	exports com.arvatosystems.t9t.rep.be.request;
	exports com.arvatosystems.t9t.rep.be.util;
	exports com.arvatosystems.t9t.rep.be;
	exports com.arvatosystems.t9t.rep.services.impl;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.doc.api;
	requires transitive com.arvatosystems.t9t.io.api;
	requires transitive com.arvatosystems.t9t.rep.api;
	requires transitive com.arvatosystems.t9t.rep.sapi;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.arvatosystems.t9t.translation.sapi;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires jasperreports;
	requires java.sql;
	requires transitive org.joda.time;
	requires org.slf4j;
}
