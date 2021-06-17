module com.arvatosystems.t9t.genconf.sapi {
	exports com.arvatosystems.t9t.genconf.services;
	exports com.arvatosystems.t9t.uiprefsv3.services;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.genconf.api;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jpaw.util;
}