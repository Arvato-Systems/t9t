module com.arvatosystems.t9t.genconf.api {
	exports com.arvatosystems.t9t.uiprefsv3;
	exports com.arvatosystems.t9t.genconf;
	exports com.arvatosystems.t9t.uiprefsv3.request;
	exports com.arvatosystems.t9t.uiprefs.request;
	exports com.arvatosystems.t9t.uiprefs;
	exports com.arvatosystems.t9t.genconf.request;

	requires transitive com.arvatosystems.t9t.annotations;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.joda.time;
}