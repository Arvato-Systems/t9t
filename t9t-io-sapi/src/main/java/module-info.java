module com.arvatosystems.t9t.io.sapi {
	exports com.arvatosystems.t9t.out.services;
	exports com.arvatosystems.t9t.io.services;
	exports com.arvatosystems.t9t.in.services;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.io.api;
	requires com.arvatosystems.t9t.server;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jpaw.util;
}