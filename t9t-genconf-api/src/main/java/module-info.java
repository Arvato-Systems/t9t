module com.arvatosystems.t9t.genconf.api {
	exports com.arvatosystems.t9t.uiprefsv3;
	exports com.arvatosystems.t9t.genconf;
	exports com.arvatosystems.t9t.uiprefsv3.request;
	exports com.arvatosystems.t9t.uiprefs.request;
	exports com.arvatosystems.t9t.uiprefs;
	exports com.arvatosystems.t9t.genconf.request;

	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.base.api;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.joda.time;
}