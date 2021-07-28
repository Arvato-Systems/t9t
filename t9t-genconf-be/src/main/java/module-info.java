module com.arvatosystems.t9t.genconf.be {
	exports com.arvatosystems.t9t.uiprefs.be.request;
	exports com.arvatosystems.t9t.uiprefsv3.be.request;
	exports com.arvatosystems.t9t.genconf.be.request;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.be;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.genconf.api;
	requires transitive com.arvatosystems.t9t.genconf.sapi;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.arvatosystems.t9t.translation.sapi;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires org.slf4j;
}