module com.arvatosystems.t9t.server {
	exports com.arvatosystems.t9t.server;
	exports com.arvatosystems.t9t.server.services;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.joda.time;
}
