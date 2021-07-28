module com.arvatosystems.t9t.translation.be.intern {
	exports com.arvatosystems.t9t.translation.be.request;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.init;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.arvatosystems.t9t.translation.api;
	requires transitive com.arvatosystems.t9t.translation.sapi;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}