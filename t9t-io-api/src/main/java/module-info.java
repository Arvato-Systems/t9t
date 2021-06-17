module com.arvatosystems.t9t.io.api {
	exports com.arvatosystems.t9t.io.event;
	exports com.arvatosystems.t9t.io.request;
	exports com.arvatosystems.t9t.io;

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