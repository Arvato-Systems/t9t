module com.arvatosystems.t9t.auth.sapi {
	exports com.arvatosystems.t9t.auth.services;
	exports com.arvatosystems.t9t.auth.hooks;

	requires transitive com.arvatosystems.t9t.auth.apiext;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.google.common;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires transitive com.arvatosystems.t9t.auth.api;
}