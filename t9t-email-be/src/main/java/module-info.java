module com.arvatosystems.t9t.email.be {
	exports com.arvatosystems.t9t.email.be.stubs;
	exports com.arvatosystems.t9t.email.be.api;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires com.arvatosystems.t9t.email.api;
	requires com.arvatosystems.t9t.email.sapi;
	requires com.arvatosystems.t9t.server;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jdp;
	requires org.eclipse.xtend.lib;
	requires org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}