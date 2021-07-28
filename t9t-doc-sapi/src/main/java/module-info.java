module com.arvatosystems.t9t.doc.sapi {
	exports com.arvatosystems.t9t.doc.services;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.doc.api;
	requires transitive com.arvatosystems.t9t.email.api;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.joda.time;
}