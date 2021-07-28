module com.arvatosystems.t9t.genconf.sapi {
	exports com.arvatosystems.t9t.genconf.services;
	exports com.arvatosystems.t9t.uiprefsv3.services;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.genconf.api;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jpaw.util;
}