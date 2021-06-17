module com.arvatosystems.t9t.auth.apiext {
	exports com.arvatosystems.t9t.authz.api;
	exports com.arvatosystems.t9t.authc.api;

	requires com.arvatosystems.t9t.base.api;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.auth;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.joda.time;
}