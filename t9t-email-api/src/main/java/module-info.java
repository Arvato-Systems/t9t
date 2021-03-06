module com.arvatosystems.t9t.email.api {
	exports com.arvatosystems.t9t.email.api;
	exports com.arvatosystems.t9t.email;
	exports com.arvatosystems.t9t.email.request;

	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.base.api;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.joda.time;
}