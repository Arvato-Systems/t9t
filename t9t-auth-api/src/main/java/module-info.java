module com.arvatosystems.t9t.auth.api {
	exports com.arvatosystems.t9t.auth.request;
	exports com.arvatosystems.t9t.auth;

	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.auth.apiext;
	requires com.arvatosystems.t9t.base.api;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.auth;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.joda.time;
	requires org.slf4j;
}