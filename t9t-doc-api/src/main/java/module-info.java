module com.arvatosystems.t9t.doc.api {
	exports com.arvatosystems.t9t.doc;
	exports com.arvatosystems.t9t.barcode.api;
	exports com.arvatosystems.t9t.doc.api;
	exports com.arvatosystems.t9t.doc.recipients;
	exports com.arvatosystems.t9t.doc.request;

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