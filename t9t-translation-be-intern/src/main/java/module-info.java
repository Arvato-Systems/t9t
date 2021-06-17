module com.arvatosystems.t9t.translation.be.intern {
	exports com.arvatosystems.t9t.translation.be.request;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.init;
	requires com.arvatosystems.t9t.server;
	requires com.arvatosystems.t9t.translation.api;
	requires com.arvatosystems.t9t.translation.sapi;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.auth;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}