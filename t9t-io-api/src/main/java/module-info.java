module com.arvatosystems.t9t.io.api {
	exports com.arvatosystems.t9t.io.event;
	exports com.arvatosystems.t9t.io.request;
	exports com.arvatosystems.t9t.io;

	requires transitive com.arvatosystems.t9t.annotations;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.joda.time;
}