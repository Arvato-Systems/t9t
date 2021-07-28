module com.arvatosystems.t9t.email.be {
	exports com.arvatosystems.t9t.email.be.stubs;
	exports com.arvatosystems.t9t.email.be.api;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.arvatosystems.t9t.email.api;
	requires transitive com.arvatosystems.t9t.email.sapi;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive org.eclipse.xtend.lib;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}