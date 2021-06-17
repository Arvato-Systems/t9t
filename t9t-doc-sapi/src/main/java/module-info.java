module com.arvatosystems.t9t.doc.sapi {
	exports com.arvatosystems.t9t.doc.services;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.doc.api;
	requires com.arvatosystems.t9t.email.api;
	requires com.arvatosystems.t9t.server;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.joda.time;
}