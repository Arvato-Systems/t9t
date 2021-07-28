module com.arvatosystems.t9t.auth.mocks {
	exports com.arvatosystems.t9t.auth.mocks;

	requires transitive com.arvatosystems.t9t.auth.sapi;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}
