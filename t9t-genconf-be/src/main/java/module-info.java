module com.arvatosystems.t9t.genconf.be {
	exports com.arvatosystems.t9t.uiprefs.be.request;
	exports com.arvatosystems.t9t.uiprefsv3.be.request;
	exports com.arvatosystems.t9t.genconf.be.request;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.be;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.genconf.api;
	requires com.arvatosystems.t9t.genconf.sapi;
	requires com.arvatosystems.t9t.server;
	requires com.arvatosystems.t9t.translation.sapi;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.auth;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.slf4j;
}