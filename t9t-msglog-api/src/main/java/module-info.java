module com.arvatosystems.t9t.msglog.api {
	exports com.arvatosystems.t9t.msglog.request;
	exports com.arvatosystems.t9t.msglog;

	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.base.api;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.joda.time;
}